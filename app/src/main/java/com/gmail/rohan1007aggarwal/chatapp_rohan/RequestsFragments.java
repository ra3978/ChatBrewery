package com.gmail.rohan1007aggarwal.chatapp_rohan;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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

public class RequestsFragments extends AppCompatActivity {

    private Button chats, groups, contacts, requests;

    private RecyclerView myRequestList;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef, requestRef, usersRef, contactsRef;
    private String currentUserId;

    private LinearLayout linearLayout;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests_fragments);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        requestRef = rootRef.child("Chat Requests");
        usersRef = rootRef.child("users");
        contactsRef = rootRef.child("Contacts");

        toolbar = (Toolbar) findViewById(R.id.requests_fragments_activity_toolbar);
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

        myRequestList = findViewById(R.id.chat_requests_list);
        myRequestList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(requestRef.child(currentUserId), Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ChatRequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, ChatRequestViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ChatRequestViewHolder holder, int position, @NonNull Contacts model) {
                        holder.itemView.findViewById(R.id.request_accept_button).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.VISIBLE);

                        final String list_user_id = getRef(position).getKey();

                        DatabaseReference getTypeRef = getRef(position).child("request_type").getRef();
                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String type = snapshot.getValue().toString();
                                    if (type.equals("received")) {
                                        usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.hasChild("imageUrl")) {
                                                    final String requestUserImage = snapshot.child("imageUrl").getValue().toString();

                                                    Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                }
                                                final String requestUsername = snapshot.child("username").getValue().toString();
                                                final String requestStatus = snapshot.child("status").getValue().toString();

                                                holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        contactsRef.child(currentUserId).child(list_user_id).child("Contacts")
                                                                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    contactsRef.child(list_user_id).child(currentUserId).child("Contacts")
                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                requestRef.child(currentUserId).child(list_user_id)
                                                                                        .removeValue()
                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                            @Override
                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                if (task.isSuccessful()) {
                                                                                                    requestRef.child(list_user_id).child(currentUserId)
                                                                                                            .removeValue()
                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                @Override
                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                    if (task.isSuccessful()) {
                                                                                                                        Toast.makeText(RequestsFragments.this, "New contact saved.", Toast.LENGTH_SHORT).show();
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
                                                            }
                                                        });
                                                    }
                                                });

                                                holder.cancelButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        requestRef.child(currentUserId).child(list_user_id)
                                                                .removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            requestRef.child(list_user_id).child(currentUserId)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                Toast.makeText(RequestsFragments.this, "Contact Deleted.", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });

                                                holder.userName.setText(requestUsername);
                                                holder.userStatus.setText("Wants to connect With you!");
                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        new AlertDialog.Builder(RequestsFragments.this, R.style.AlertDialog2)
                                                                .setTitle(requestUsername + " Chat Request")
                                                                .setMessage("Do you want to add this contact?")
                                                                .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int i) {
                                                                        contactsRef.child(currentUserId).child(list_user_id).child("Contacts")
                                                                                .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    contactsRef.child(list_user_id).child(currentUserId).child("Contacts")
                                                                                            .setValue("Saved").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                requestRef.child(currentUserId).child(list_user_id)
                                                                                                        .removeValue()
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if (task.isSuccessful()) {
                                                                                                                    requestRef.child(list_user_id).child(currentUserId)
                                                                                                                            .removeValue()
                                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                @Override
                                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                    if (task.isSuccessful()) {
                                                                                                                                        Toast.makeText(RequestsFragments.this, "New contact saved.", Toast.LENGTH_SHORT).show();
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
                                                                            }
                                                                        });
                                                                    }
                                                                })
                                                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        requestRef.child(currentUserId).child(list_user_id)
                                                                                .removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            requestRef.child(list_user_id).child(currentUserId)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                Toast.makeText(RequestsFragments.this, "Contact Deleted.", Toast.LENGTH_SHORT).show();
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                })
                                                                .show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });
                                    } else if (type.equals("sent")) {
                                        Button requestSentButton = holder.itemView.findViewById(R.id.request_accept_button);
                                        requestSentButton.setText("Request Sent");

                                        holder.itemView.findViewById(R.id.request_cancel_button).setVisibility(View.INVISIBLE);

                                        usersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if (snapshot.hasChild("imageUrl")) {
                                                    final String requestUserImage = snapshot.child("imageUrl").getValue().toString();

                                                    Picasso.get().load(requestUserImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                                                }
                                                final String requestUsername = snapshot.child("username").getValue().toString();
                                                final String requestStatus = snapshot.child("status").getValue().toString();

                                                holder.acceptButton.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        requestRef.child(currentUserId).child(list_user_id)
                                                                .removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            requestRef.child(list_user_id).child(currentUserId)
                                                                                    .removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if (task.isSuccessful()) {
                                                                                                Toast.makeText(RequestsFragments.this, "You have Cancelled the Chat request.", Toast.LENGTH_SHORT).show();
                                                                                            }
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                });
                                                    }
                                                });

                                                holder.userName.setText(requestUsername);
                                                holder.userStatus.setText("You have sent a request to " + requestUsername);
                                                holder.itemView.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        new AlertDialog.Builder(RequestsFragments.this, R.style.AlertDialog2)
                                                                .setTitle("Already Sent Request")
                                                                .setNegativeButton("Cancel Chat Request", new DialogInterface.OnClickListener() {
                                                                    @Override
                                                                    public void onClick(DialogInterface dialog, int which) {
                                                                        requestRef.child(currentUserId).child(list_user_id)
                                                                                .removeValue()
                                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                    @Override
                                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                                        if (task.isSuccessful()) {
                                                                                            requestRef.child(list_user_id).child(currentUserId)
                                                                                                    .removeValue()
                                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                        @Override
                                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                                            if (task.isSuccessful()) {
                                                                                                                Toast.makeText(RequestsFragments.this, "You have Cancelled the Chat request.", Toast.LENGTH_SHORT).show();
                                                                                                            }
                                                                                                        }
                                                                                                    });
                                                                                        }
                                                                                    }
                                                                                });
                                                                    }
                                                                })
                                                                .show();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {

                                            }
                                        });

                                    }
                                }
                            }


                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ChatRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                        ChatRequestViewHolder viewHolder = new ChatRequestViewHolder(view);
                        return viewHolder;
                    }
                };

        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatRequestViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;
        Button acceptButton, cancelButton;

        public ChatRequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            acceptButton = itemView.findViewById(R.id.request_accept_button);
            cancelButton = itemView.findViewById(R.id.request_cancel_button);
        }
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
                firebaseAuth.signOut();
                sendUserToPhoneOtpActivity();
                return true;
            default:
                return false;
        }

    }

    private void sendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(RequestsFragments.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(RequestsFragments.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void sendUserToPhoneOtpActivity() {
        Intent phoneOtpIntent = new Intent(RequestsFragments.this, PhoneotpActivity.class);
        phoneOtpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(phoneOtpIntent);
        finish();
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RequestsFragments.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");
        final EditText groupNameField = new EditText(RequestsFragments.this);
        groupNameField.setHint("Enter a name to the group");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)) {
                    groupNameField.requestFocus();
                    groupNameField.setError("Please write a Group Name...");
                    //Toast.makeText(RequestsFragments.this, "Please write a Group Name...", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(RequestsFragments.this, groupName + "group is created successfully...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initializeFields() {
        linearLayout = findViewById(R.id.linearLayout);
        chats = findViewById(R.id.button);
        groups = findViewById(R.id.button2);
        contacts = findViewById(R.id.button3);
        requests = findViewById(R.id.button4);
    }
}
