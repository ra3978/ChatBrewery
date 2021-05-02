package com.gmail.rohan1007aggarwal.chatapp_rohan;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginActivity extends AppCompatActivity {

    private Button loginButton, registerButton, phoneLoginButton;
    private EditText userEmail, userPassword;
    private TextView needNewAccountLink, forgetPasswordLink;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference userRef;
    private ProgressDialog loadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");

        initializeFields();

/*        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToRegisterActivity();
            }
        });

 */

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser mFirebaseUser = firebaseAuth.getCurrentUser();
                if (mFirebaseUser != null) {
                    Toast.makeText(LoginActivity.this, "You are logged in", Toast.LENGTH_SHORT).show();
                    sendUserToMainActivity();
                } else {
                    Toast.makeText(LoginActivity.this, "Please Login", Toast.LENGTH_SHORT).show();
                }
            }
        };

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailAddress = userEmail.getText().toString();
                String pwd = userPassword.getText().toString();
                if (TextUtils.isEmpty(emailAddress)) {
                    userEmail.requestFocus();
                    userEmail.setError("Please enter Email Address");
                }
                if (TextUtils.isEmpty(pwd)) {
                    userPassword.requestFocus();
                    userPassword.setError("Please enter Password");
                }
                if (TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(pwd)) {
                    new android.app.AlertDialog.Builder(LoginActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Fields not filled!")
                            .setMessage("Please fill all the mandatory details to Login...")
                            .setNeutralButton("Ok", null)
                            .show();
                }
                else {

                    loadingBar.setTitle("Logging into account");
                    loadingBar.setMessage("please wait, we are logging into your account...");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();
                    firebaseAuth.signInWithEmailAndPassword(emailAddress, pwd)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
                                        sendUserToMainActivity();

                                        /*String currentUserId = firebaseAuth.getCurrentUser().getUid();
                                        String deviceToken = FirebaseInstanceId.getInstance().getToken();

                                        userRef.child(currentUserId).child("device_token")
                                                .setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), "Login Successful!", Toast.LENGTH_SHORT).show();
                                                    sendUserToMainActivity();
                                                }
                                            }
                                        });*/

                                    } else {
                                        Toast.makeText(getApplicationContext(), "Login Failed!", Toast.LENGTH_SHORT).show();
                                    }
                                    loadingBar.dismiss();
                                }
                            });
                }
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String emailAddress = userEmail.getText().toString();
                String pwd = userPassword.getText().toString();
                if (TextUtils.isEmpty(emailAddress)) {
                    userEmail.requestFocus();
                    userEmail.setError("Please enter Email Address");
                }
                if (TextUtils.isEmpty(pwd)) {
                    userPassword.requestFocus();
                    userPassword.setError("Please enter Password");
                }
                if (TextUtils.isEmpty(emailAddress) || TextUtils.isEmpty(pwd)) {
                    new android.app.AlertDialog.Builder(LoginActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Fields not filled!")
                            .setMessage("Please fill all the mandatory details to Login...")
                            .setNeutralButton("Ok", null)
                            .show();
                }
                else {

                    loadingBar.setTitle("Logging into account");
                    loadingBar.setMessage("please wait, we are logging into your account...");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();

                    firebaseAuth.createUserWithEmailAndPassword(emailAddress, pwd)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        /*String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                        String currentUserId = firebaseAuth.getCurrentUser().getUid();

                                        userRef.child(currentUserId).child("device_token")
                                                .setValue(deviceToken);*/

                                        // Create Database
                                        users_email information = new users_email(
                                                emailAddress
                                        );

                                        FirebaseDatabase.getInstance().getReference("users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .setValue(information).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(LoginActivity.this, "Sign Up Completed", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        sendUserToMainActivity();
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Login Error, Please Login Again", Toast.LENGTH_SHORT).show();
                                    }
                                    loadingBar.dismiss();
                                }
                            });
                }
            }
        });

        phoneLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), PhoneotpActivity.class));
            }
        });

/*        NeedNewAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SendUserToRegisterActivity();
            }
        });

 */

        forgetPasswordLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialogue = new AlertDialog.Builder(v.getContext());
                passwordResetDialogue.setTitle("Reset Password?");
                passwordResetDialogue.setMessage("Enter the Email to received Reset Link.");
                passwordResetDialogue.setView(resetMail);

                passwordResetDialogue.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //extract the email and send reset link
                        String mail = resetMail.getText().toString();
                        firebaseAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this, "Reset Link has been sent to your Email.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Error in Sending the Reset Link to your Email.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                passwordResetDialogue.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //close the dialogue

                    }
                });

                passwordResetDialogue.create().show();
            }
        });
    }

    private void initializeFields() {
        loginButton = findViewById(R.id.login_button);
        registerButton = findViewById(R.id.register_button);
        phoneLoginButton = findViewById(R.id.phone_login_button);
        userEmail = findViewById(R.id.login_email);
        userPassword = findViewById(R.id.login_password);
        //NeedNewAccountLink = findViewById(R.id.need_new_account_link);
        forgetPasswordLink = findViewById(R.id.forget_password_link);
        loadingBar = new ProgressDialog(this);
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(LoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

/*    private void SendUserToRegisterActivity() {
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(registerIntent);
    }

 */
}
