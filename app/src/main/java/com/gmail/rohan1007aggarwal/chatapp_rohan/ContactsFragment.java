package com.gmail.rohan1007aggarwal.chatapp_rohan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class ContactsFragment extends AppCompatActivity {

    private Button chats, groups, contacts, requests;
    private RecyclerView myContactsList;

    private String currentUserId;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference rootRef, contactsRef, usersRef;
    private LinearLayout linearLayout;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_fragment);

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        contactsRef = rootRef.child("Contacts").child(currentUserId);
        usersRef = rootRef.child("users");

        toolbar = (Toolbar) findViewById(R.id.contacts_fragment_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatBrewery");

        initializeFields();

        linearLayout.setVisibility(View.GONE);

        myContactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

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
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, ContactsViewHolder> adapter
                = new FirebaseRecyclerAdapter<Contacts, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, final int position, @NonNull final Contacts model) {
                String userIDs = getRef(position).getKey();

                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String visit_user_id = getRef(position).getKey();

                                    Intent profileIntent = new Intent(ContactsFragment.this, ProfileActivity.class);
                                    profileIntent.putExtra("visit_user_id", visit_user_id);
                                    startActivity(profileIntent);
                                }
                            });

                            if (snapshot.child("userState").hasChild("state")) {
                                String state = snapshot.child("userState").child("state").getValue().toString();
                                String date = snapshot.child("userState").child("date").getValue().toString();
                                String time = snapshot.child("userState").child("time").getValue().toString();

                                if (state.equals("online")) {
                                    holder.onlineIcon.setVisibility(View.VISIBLE);
                                }
                                else if (state.equals("offline")) {
                                    holder.onlineIcon.setVisibility(View.INVISIBLE);
                                }
                            }
                            else {
                                holder.onlineIcon.setVisibility(View.INVISIBLE);
                            }

                            if (snapshot.hasChild("imageUrl")) {
                                String profileUsername = snapshot.child("username").getValue().toString();
                                String profileStatus = snapshot.child("status").getValue().toString();
                                String userImage = snapshot.child("imageUrl").getValue().toString();

                                holder.userName.setText(profileUsername);
                                holder.userStatus.setText(profileStatus);
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }
                            else {
                                String profileUsername = snapshot.child("username").getValue().toString();
                                String profileStatus = snapshot.child("status").getValue().toString();

                                holder.userName.setText(profileUsername);
                                holder.userStatus.setText(profileStatus);
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
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout, parent, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        myContactsList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;


        public ContactsViewHolder(@NonNull View itemView) {

            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
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
        Intent findFriendsIntent = new Intent(ContactsFragment.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(ContactsFragment.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void sendUserToPhoneOtpActivity() {
        Intent phoneOtpIntent = new Intent(ContactsFragment.this, PhoneotpActivity.class);
        phoneOtpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(phoneOtpIntent);
        finish();
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ContactsFragment.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");
        final EditText groupNameField = new EditText(ContactsFragment.this);
        groupNameField.setHint("Enter a name to the group");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)) {
                    groupNameField.requestFocus();
                    groupNameField.setError("Please write a Group Name...");
                    //Toast.makeText(ContactsFragment.this, "Please write a Group Name...", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ContactsFragment.this, groupName + "group is created successfully...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initializeFields() {
        chats = findViewById(R.id.button);
        groups = findViewById(R.id.button2);
        contacts = findViewById(R.id.button3);
        requests = findViewById(R.id.button4);
        myContactsList = findViewById(R.id.contacts_list);
        linearLayout = findViewById(R.id.linearLayout);
    }
}
