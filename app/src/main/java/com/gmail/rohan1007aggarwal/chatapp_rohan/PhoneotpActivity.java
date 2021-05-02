package com.gmail.rohan1007aggarwal.chatapp_rohan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.TimeUnit;

public class PhoneotpActivity extends AppCompatActivity {

    private ImageView imageView_login, imageView_active;
    private TextView textView_or, textView_active, textView_code;
    private EditText editText_phone, editText_otp;
    private Button button_email, button_login, button_resend, button_active;
    private PhoneAuthProvider.ForceResendingToken mResendCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String verificationCodeBySystem;
    private ProgressDialog loadingBar;
    private String currentUserId;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phoneotp);

        initializeFields();

        firebaseAuth = FirebaseAuth.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        button_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });

        button_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String phone = editText_phone.getText().toString();
                if (TextUtils.isEmpty(phone)) {
                    editText_phone.requestFocus();
                    editText_phone.setError("Please Phone No.");
                    new android.app.AlertDialog.Builder(PhoneotpActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Fields not filled!")
                            .setMessage("Please fill all the mandatory details to Login...")
                            .setNeutralButton("Ok", null)
                            .show();
                }
                else {
                    visibilityOfActiveItems();
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait, we are authenticating your phone...");
                    loadingBar.setCanceledOnTouchOutside(false);
                    loadingBar.show();

                        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                "+91" + phone,        // Phone number to verify
                                60,                 // Timeout duration
                                TimeUnit.SECONDS,   // Unit of timeout
                                PhoneotpActivity.this,               // Activity (for callback binding)
                                callbacks);
                    }
            }
        });

        button_active.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String code = editText_otp.getText().toString();

                if (code.isEmpty() || code.length() < 6) {
                    editText_otp.setError("Wrong OTP...");
                    editText_otp.requestFocus();
                    return;
                }

                loadingBar.setTitle("Verification code");
                loadingBar.setMessage("Please wait, we are verifying code...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCodeBySystem, code);
                signInUserByCredentials(credential);

            }
        });

        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {
                signInUserByCredentials(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e)
            {

                loadingBar.dismiss();
                Toast.makeText(PhoneotpActivity.this, e.getMessage() ,Toast.LENGTH_SHORT).show();

                visibilityOfLoginItems();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                loadingBar.dismiss();

                verificationCodeBySystem = s;
                mResendCode = forceResendingToken;
                Toast.makeText(PhoneotpActivity.this, "Verification Code Sent!" ,Toast.LENGTH_SHORT).show();

                visibilityOfActiveItems();
            }
        };
    }

    private void signInUserByCredentials(PhoneAuthCredential credential) {
        final String phone = editText_phone.getText().toString();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signInWithCredential(credential)
                    .addOnCompleteListener(PhoneotpActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                loadingBar.dismiss();

                                sendUserToMainActivity();

/*                            users_registerPhone information = new users_registerPhone(
                                    phone
                            );

                            FirebaseDatabase.getInstance().getReference("users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(information).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Toast.makeText(PhoneotpActivity.this, "Sign Up Completed", Toast.LENGTH_SHORT).show();
                                }
                            });

 */

                            } else {
                                Toast.makeText(PhoneotpActivity.this, "Verification Failed!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser != null) {
            sendUserToMainActivity();
        }

    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneotpActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void visibilityOfLoginItems(){
        editText_phone.setVisibility(View.VISIBLE);
        textView_or.setVisibility(View.VISIBLE);
        button_email.setVisibility(View.VISIBLE);
        imageView_login.setVisibility(View.VISIBLE);
        button_login.setVisibility(View.VISIBLE);
        textView_active.setVisibility(View.INVISIBLE);
        textView_code.setVisibility(View.INVISIBLE);
        editText_otp.setVisibility(View.INVISIBLE);
        button_resend.setVisibility(View.INVISIBLE);
        imageView_active.setVisibility(View.INVISIBLE);
        button_active.setVisibility(View.INVISIBLE);
    }

    private void visibilityOfActiveItems() {
        editText_phone.setVisibility(View.INVISIBLE);
        textView_or.setVisibility(View.INVISIBLE);
        button_email.setVisibility(View.INVISIBLE);
        imageView_login.setVisibility(View.INVISIBLE);
        button_login.setVisibility(View.INVISIBLE);
        textView_active.setVisibility(View.VISIBLE);
        textView_code.setVisibility(View.VISIBLE);
        editText_otp.setVisibility(View.VISIBLE);
        button_resend.setVisibility(View.VISIBLE);
        imageView_active.setVisibility(View.VISIBLE);
        button_active.setVisibility(View.VISIBLE);
    }

    private void initializeFields() {
        editText_phone = findViewById(R.id.editText_phone);
        editText_otp = findViewById(R.id.editText_otp);
        button_email = findViewById(R.id.button_email);
        button_login = findViewById(R.id.button_login);
        button_resend = findViewById(R.id.button_resend);
        button_active = findViewById(R.id.button_active);
        textView_active = findViewById(R.id.textView_active);
        textView_code = findViewById(R.id.textView_code);
        textView_or = findViewById(R.id.textView_or);
        imageView_active = findViewById(R.id.imageView_active);
        imageView_login = findViewById(R.id.imageView_login);
        loadingBar = new ProgressDialog(this);
    }
}
