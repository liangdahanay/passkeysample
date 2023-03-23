package com.example.passkeysample;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button userButton = findViewById(R.id.manage_user_btn);
        final Button enrollButton = findViewById(R.id.enroll_passkey_btn);
        final Button authButton = findViewById(R.id.auth_passkey_btn);
        final Button miscellaneousButton = findViewById(R.id.miscellaneous_btn);

        userButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, ManageUserActivity.class));
        });

        enrollButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, PasskeyEnrollmentActivity.class));
        });

        authButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, PasskeyAuthenticateActivity.class));
        });

        miscellaneousButton.setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, MiscellaneousActivity.class));
        });
    }
}