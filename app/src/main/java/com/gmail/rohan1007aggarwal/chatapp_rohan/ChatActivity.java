package com.gmail.rohan1007aggarwal.chatapp_rohan;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String messageReceiverId, messageReceiverName, messageReceiverImage, messageSenderId;
    private TextView userName, userLastSeen;
    private CircleImageView userImage;

    private Toolbar toolbar;
    private ImageButton sendMessageButton, sendFilesButton;
    private EditText messageInputText;

    private String messageSendTime, messageSendDate;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef;

    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;

    private String checker = "", myUrl = "";
    private StorageTask uploadTask;
    private Uri fileUri;
    private ProgressDialog loadingBar;

    private RecyclerView usersMessagesList;

    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAAtfrB4bE:APA91bHLm2hhVHBVK6eNYPEGbNGCHXm1Sa0PEc4E9gFnU_AjErmObtOkrP6u5K0AfeqWncimzZqKg6Qe0Vp6ilfvxvLlFst4EQS-o0wffv_LmcMMohYvV7dQ9eXjVjdQMnFRelJ2YzHi";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

    String NOTIFICATION_TITLE;
    String NOTIFICATION_MESSAGE;
    String TOPIC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        messageReceiverId = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_image").toString();

        firebaseAuth = FirebaseAuth.getInstance();
        messageSenderId = firebaseAuth.getCurrentUser().getUid();

        rootRef = FirebaseDatabase.getInstance().getReference();

        initializeFields();

        displayLastSeen();

        FirebaseMessaging.getInstance().subscribeToTopic(firebaseAuth.getUid())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(getApplicationContext(), "Topic Subscribed", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String messageTxt = "Shared A File";

                TOPIC = "/topics/" + messageReceiverId; //topic must match with what the receiver subscribed to

                rootRef.child("users").child(messageSenderId).child("username").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            NOTIFICATION_TITLE = snapshot.getValue().toString();
                            NOTIFICATION_MESSAGE = messageTxt;
                            JSONObject notification = new JSONObject();
                            JSONObject notificationBody = new JSONObject();
                            try {
                                notificationBody.put("title", NOTIFICATION_TITLE);
                                notificationBody.put("message", NOTIFICATION_MESSAGE);
                                notification.put("to", TOPIC);
                                notification.put("data", notificationBody);
                            } catch (JSONException e) {
                                Log.e(TAG, "onCreate: " + e.getMessage() );
                            }
                            sendNotification(notification);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                CharSequence options[] = new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "MS Word Files"
                        };
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select the File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if (i == 0) {
                            checker = "image";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Select Image"), 438);
                        }
                        if (i == 1) {
                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent, "Select PDF File"), 438);
                        }
                        if (i == 2) {
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent, "Select MS Docx File"), 438);
                        }
                    }
                });
                builder.show();
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

    private void initializeFields() {

        toolbar = (Toolbar) findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);

        userImage = findViewById(R.id.custom_profile_image);
        userName = findViewById(R.id.custom_profile_name);
        userLastSeen = findViewById(R.id.custom_user_last_seen);

        sendMessageButton = findViewById(R.id.send_message_button);
        sendFilesButton = findViewById(R.id.send_files_button);
        messageInputText = findViewById(R.id.input_message);

        messageAdapter = new MessageAdapter(messagesList);
        usersMessagesList = findViewById(R.id.privates_massages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        usersMessagesList.setLayoutManager(linearLayoutManager);
        usersMessagesList.setAdapter(messageAdapter);

        loadingBar = new ProgressDialog(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 438 && resultCode == RESULT_OK && data != null && data.getData() != null) {

            fileUri = data.getData();

            if (!checker.equals("image")) {

                loadingBar.setTitle("Sending the file");
                loadingBar.setMessage("Please wait, we are Sending the file when it is uploaded successfully...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");

                final String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
                final String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId)
                        .child(messageReceiverId).push();

                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + "." + checker);

                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map<String, String> messageDocumentBody = new HashMap<>();
                            messageDocumentBody.put("name", fileUri.getLastPathSegment());
                            messageDocumentBody.put("time", messageSendTime);
                            messageDocumentBody.put("date", messageSendDate);
                            messageDocumentBody.put("to", messageReceiverId);
                            messageDocumentBody.put("messageId", messagePushId);
                            messageDocumentBody.put("message", myUrl);
                            messageDocumentBody.put("type", checker);
                            messageDocumentBody.put("from", messageSenderId);

                            Map<String, Object> messageBodyDetails = new HashMap<String, Object>();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageDocumentBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageDocumentBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message sent successfully...", Toast.LENGTH_SHORT).show();
                                        MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.send_tone);
                                        mPlayer.start();
                                    } else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    messageInputText.setText("");
                                }
                            });
                        }
                    }
                });
            } else if (checker.equals("image")) {

                loadingBar.setTitle("Sending the file");
                loadingBar.setMessage("Please wait, we are Sending the file...");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");

                final String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
                final String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId)
                        .child(messageReceiverId).push();

                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + "." + "jpg");

                uploadTask = filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUrl = task.getResult();
                            myUrl = downloadUrl.toString();

                            Map<String, String> messageImageBody = new HashMap<>();
                            messageImageBody.put("name", fileUri.getLastPathSegment());
                            messageImageBody.put("time", messageSendTime);
                            messageImageBody.put("date", messageSendDate);
                            messageImageBody.put("to", messageReceiverId);
                            messageImageBody.put("messageId", messagePushId);
                            messageImageBody.put("message", myUrl);
                            messageImageBody.put("type", checker);
                            messageImageBody.put("from", messageSenderId);

                            Map<String, Object> messageBodyDetails = new HashMap<String, Object>();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageImageBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageImageBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Message sent successfully...", Toast.LENGTH_SHORT).show();
                                        MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.send_tone);
                                        mPlayer.start();
                                    } else {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                    messageInputText.setText("");
                                }
                            });
                        }
                    }
                });

            } else {
                loadingBar.dismiss();
                Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayLastSeen(){
        rootRef.child("users").child(messageReceiverId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.child("userState").hasChild("state")) {
                            String state = snapshot.child("userState").child("state").getValue().toString();
                            String date = snapshot.child("userState").child("date").getValue().toString();
                            String time = snapshot.child("userState").child("time").getValue().toString();

                            if (state.equals("online")) {
                                userLastSeen.setText("online");
                            }
                            else if (state.equals("offline")) {
                                userLastSeen.setText("Last Seen: " + "\n" + date + " " + time);
                            }
                        }
                        else {
                            userLastSeen.setText("offline");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();

        rootRef.child("Messages").child(messageSenderId).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        Messages messages = snapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
                        usersMessagesList.smoothScrollToPosition(usersMessagesList.getAdapter().getItemCount());

                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

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

    private void sendMessage(){

        final String messageTxt = messageInputText.getText().toString();
        TOPIC = "/topics/" + messageReceiverId; //topic must match with what the receiver subscribed to

        rootRef.child("users").child(messageSenderId).child("username").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {

                    NOTIFICATION_TITLE = snapshot.getValue().toString();
                    NOTIFICATION_MESSAGE = messageTxt;
                    JSONObject notification = new JSONObject();
                    JSONObject notificationBody = new JSONObject();
                    try {
                        notificationBody.put("title", NOTIFICATION_TITLE);
                        notificationBody.put("message", NOTIFICATION_MESSAGE);
                        notification.put("to", TOPIC);
                        notification.put("data", notificationBody);
                    } catch (JSONException e) {
                        Log.e(TAG, "onCreate: " + e.getMessage() );
                    }
                    sendNotification(notification);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if(TextUtils.isEmpty(messageTxt))
        {
            messageInputText.requestFocus();
            messageInputText.setError("Write the message...");
        }
        else {
            sendMessageButton.setEnabled(true);
            String messageSenderRef = "Messages/" + messageSenderId + "/" + messageReceiverId;
            String messageReceiverRef = "Messages/" + messageReceiverId + "/" + messageSenderId;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderId)
                    .child(messageReceiverId).push();

            String messagePushId = userMessageKeyRef.getKey();

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat messageTime = new SimpleDateFormat("hh:mm, a" );
            messageSendTime = messageTime.format(calendar.getTime());
            SimpleDateFormat messageDate = new SimpleDateFormat("dd MMM, yyyy" );
            messageSendDate = messageDate.format(calendar.getTime());

            Map<String, String> messageTextBody = new HashMap<>();
            messageTextBody.put("time", messageSendTime);
            messageTextBody.put("date", messageSendDate);
            messageTextBody.put("to", messageReceiverId);
            messageTextBody.put("messageId", messageSenderId);
            messageTextBody.put("message", messageTxt);
            messageTextBody.put("type", "text");
            messageTextBody.put("from", messageSenderId);

            Map<String, Object> messageBodyDetails = new HashMap<String, Object>();
            messageBodyDetails.put(messageSenderRef + "/" + messagePushId, messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/" + messagePushId, messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(ChatActivity.this, "Message sent successfully...", Toast.LENGTH_SHORT).show();
                        MediaPlayer mPlayer = MediaPlayer.create(getApplicationContext(), R.raw.send_tone);
                        mPlayer.start();
                    }
                    else {
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                    messageInputText.setText("");
                }
            });
        }

    }

    private void sendNotification(JSONObject notification) {

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: " + response.toString());
                        messageInputText.setText("");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(ChatActivity.this, "Request error", Toast.LENGTH_LONG).show();
                        Log.i(TAG, "onErrorResponse: Didn't work");
                    }
                }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }

    public static class MySingleton {
        private  static MySingleton instance;
        private RequestQueue requestQueue;
        private Context ctx;

        private MySingleton(Context context) {
            ctx = context;
            requestQueue = getRequestQueue();
        }

        public static synchronized MySingleton getInstance(Context context) {
            if (instance == null) {
                instance = new MySingleton(context);
            }
            return instance;
        }

        public RequestQueue getRequestQueue() {
            if (requestQueue == null) {
                // getApplicationContext() is key, it keeps you from leaking the
                // Activity or BroadcastReceiver if someone passes one in.
                requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
            }
            return requestQueue;
        }

        public <T> void addToRequestQueue(Request<T> req) {
            getRequestQueue().add(req);
        }
    }
}