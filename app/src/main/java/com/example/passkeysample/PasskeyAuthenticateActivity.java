package com.example.passkeysample;

import static com.example.passkeysample.Constants.TIMEOUT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.GetPasswordOption;
import androidx.credentials.GetPublicKeyCredentialOption;
import androidx.credentials.PublicKeyCredential;
import androidx.credentials.exceptions.GetCredentialException;

import android.content.Context;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class PasskeyAuthenticateActivity extends AppCompatActivity {

    private Context context;
    private CredentialManager credentialManager;
    private UserManager userManager;

    private Button continueButton;
    private EditText userNameEditText;

    private TextView resultView;

    private List<User> userList;

    private CancellationSignal cancellationSignal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passkey_authenticate);
        context = this.getApplicationContext();
        credentialManager = CredentialManager.create(this);
        userManager = UserManager.getInstance(context);


        userList = userManager.getUsers();

        userNameEditText = findViewById(R.id.auth_username_edittext);
        continueButton = findViewById(R.id.auth_continue_btn);
        resultView = findViewById(R.id.auth_passkey_result);

        initUI();
    }

    private void initUI() {
        continueButton.setOnClickListener(view -> {
            final String userName = userNameEditText.getText().toString();
            final User user = userManager.getUser(userName);

            cancellationSignal = new CancellationSignal();
            cancellationSignal.setOnCancelListener(() -> System.out.println("SB SB OnCancelListener on cancel"));

            authPasskey(user);
//            Thread t1 = new Thread(() -> {
//                // code goes here.
//                try {
//                    Thread.sleep(10000);
//                    System.out.println("SB SB cancel task");
//
//                    cancellationSignal.cancel();
//
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//
//            });
//            t1.start();
        });
    }

    private void authPasskey(final User user) {
        try {
            GetPasswordOption getPasswordOption = new GetPasswordOption();
            GetPublicKeyCredentialOption getPublicKeyCredentialOption =
                    new GetPublicKeyCredentialOption(getRequestJsonString(user), null);

            GetCredentialRequest getCredRequest = new GetCredentialRequest.Builder()
                    .addCredentialOption(getPasswordOption)
                    .addCredentialOption(getPublicKeyCredentialOption)
                    .build();

            credentialManager.getCredentialAsync(
                    getCredRequest,
                    this,
                    cancellationSignal,
                    Executors.newFixedThreadPool(1),
                    new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                        @Override
                        public void onResult(GetCredentialResponse getCredentialResponse) {
                            System.out.println("SB SB getCredentialResponse");

                            final PublicKeyCredential credential = (PublicKeyCredential) getCredentialResponse.getCredential();
                            final String responseString = credential.getAuthenticationResponseJson();


                            try {
                                final JSONObject responseJson = new JSONObject(responseString);
                                updateResultView(String.format("Successfully get passkey credentials\nuser:%s\n:\nResponse:\n %s",
                                        user == null ? null : user.name,
                                        responseJson.toString(2)));

                                System.out.println(responseJson.toString(2));
                            } catch (final JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(@NonNull GetCredentialException e) {
                            System.out.println("SB SB GetCredentialException");
                            e.printStackTrace();
                            System.out.println("SB SB: " + e.getType());
                            System.out.println("SB SB: " + e.getMessage());

                            updateResultView(String.format("Failed to create passkey\nuser:%s:\nError message: %s\nException:\n%s",
                                    user == null ? null : user.name,
                                    e.getMessage(),
                                    e.toString()));
                        }
                    }
            );

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // https://issuetracker.google.com/issues/265602040
    private String getRequestJsonString(final User user) throws JSONException {
        final JSONObject requestJson = new JSONObject();
        requestJson.put("challenge", UUID.randomUUID().toString());
        requestJson.put("timeout", TIMEOUT);
        requestJson.put("rpId", "liangda-android-play.herokuapp.com");
        requestJson.put("userVerification", "preferred");

        if (user != null) {
            final JSONArray publicKeyCredentialDescriptorJSONArr = new JSONArray();
            final JSONObject publicKeyCredentialDescriptorJSON = new JSONObject();
            publicKeyCredentialDescriptorJSON.put("id", user.credentialId);
            publicKeyCredentialDescriptorJSON.put("type", "public-key");
            final JSONArray transportsArr = new JSONArray();
            transportsArr.put("internal");
            transportsArr.put("hybrid");
            publicKeyCredentialDescriptorJSON.put("transports", transportsArr);
            publicKeyCredentialDescriptorJSONArr.put(publicKeyCredentialDescriptorJSON);
            requestJson.put("allowCredentials", publicKeyCredentialDescriptorJSONArr);
        }


        System.out.println("SB SB auth request json");
        System.out.println(requestJson.toString(2));

        return requestJson.toString();
    }

    private void updateResultView(final String resultStr) {
        runOnUiThread(() -> resultView.setText(resultStr));
    }
}