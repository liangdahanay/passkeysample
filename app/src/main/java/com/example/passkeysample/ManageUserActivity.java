package com.example.passkeysample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;
import java.util.UUID;

public class ManageUserActivity extends AppCompatActivity {

    private UserManager userManager;
    private Context context;

    private Button createUserButton;
    private Button resetUserButton;
    private EditText nameEditText;
    private EditText displayNameText;
    private ListView userListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_user);
        context = this.getApplicationContext();
        userManager = UserManager.getInstance(context);
        createUserButton = findViewById(R.id.create_user_btn);
        resetUserButton = findViewById(R.id.reset_user_btn);
        nameEditText = findViewById(R.id.user_name_edittext);
        displayNameText = findViewById(R.id.user_displayname_edittext);
        userListView = findViewById(R.id.user_list);

        initListener();
        refreshUserList();
    }

    private void initListener() {
        createUserButton.setOnClickListener(view -> {
            final String name = nameEditText.getText().toString();
            final String displayName = displayNameText.getText().toString();

            if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(displayName)) {
                userManager.saveNewUser(name, displayName);
                refreshUserList();
            } else {
                Toast.makeText(context, "Please input name and display name", Toast.LENGTH_LONG).show();
            }
        });

        resetUserButton.setOnClickListener(view -> {
            userManager.reset();
            refreshUserList();
        });
    }


    private void refreshUserList() {
        final List<User> userList = userManager.getUsers();
        System.out.println("SB SB users: " + userList.size());

        final String[] userInfoArr = new String[userList.size()];

        for (int i = 0; i < userList.size(); i++) {
            final User user = userList.get(i);
            if (TextUtils.isEmpty(user.credentialId)) {
                userInfoArr[i] = String.format("Name: %s; DisplayName: %s, Passkeys enrolled : %s",
                        user.name,
                        user.displayName,
                        user.hasPasskey);
            } else {
                userInfoArr[i] = String.format("Name: %s; DisplayName: %s, Passkeys enrolled : %s, credentialId: %s",
                        user.name,
                        user.displayName,
                        user.hasPasskey,
                        user.credentialId);
            }

        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.user_list_layout, userInfoArr);
        userListView.setAdapter(adapter);
    }

}