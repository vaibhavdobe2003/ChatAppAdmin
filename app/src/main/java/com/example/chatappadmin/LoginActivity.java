package com.example.chatappadmin;

import static android.view.View.VISIBLE;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatappadmin.MainActivity;
import com.example.chatappadmin.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends Activity {

    private LinearLayout layoutPhone,layoutOTP;
//    private EditText etPhoneNumber;
    private EditText etOTP;
    private Button btnSendOTP, btnVerifyOTP;
    private TextView tvMsg;
    private FirebaseAuth mAuth;
    private String verificationId;
    private final String phone="+919673526970";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        etOTP = findViewById(R.id.etOTP);
        // Check if the user is already logged in and profile is complete
//        checkUserStatus();


//        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        //  etOTP = findViewById(R.id.etOTP);
        btnSendOTP = findViewById(R.id.btnSendOTP);
        btnVerifyOTP = findViewById(R.id.btnVerifyOTP);
        layoutOTP=findViewById(R.id.otpLayout);
        layoutPhone=findViewById(R.id.phoneLayout);
        tvMsg=findViewById(R.id.tvMsg);


        btnSendOTP.setOnClickListener(view -> sendOTP());

        btnVerifyOTP.setOnClickListener(view -> verifyOTP());
    }

//    private void checkUserStatus() {
//        FirebaseUser user = mAuth.getCurrentUser();
//        if (user != null) {
//            FirebaseFirestore.getInstance().collection("users").document(user.getUid()).get()
//                    .addOnSuccessListener(documentSnapshot -> {
//                        if (documentSnapshot.exists()) {
//                            String username = documentSnapshot.getString("username");
//                            String status = documentSnapshot.getString("status");
//
//                            if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(status)) {
//                                // Profile is complete, go to MainActivity
//                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
//                            } else {
//                                // Profile is incomplete, go to ProfileActivity
//                                startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
//                            }
//                            finish(); // Prevent user from going back to LoginActivity
//                        } else {
//                            // No profile found, send to ProfileActivity
//                            startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
//                            finish();
//                        }
//                    });
//        }
//    }

    private void sendOTP() {
        String phoneNumber = phone;
        if (TextUtils.isEmpty(phoneNumber)) {
            Toast.makeText(this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+919673526970")
                        .setTimeout(60L, TimeUnit.SECONDS)
                        .setActivity(this)
                        .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {
                                signInWithCredential(credential);
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                Toast.makeText(LoginActivity.this, "Verification Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
                                LoginActivity.this.verificationId = verificationId;
                                Toast.makeText(LoginActivity.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                                layoutPhone.setVisibility(View.GONE);
                                layoutOTP.setVisibility(VISIBLE);
                                tvMsg.setText("Enter The Verification Code Sent To +91"+phoneNumber);
//                                etOTP.setVisibility(VISIBLE);
//                                btnVerifyOTP.setVisibility(VISIBLE);
                            }
                        })
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void verifyOTP() {
        String otp = etOTP.getText().toString().trim();
        if (TextUtils.isEmpty(otp)) {
            Toast.makeText(this, "Enter OTP", Toast.LENGTH_SHORT).show();
            return;
        }

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Verified", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void resendOTP(View view){
        sendOTP();
    }
}

