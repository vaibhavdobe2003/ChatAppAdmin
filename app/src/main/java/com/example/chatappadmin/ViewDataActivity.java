package com.example.chatappadmin;

import static com.example.chatappadmin.R.*;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ViewDataActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView title;
    private TextView mobile;
    private TableLayout dataTable;
    private ProgressBar progressBar;
    private Button sendData;
    private View black_divider,green_divider;

    private String[] infoBlocks;
    private static final String TAG = "ViewDataActivity";
    private Context context;
    private ExecutorService executorService = Executors.newFixedThreadPool(2); // Use multiple threads for parallel tasks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);

        imageView = findViewById(R.id.image);
        title = findViewById(R.id.title);
        mobile = findViewById(R.id.mobile);
        dataTable = findViewById(R.id.dataTable);
        progressBar = findViewById(R.id.progressBar);
        sendData=findViewById(R.id.sendData);
        black_divider=findViewById(R.id.black_divider);
        green_divider=findViewById(id.green_divider);

        context = this;

        // Hide all content except ProgressBar initially
        hideAllContent();

        String imageUrl = getIntent().getStringExtra("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            downloadImageAndExtractData(imageUrl);
        }
    }

    private void hideAllContent() {
        imageView.setVisibility(View.GONE);
        title.setVisibility(View.GONE);
        mobile.setVisibility(View.GONE);
        green_divider.setVisibility(View.GONE);
        black_divider.setVisibility(View.GONE);
        dataTable.setVisibility(View.GONE);
        sendData.setVisibility(View.GONE);

        progressBar.setVisibility(View.VISIBLE);  // Show progress bar
    }

    private void downloadImageAndExtractData(String imageUrl) {
        executorService.submit(() -> {
            try {
                // Download the image asynchronously using OkHttp
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(imageUrl).build();
                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "Image download failed: " + response.message());
                        return;
                    }

                    // Stream the image and resize it to reduce memory usage
                    InputStream inputStream = response.body().byteStream();
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();

                    if (bitmap == null) {
                        Log.e(TAG, "Image is null after downloading!");
                        return;
                    }

                    // Extract steganographic data asynchronously
                    SteganographyUtil.extractDataAsync(bitmap, new SteganographyUtil.ExtractCallback() {
                        @Override
                        public void onCompleted(String extractedData) {
                            if (extractedData == null || extractedData.trim().isEmpty()) {
//                                mobile.setText("No hidden data found!");
                                return;
                            }

                            runOnUiThread(() -> {
                                Glide.with(ViewDataActivity.this)
                                        .load(imageUrl)
                                        .placeholder(drawable.ic_image)
                                        .error(drawable.ic_image)
                                        .into(imageView);
                            });

                            String cleanData = extractedData.trim();
                            Log.d(TAG, "🧩 Extracted Raw: " + cleanData);

//                            if (cleanData.contains("❌") && cleanData.contains("+")) {
//                                cleanData = cleanData.replace("❌ No hidden data found.", "").trim();
//                            }

                            if (cleanData.isEmpty() || cleanData.startsWith("❌")) {
                                showExtractedDialog("⚠️ No embedded info found.");
                            } else {
                                infoBlocks = cleanData.split("\\r?\\n");
                                showExtractedDialog(cleanData);

                                String finalCleanData = cleanData;
                                sendData.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        fetchInvestigatorEmailAndSend(imageUrl, finalCleanData);
                                    }
                                });
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e(TAG, "Error during extraction: " + e.getMessage());
                        }
                    });

                } catch (IOException e) {
                    Log.e(TAG, "Error downloading image: " + e.getMessage());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error in download or extraction: " + e.getMessage());
            }
        });
    }

    private void showExtractedDialog(String message) {
        new Handler(Looper.getMainLooper()).post(() -> {
            dataTable.removeAllViews();  // Clear existing rows

            // Header row
            TableRow headerRow = new TableRow(context);
            headerRow.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

            String[] headers = {"\uD83D\uDCF1 Phone: ", "\uD83C\uDD94 UID", "\uD83D\uDD52 Time"};
            for (String header : headers) {
                TextView tv = new TextView(context);
                tv.setText(header);
                tv.setTypeface(null, Typeface.BOLD);
                tv.setPadding(24, 16, 24, 16);
                tv.setTextColor(getResources().getColor(android.R.color.white));
                headerRow.addView(tv);
            }
            dataTable.addView(headerRow);

            // Data rows
            for (String block : infoBlocks) {
                String[] parts = block.split(",");
                if (parts.length == 3) {
                    TableRow row = new TableRow(context);
                    row.setBackgroundColor(getResources().getColor(android.R.color.white));

                    for (int i = 0; i < 3; i++) {
                        TextView tv = new TextView(context);
                        tv.setText(i == 2 ? formatTimestamp(parts[i].trim()) : parts[i].trim());
                        tv.setPadding(24, 16, 24, 16);
                        tv.setTextColor(getResources().getColor(android.R.color.black));
                        row.addView(tv);
                    }
                    dataTable.addView(row);
                }
            }

            progressBar.setVisibility(View.GONE);  // Hide ProgressBar when loading is done
            // Display all content now
//            mobile.setText(message.trim());
            imageView.setVisibility(View.VISIBLE);
            title.setVisibility(View.VISIBLE);
            dataTable.setVisibility(View.VISIBLE);
            green_divider.setVisibility(View.VISIBLE);
            black_divider.setVisibility(View.VISIBLE);
            sendData.setVisibility(View.VISIBLE);

        });
    }

    private String formatTimestamp(String timestampMillis) {
        try {
            long millis = Long.parseLong(timestampMillis);
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a");
            return sdf.format(new Date(millis));
        } catch (Exception e) {
            return timestampMillis;
        }
    }


    private void fetchInvestigatorEmailAndSend(final String imageUrl, final String extractedData) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("investigateMessage")
                .whereEqualTo("imageUrl", imageUrl)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            String email = doc.getString("receiverId");
                            if (email != null && !email.isEmpty()) {
                                sendEmailWithExtractedData(extractedData, email);
                            } else {
                                Log.e(TAG, "No receiverId (email) found in Firestore for image: " + imageUrl);
                            }
                        }
                    } else {
                        Log.e(TAG, "No matching Firestore document found for image: " + imageUrl);
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error fetching document: " + e.getMessage()));
    }

    private void sendEmailWithExtractedData(String extractedData, String emailAddress) {
        ExecutorService emailExecutor = Executors.newSingleThreadExecutor();
        emailExecutor.submit(() -> {
            try {
                String senderEmail = "vaibhavdobe2310@gmail.com"; // replace with your sender email
                String senderPassword = "qfwg heul nccb qtak"; // use app password if Gmail has 2FA

                GmailSender sender = new GmailSender(senderEmail, senderPassword);

                String subject = "📄 Extracted Steganographic Data";
                StringBuilder body = new StringBuilder("Here is the extracted data:\n\n");
                for (String line : infoBlocks) {
                    body.append(line).append("\n");
                }

                sender.sendMail(subject, body.toString(), senderEmail, emailAddress);
                Log.d(TAG, "✅ Email sent successfully to " + emailAddress);
                runOnUiThread(() ->
                        Toast.makeText(ViewDataActivity.this, "Email sent to: " + emailAddress, Toast.LENGTH_LONG).show()
                );

            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to send email: " + e.getMessage());
                runOnUiThread(() ->
                        Toast.makeText(ViewDataActivity.this, "Email not sent : " + e.getMessage().toString(), Toast.LENGTH_LONG).show()
                );

            }
        });
    }



}
