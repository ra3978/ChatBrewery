package com.example.chatapp_rohan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

public class SettingsActivity extends AppCompatActivity {

    private Button set_update, verify_email;
    private EditText set_username, set_status, set_emailAddress;
    private ImageView imageView;

    private String CurrentUserID;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference RootRef;

    private Toolbar mToolbar;

    private static final int GalleryPick = 1;
    private StorageReference UserProfileImagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        firebaseAuth = FirebaseAuth.getInstance();
        CurrentUserID = firebaseAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();

/*        StorageReference filePath = UserProfileImagesRef.child(CurrentUserID + ".jpg");
        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(imageView);
            }
        });
*/



        set_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateUserSettings();
            }
        });

/*        verify_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send Verification Link
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                firebaseUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(SettingsActivity.this, "Verification link has been sent.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SettingsActivity.this, "Error in sending the Verification link.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

 */

        set_username.setVisibility(View.INVISIBLE);
        set_emailAddress.setVisibility(View.INVISIBLE);

        RetrieveUserInfo();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GalleryPick);
            }
        });


 /*       imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });

  */

    }

    private void InitializeFields() {
        set_update = findViewById(R.id.set_update);
        verify_email = findViewById(R.id.verify_email);
        set_username = findViewById(R.id.set_username);
        set_status = findViewById(R.id.set_status);
        set_emailAddress = findViewById(R.id.set_emailAddress);
        imageView = findViewById(R.id.imageView_dp);

        mToolbar = (Toolbar) findViewById(R.id.settings_activity_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Settings");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GalleryPick && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                final StorageReference filePath = UserProfileImagesRef.child(CurrentUserID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot task) {

                        Toast.makeText(SettingsActivity.this, "Profile Picture Updated!", Toast.LENGTH_SHORT).show();

                        Task<Uri> downloadUrl = filePath.getDownloadUrl();
                        downloadUrl.addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageReference = uri.toString();
                                RootRef.child("users").child(CurrentUserID).child("imageUrl").setValue(imageReference);

                            }
                        });

                        filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get().load(uri).into(imageView);
                            }
                        });

                    }
                });
            }
        }
    }

    private void UpdateUserSettings() {
        final String setUsername = set_username.getText().toString();
        final String setEmailAddress = set_emailAddress.getText().toString();
        final String setStatus = set_status.getText().toString();

        if (TextUtils.isEmpty(setUsername)) {
            Toast.makeText(SettingsActivity.this, "Please Enter Username", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setEmailAddress)) {
            Toast.makeText(SettingsActivity.this, "Please Enter Email Address", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus)) {
            Toast.makeText(SettingsActivity.this, "Please Enter Status", Toast.LENGTH_SHORT).show();
        }
        else {
            HashMap<String, Object> profileMap = new HashMap<>();
                profileMap.put("uid", CurrentUserID);
                profileMap.put("username", setUsername);
                profileMap.put("status", setStatus);
                profileMap.put("emailAddress", setEmailAddress);
            RootRef.child("users").child(CurrentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                SendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully...", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void SendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void SendUserToPhoneotpActivity() {
        Intent phoneotpIntent = new Intent(SettingsActivity.this, PhoneotpActivity.class);
        phoneotpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(phoneotpIntent);
        finish();
    }

    private void RetrieveUserInfo() {
        RootRef.child("users").child(CurrentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if ((snapshot.exists())&&(snapshot.hasChild("username"))&&(snapshot.hasChild("emailAddress"))&&(snapshot.hasChild("imageUrl"))) {
                            String retrieveUsername = snapshot.child("username").getValue().toString();
                            String retrieveStatus = snapshot.child("status").getValue().toString();
                            String retrieveEmailAddress = snapshot.child("emailAddress").getValue().toString();
                            String retrieveImage = snapshot.child("imageUrl").getValue().toString();

                            set_username.setText(retrieveUsername);
                            set_status.setText(retrieveStatus);
                            set_emailAddress.setText(retrieveEmailAddress);

                            Picasso.get().load(retrieveImage).into(imageView);
                        } else if ((snapshot.exists())&&(snapshot.hasChild("username"))&&(snapshot.hasChild("emailAddress"))) {
                            String retrieveUsername = snapshot.child("username").getValue().toString();
                            String retrieveStatus = snapshot.child("status").getValue().toString();
                            String retrieveEmailAddress = snapshot.child("emailAddress").getValue().toString();

                            set_username.setText(retrieveUsername);
                            set_status.setText(retrieveStatus);
                            set_emailAddress.setText(retrieveEmailAddress);

                        } else{
                            set_username.setVisibility(View.VISIBLE);
                            set_emailAddress.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please set and update your Profile information...", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }
}
