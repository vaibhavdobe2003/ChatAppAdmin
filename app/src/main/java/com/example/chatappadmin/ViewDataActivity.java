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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

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
                                mobile.setText("No hidden data found!");
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

                            if (cleanData.contains("❌") && cleanData.contains("+")) {
                                cleanData = cleanData.replace("❌ No hidden data found.", "").trim();
                            }

                            if (cleanData.isEmpty() || cleanData.startsWith("❌")) {
                                showExtractedDialog("⚠️ No embedded info found.");
                            } else {
                                infoBlocks = cleanData.split("\\r?\\n");
                                showExtractedDialog(cleanData);
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

            // Display all content now
            mobile.setText(message.trim());
            imageView.setVisibility(View.VISIBLE);
            title.setVisibility(View.VISIBLE);
            dataTable.setVisibility(View.VISIBLE);
            green_divider.setVisibility(View.VISIBLE);
            black_divider.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);  // Hide ProgressBar when loading is done
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
}
