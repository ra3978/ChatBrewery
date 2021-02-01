package com.example.chatapp_rohan;

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

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PhoneotpActivity extends AppCompatActivity {

    ImageView imageView_login, imageView_active;
    TextView textView_or, textView_active, textView_code;
    EditText editText_phone, editText_otp;
    Button button_email, button_login, button_resend, button_active;
    private PhoneAuthProvider.ForceResendingToken mResendCode;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String verificationCodeBySystem;
    private ProgressDialog loadingBar;
    private String CurrentUserID;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phoneotp);

        InitializeFeilds();

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
                VisibilityOfActiveItems();
                final String phone = editText_phone.getText().toString();

                loadingBar.setTitle("Phone Verification");
                loadingBar.setMessage("Please wait, we are authenticating your phone...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                if (TextUtils.isEmpty(phone)) {
                    Toast.makeText(PhoneotpActivity.this, "Please Enter Phone No.", Toast.LENGTH_SHORT).show();
                    editText_phone.requestFocus();
                }
                else {

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            "+91" + phone,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneotpActivity.this,               // Activity (for callback binding)
                            mCallbacks);
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
                signInUserByCredntials(credential);

            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential)
            {
                signInUserByCredntials(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e)
            {

                loadingBar.dismiss();
                Toast.makeText(PhoneotpActivity.this, e.getMessage() ,Toast.LENGTH_SHORT).show();

                VisibilityOfLoginItems();
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);

                loadingBar.dismiss();

                verificationCodeBySystem = s;
                mResendCode = forceResendingToken;
                Toast.makeText(PhoneotpActivity.this, "Verification Code Sent!" ,Toast.LENGTH_SHORT).show();

                VisibilityOfActiveItems();
            }
        };
    }

    private void signInUserByCredntials(PhoneAuthCredential credential) {
        final String phone = editText_phone.getText().toString();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(PhoneotpActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            loadingBar.dismiss();

                            SendUserToMainActivity();

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

                        }
                        else {
                            Toast.makeText(PhoneotpActivity.this, "Verification Failed!" ,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser != null) {
            SendUserToMainActivity();
        }

    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneotpActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void VisibilityOfLoginItems(){
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

    private void VisibilityOfActiveItems() {
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

    private void InitializeFeilds() {
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
