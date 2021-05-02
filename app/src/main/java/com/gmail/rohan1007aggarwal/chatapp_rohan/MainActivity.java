package com.gmail.rohan1007aggarwal.chatapp_rohan;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef, chatsRef, usersRef;
    private ProgressDialog loadingBar;
    private ConstraintLayout constraintLayout;
    private LinearLayout linearLayout;

    private RecyclerView chatsList;

    private String currentUserId;

    private Toolbar toolbar;

    private Button chats, groups, contacts, requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        chatsRef = rootRef.child("Contacts").child(currentUserId);
        usersRef = rootRef.child("users");

        chatsList = findViewById(R.id.chats_list);
        chatsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        toolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatBrewery");

        initializeFields();

        linearLayout.setVisibility(View.GONE);

        chats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        });

        groups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), GroupsFragment.class));
            }
        });

        contacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ContactsFragment.class));
            }
        });

        requests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RequestsFragments.class));
            }
        });

        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                linearLayout.setVisibility(View.VISIBLE);
            }
        });

//
//        loadingBar.setTitle("Loading Page");
//        loadingBar.setMessage("Please wait, we are loading the page...");
//        loadingBar.setCanceledOnTouchOutside(false);
//        loadingBar.show();

        CountDownTimer countDownTimer = new CountDownTimer(6000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
//                ImageView logo = findViewById(R.id.imageView);
//                logo.animate().alpha(1).setDuration(500);
            }

            @Override
            public void onFinish() {
                //loadingBar.dismiss();
                constraintLayout.setVisibility(View.VISIBLE);
//                ImageView logo = findViewById(R.id.imageView);
//                logo.animate().alpha(0).setDuration(500);
            }
        };
        countDownTimer.start();

    }





    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            sendUserToPhoneOtpActivity();
        }
        else {
            updateUserStatus("online");
            verifyUserExistence();
        }

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                        .setQuery(chatsRef, Contacts.class)
                        .build();

        FirebaseRecyclerAdapter<Contacts, MainActivity.ChatsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ChatsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final MainActivity.ChatsViewHolder holder, int position, @NonNull final Contacts model) {
                final String userIds = getRef(position).getKey();
                final String[] retImage = {"default_image"};
                usersRef.child(userIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if (snapshot.exists()) {
                            if (snapshot.hasChild("imageUrl")) {

                                retImage[0] = snapshot.child("imageUrl").getValue().toString();
                                Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }

                            final String retUsername = snapshot.child("username").getValue().toString();

                            holder.userName.setText(retUsername);


                            if (snapshot.child("userState").hasChild("state")) {
                                String state = snapshot.child("userState").child("state").getValue().toString();
                                String date = snapshot.child("userState").child("date").getValue().toString();
                                String time = snapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online")) {
                                    holder.userStatus.setText("online");
                                }
                                else if (state.equals("offline")) {
                                    holder.userStatus.setText("Last Seen: " + "\n" + date + " " + time);
                                }
                            }
                            else {
                                holder.userStatus.setText("offline");
                            }

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Intent chatIntent = new Intent(getApplicationContext(), ChatActivity.class);
                                    chatIntent.putExtra("visit_user_id", userIds);
                                    chatIntent.putExtra("visit_user_name", retUsername);
                                    chatIntent.putExtra("visit_image", retImage[0]);
                                    startActivity(chatIntent);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public MainActivity.ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                MainActivity.ChatsViewHolder viewHolder = new ChatsViewHolder(view);
                return viewHolder;
            }
        };

        chatsList.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            updateUserStatus("offline");
        }
    }

    public static class ChatsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;


        public ChatsViewHolder(@NonNull View itemView) {

            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
        }
    }





    private void initializeFields() {
        loadingBar = new ProgressDialog(this);
        chats = findViewById(R.id.button);
        groups = findViewById(R.id.button2);
        contacts = findViewById(R.id.button3);
        requests = findViewById(R.id.button4);
        constraintLayout = findViewById(R.id.constraintLayout);
        linearLayout = findViewById(R.id.linearLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_bar, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        switch (item.getItemId()){
            case R.id.main_find_friends:
                //Toast.makeText(this, "You are in Find friends now",Toast.LENGTH_SHORT).show();
                sendUserToFindFriendsActivity();
                return true;
            case R.id.main_create_group_option:
                requestNewGroup();
                //Toast.makeText(this, "Create Group",Toast.LENGTH_SHORT).show();
                return true;
            case R.id.settings:
                //Toast.makeText(this, "Settings",Toast.LENGTH_SHORT).show();
                sendUserToSettingsActivity();
                return true;
            case R.id.logout:
                //Toast.makeText(this, "You are Logged out",Toast.LENGTH_SHORT).show();
                updateUserStatus("offline");
                firebaseAuth.signOut();
                sendUserToPhoneOtpActivity();
                return true;
            default:
                return false;
        }

    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void sendUserToPhoneOtpActivity() {
        Intent phoneotpIntent = new Intent(MainActivity.this, PhoneotpActivity.class);
        phoneotpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(phoneotpIntent);
        finish();
    }

    private void sendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void verifyUserExistence() {
        String CurrentUserID = firebaseAuth.getCurrentUser().getUid();
        rootRef.child("users").child(CurrentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if ((snapshot.child("username").exists())) {
                    //Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");
        final EditText groupNameField = new EditText(MainActivity.this);
        groupNameField.setHint("Enter a name to the group");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)) {
                    groupNameField.requestFocus();
                    groupNameField.setError("Please write a Group Name...");
                    //Toast.makeText(MainActivity.this, "Please write a Group Name...", Toast.LENGTH_SHORT).show();
                }
                else {
                    createNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void createNewGroup(final String groupName) {
        rootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, groupName + "group is created successfully...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateUserStatus(String state){
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd MMM, yyyy" );
        saveCurrentDate = currentDate.format(calendar.getTime());
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm, a" );
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String, Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date", saveCurrentDate);
        onlineStateMap.put("state", state);

        rootRef.child("users").child(currentUserId).child("userState")
                .updateChildren(onlineStateMap);
    }
}
