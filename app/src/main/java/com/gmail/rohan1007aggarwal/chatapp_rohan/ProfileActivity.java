package com.gmail.rohan1007aggarwal.chatapp_rohan;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private String receiverUserId, senderUserId, current_state;
    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton, declineMessageRequestButton;

    private DatabaseReference userRef, chatRequestRef, contactsRef, notificationRef;
    private FirebaseAuth firebaseAuth;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        userRef = FirebaseDatabase.getInstance().getReference().child("users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("Notifications");

        toolbar = (Toolbar) findViewById(R.id.profile_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("User Profile");

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        senderUserId = firebaseAuth.getCurrentUser().getUid();
        current_state = "new";

        initializeFields();

        retrieveUserInfo();

        declineMessageRequestButton.setVisibility(View.GONE);
    }

    private void initializeFields() {
        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_profile_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        sendMessageRequestButton = findViewById(R.id.send_message_request_button);
        declineMessageRequestButton = findViewById(R.id.decline_message_request_button);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            this.finish();

        return super.onOptionsItemSelected(item);
    }

    private void retrieveUserInfo() {
        userRef.child(receiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.exists())&&(snapshot.hasChild("imageUrl"))) {
                    String userUsername = snapshot.child("username").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();
                    String userImage = snapshot.child("imageUrl").getValue().toString();

                    userProfileName.setText(userUsername);
                    userProfileStatus.setText(userStatus);
                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);

                    manageChatRequests();
                }
                else {
                    String userUsername = snapshot.child("username").getValue().toString();
                    String userStatus = snapshot.child("status").getValue().toString();

                    userProfileName.setText(userUsername);
                    userProfileStatus.setText(userStatus);

                    manageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void manageChatRequests() {

        chatRequestRef.child(senderUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(receiverUserId)) {
                            String request_type = snapshot.child(receiverUserId).child("request_type").getValue().toString();
                            if (request_type.equals("sent")) {
                                current_state = "request_sent";
                                sendMessageRequestButton.setText("Cancel Chat Request");
                            } else if (request_type.equals("received")) {
                                current_state = "request_received";
                                sendMessageRequestButton.setText("Accept Chat Request");

                                declineMessageRequestButton.setVisibility(View.VISIBLE);
                                declineMessageRequestButton.setEnabled(true);

                                declineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        }
                        else {
                            contactsRef.child(senderUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild(receiverUserId)) {
                                                current_state = "friends";
                                                sendMessageRequestButton.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if (!senderUserId.equals(receiverUserId)) {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);
                    if (current_state.equals("new")) {
                        sendChatRequest();
                    }
                    if (current_state.equals("request_sent")) {
                        cancelChatRequest();
                    }
                    if (current_state.equals("request_received")) {
                        acceptChatRequest();
                    }
                    if (current_state.equals("friends")) {
                        removeSpecificContact();
                    }
                }
            });
        }
        else {
            sendMessageRequestButton.setVisibility(View.GONE);
        }
    }

    private void removeSpecificContact() {
        contactsRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            contactsRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendMessageRequestButton.setEnabled(true);
                                            current_state = "new";
                                            sendMessageRequestButton.setText("Send Message");
                                            declineMessageRequestButton.setVisibility(View.GONE);
                                            declineMessageRequestButton.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptChatRequest() {
        contactsRef.child(senderUserId).child(receiverUserId)
                .child("Contacts")
                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    contactsRef.child(receiverUserId).child(senderUserId)
                            .child("Contacts")
                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                chatRequestRef.child(senderUserId).child(receiverUserId)
                                        .removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    chatRequestRef.child(receiverUserId).child(senderUserId)
                                                            .removeValue()
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    sendMessageRequestButton.setEnabled(true);
                                                                    current_state = "friends";
                                                                    sendMessageRequestButton.setText("Remove this Contact");
                                                                    declineMessageRequestButton.setVisibility(View.GONE);
                                                                    declineMessageRequestButton.setEnabled(false);
                                                                }
                                                            });
                                                }
                                            }
                                        });
                            }
                        }
                    });
                }
            }
        });
    }

    private void sendChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {

                                                sendMessageRequestButton.setEnabled(true);
                                                current_state = "request_sent";
                                                sendMessageRequestButton.setText("Cancel Chat Request");

                                                /*HashMap<String, String> chatNotificationMap = new HashMap<>();
                                                chatNotificationMap.put("from", senderUserId);
                                                chatNotificationMap.put("type", "request");
                                                notificationRef.child(receiverUserId).push()
                                                        .setValue(chatNotificationMap)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {

                                                            sendMessageRequestButton.setEnabled(true);
                                                            current_state = "request_sent";
                                                            sendMessageRequestButton.setText("Cancel Chat Request");
                                                        }
                                                    }
                                                });*/
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void cancelChatRequest() {
        chatRequestRef.child(senderUserId).child(receiverUserId)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            chatRequestRef.child(receiverUserId).child(senderUserId)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            sendMessageRequestButton.setEnabled(true);
                                            current_state = "new";
                                            sendMessageRequestButton.setText("Send Message");
                                            declineMessageRequestButton.setVisibility(View.GONE);
                                            declineMessageRequestButton.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });
    }

}
