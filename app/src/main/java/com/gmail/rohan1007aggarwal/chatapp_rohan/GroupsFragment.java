package com.gmail.rohan1007aggarwal.chatapp_rohan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class GroupsFragment extends AppCompatActivity {

    private Button chats, groups, contacts, requests;

    private FirebaseAuth firebaseAuth;

    private ListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> listOfGroups = new ArrayList<>();
    private DatabaseReference groupRef;
    private LinearLayout linearLayout;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_fragment);

        firebaseAuth = FirebaseAuth.getInstance();
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        initializeFields();

        retrieveAndDisplayGroups();

        toolbar = (Toolbar) findViewById(R.id.groups_fragment_activity_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("ChatBrewery");

        linearLayout.setVisibility(View.GONE);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String currentGroupName = parent.getItemAtPosition(position).toString();
                Intent groupChatIntent = new Intent(getApplicationContext(), GroupChatActivity.class);
                groupChatIntent.putExtra("groupName", currentGroupName);
                startActivity(groupChatIntent);
            }
        });

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
        Intent findFriendsIntent = new Intent(GroupsFragment.this, FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }

    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(GroupsFragment.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void sendUserToPhoneOtpActivity() {
        Intent phoneOtpIntent = new Intent(GroupsFragment.this, PhoneotpActivity.class);
        phoneOtpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(phoneOtpIntent);
        finish();
    }

    private void requestNewGroup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(GroupsFragment.this, R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");
        final EditText groupNameField = new EditText(GroupsFragment.this);
        groupNameField.setHint("Enter a name to the group");
        builder.setView(groupNameField);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String groupName = groupNameField.getText().toString();

                if (TextUtils.isEmpty(groupName)) {
                    groupNameField.requestFocus();
                    groupNameField.setError("Please write a Group Name...");
                    //Toast.makeText(GroupsFragment.this, "Please write a Group Name...", Toast.LENGTH_SHORT).show();
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
        groupRef.child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(GroupsFragment.this, groupName + "group is created successfully...", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initializeFields() {

        chats = findViewById(R.id.button);
        groups = findViewById(R.id.button2);
        contacts = findViewById(R.id.button3);
        requests = findViewById(R.id.button4);

        listView = findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, listOfGroups);
        listView.setAdapter(arrayAdapter);
        linearLayout = findViewById(R.id.linearLayout);

    }

    private void retrieveAndDisplayGroups() {
        groupRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Set<String> set = new HashSet<>();
                Iterator iterator = snapshot.getChildren().iterator();

                while (iterator.hasNext()){
                    set.add(((DataSnapshot)iterator.next()).getKey());
                }
                listOfGroups.clear();
                listOfGroups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
