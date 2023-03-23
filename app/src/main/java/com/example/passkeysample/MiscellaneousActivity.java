package com.example.passkeysample;

import static androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS;

import androidx.appcompat.app.AppCompatActivity;

import androidx.biometric.BiometricManager;
import android.os.Bundle;
import android.widget.TextView;

public class MiscellaneousActivity extends AppCompatActivity {
    private BiometricManager biometricManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miscellaneous);

        biometricManager = BiometricManager.from(this);

        updateCapability();
    }

    private void updateCapability() {
        final TextView strongBiometricsTextView = findViewById(R.id.strong_biometrics_capability_text);
        strongBiometricsTextView.setText(String.valueOf(BIOMETRIC_SUCCESS == biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)));

        final TextView weakBiometricsTextView = findViewById(R.id.weak_biometrics_capability_text);
        weakBiometricsTextView.setText(String.valueOf(BIOMETRIC_SUCCESS == biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)));

        final TextView deviceCredentialsTextView = findViewById(R.id.device_credentials_text);
        deviceCredentialsTextView.setText(String.valueOf(BIOMETRIC_SUCCESS == biometricManager.canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL)));
    }
}