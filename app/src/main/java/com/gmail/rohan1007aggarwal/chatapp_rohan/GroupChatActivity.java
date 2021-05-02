package com.gmail.rohan1007aggarwal.chatapp_rohan;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef, usersRef, groupNameRef, groupMessageKeyRef;


    private Toolbar toolbar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView scrollView;
    private TextView displayTextMessages;
    private String currentGroupName, currentUserId, currentUsername, currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        currentGroupName = getIntent().getExtras().get("groupName").toString();
        rootRef = FirebaseDatabase.getInstance().getReference();
        usersRef = rootRef.child("users");
        groupNameRef = rootRef.child("Groups").child(currentGroupName);

        toolbar = (Toolbar) findViewById(R.id.group_chat_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(currentGroupName);

        initializeFields();

        getUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMessageToDatabase();

                userMessageInput.setText("");

                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home)
            this.finish();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        groupNameRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    displayMessages(snapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    displayMessages(snapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeFields() {

        sendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        scrollView = findViewById(R.id.my_scroll_view);
        displayTextMessages = findViewById(R.id.group_chat_display);
    }

    private void getUserInfo() {
        usersRef.child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    currentUsername = snapshot.child("username").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void saveMessageToDatabase() {
        String message = userMessageInput.getText().toString();
        String messageKey = groupNameRef.push().getKey();

        if (TextUtils.isEmpty(message)) {
            userMessageInput.requestFocus();
            userMessageInput.setError("Please Write a message...");
        }
        else {
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd MMM, yyyy");
            currentDate = currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(calForTime.getTime());

            HashMap<String, Object> groupMessageKey = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef = groupNameRef.child(messageKey);

            HashMap<String, Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("username", currentUsername);
                messageInfoMap.put("message", message);
                messageInfoMap.put("date", currentDate);
                messageInfoMap.put("time", currentTime);
            groupMessageKeyRef.updateChildren(messageInfoMap);
        }
    }

    private void displayMessages(DataSnapshot snapshot) {
        Iterator iterator = snapshot.getChildren().iterator();
        while (iterator.hasNext()){
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatUsername = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessages.append(chatUsername + ":\n" + chatMessage + "\n" + chatTime + "    " + chatDate + "\n\n\n");

            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }
}
