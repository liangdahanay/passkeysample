package com.example.passkeysample;

import static com.example.passkeysample.Constants.TIMEOUT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.credentials.CreateCredentialResponse;
import androidx.credentials.CreatePublicKeyCredentialRequest;
import androidx.credentials.CreatePublicKeyCredentialResponse;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.exceptions.CreateCredentialException;

import android.content.Context;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;

public class PasskeyEnrollmentActivity extends AppCompatActivity {

    private CredentialManager credentialManager;
    private UserManager userManager;

    private Spinner userSpinner;
    private Button enrollPasskeyButton;

    private TextView responseView;
    private TextView parsedView;

    private Context context;

    private List<User> userList;

    private User currentUser;

    private CancellationSignal cancellationSignal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passkey_enrollment);
        context = this.getApplicationContext();
        userManager = UserManager.getInstance(context);
        credentialManager = CredentialManager.create(this);

        cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                System.out.println("SB SB OnCancelListener on cancel");
            }
        });

        userSpinner = findViewById(R.id.user_spinner);
        enrollPasskeyButton = findViewById(R.id.start_enroll_passkey_btn);
        responseView = findViewById(R.id.enroll_passkey_result);
        parsedView = findViewById(R.id.enroll_passkey_parsed);

        userList = userManager.getUsers();
        if (userList.size() > 0) {
            currentUser = userList.get(0);
        }

        initUI();
    }

    private void initUI() {

        String[] items = new String[userList.size()];
        for (int i = 0; i < userList.size(); i++) {
            final User user = userList.get(i);
            items[i] = user.name;
        }

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, items);

        userSpinner.setAdapter(adapter);
        userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentUser = userList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        enrollPasskeyButton.setOnClickListener((view) -> {
            if (currentUser != null) {
                enrollPasskey(currentUser);
//                Thread t1 = new Thread(() -> {
//                    // code goes here.
//                    try {
//                        Thread.sleep(10000);
//                        System.out.println("SB SB cancel task");
//
//                        cancellationSignal.cancel();
//
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//
//                });
//                t1.start();
            }
        });
    }

    private void enrollPasskey(final User user) {
        try {
            CreatePublicKeyCredentialRequest createPublicKeyCredentialRequest = new CreatePublicKeyCredentialRequest(
                    getRequestJsonString(user),
                    null
            );
            credentialManager.createCredentialAsync(
                    createPublicKeyCredentialRequest,
                    this,
                    cancellationSignal,
                    Executors.newFixedThreadPool(1),
                    new CredentialManagerCallback<CreateCredentialResponse, CreateCredentialException>() {
                        @Override
                        public void onResult(CreateCredentialResponse createCredentialResponse) {
                            try {
                                System.out.println("SB SB createCredentialResponse");

                                final CreatePublicKeyCredentialResponse response = (CreatePublicKeyCredentialResponse) createCredentialResponse;
                                final String responseString = response.getRegistrationResponseJson();
                                final JSONObject responseJson = handleEnrollment(user, responseString);
                                AttestationObject attestationObject = AttestationObject.parseResponse(responseJson);

                                updateResultView(String.format("Successfully create passkey for %s:\nresponse:\n %s",
                                        user.name,
                                        responseJson.toString(2)), attestationObject == null ? null : attestationObject.toString());
                            } catch (final Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(@NonNull CreateCredentialException e) {
                            System.out.println("SB SB CreateCredentialException");
                            e.printStackTrace();

                            updateResultView(String.format("Failed to create passkey for %s:\nError message: %s\nException:\n%s",
                                    user.name,
                                    e.getMessage(),
                                    e.toString()), null);
                        }
                    });
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getRequestJsonString(final User user) throws JSONException {
        final JSONObject requestJson = new JSONObject();

        requestJson.put("challenge", UUID.randomUUID().toString());
        requestJson.put("timeout", TIMEOUT);

        final JSONObject publicKeyCredentialRpEntity = new JSONObject();
        publicKeyCredentialRpEntity.put("id", "liangda-android-play.herokuapp.com");
        publicKeyCredentialRpEntity.put("name", "LiangdaPlay");
        requestJson.put("rp", publicKeyCredentialRpEntity);

        final JSONObject publicKeyCredentialUserEntityJSON = new JSONObject();
        publicKeyCredentialUserEntityJSON.put("id", user.userId);
        publicKeyCredentialUserEntityJSON.put("name", user.name);
        publicKeyCredentialUserEntityJSON.put("displayName", user.displayName);
        requestJson.put("user", publicKeyCredentialUserEntityJSON);

        final JSONArray pubKeyCredParamsArray = new JSONArray();
        final JSONObject publicKeyCredentialParameters = new JSONObject();
        publicKeyCredentialParameters.put("type", "public-key");
        publicKeyCredentialParameters.put("alg", -7);
        pubKeyCredParamsArray.put(publicKeyCredentialParameters);
        requestJson.put("pubKeyCredParams", pubKeyCredParamsArray);

        final JSONObject authenticatorSelectionCriteria = new JSONObject();
        authenticatorSelectionCriteria.put("authenticatorAttachment", "platform");
        authenticatorSelectionCriteria.put("residentKey", "required");

        //authenticatorSelectionCriteria.put("userVerification", "preferred");
        requestJson.put("authenticatorSelection", authenticatorSelectionCriteria);

        // excludeCredentials
        // Don’t re-register any authenticator that has one of these credentials
//        final JSONArray publicKeyCredentialDescriptorJSONArr = new JSONArray();
//        for (final User element: userList) {
//            if (!TextUtils.isEmpty(element.credentialId)) {
//                final JSONObject publicKeyCredentialDescriptorJSON = new JSONObject();
//                publicKeyCredentialDescriptorJSON.put("id", element.credentialId);
//                publicKeyCredentialDescriptorJSON.put("type", "public-key");
//                final JSONArray transportsArr = new JSONArray();
//                transportsArr.put("internal");
//                //transportsArr.put("hybrid");
//                publicKeyCredentialDescriptorJSON.put("transports", transportsArr);
//                publicKeyCredentialDescriptorJSONArr.put(publicKeyCredentialDescriptorJSON);
//            }
//        }
//        if (publicKeyCredentialDescriptorJSONArr.length() > 0) {
//            requestJson.put("excludeCredentials", publicKeyCredentialDescriptorJSONArr);
//        }


        final JSONObject authenticationExtensionsClientInputsJSON = new JSONObject();
        final JSONObject authenticationExtensionsDevicePublicKeyInputs = new JSONObject();
        authenticationExtensionsDevicePublicKeyInputs.put("attestation", "direct");
        final JSONArray attestationFormats = new JSONArray();
        attestationFormats.put("packed");
        attestationFormats.put("tpm");
        attestationFormats.put("android-key");
        attestationFormats.put("android-safetynet");
        attestationFormats.put("fido-u2f");
        attestationFormats.put("apple");
        attestationFormats.put("none");
        authenticationExtensionsDevicePublicKeyInputs.put("attestationFormats", attestationFormats);
        authenticationExtensionsClientInputsJSON.put("devicePubKey", authenticationExtensionsDevicePublicKeyInputs);
        requestJson.put("extensions", authenticationExtensionsClientInputsJSON);


        System.out.println("SB SB enroll request json");
        System.out.println(requestJson.toString(2));
        return requestJson.toString();
    }

    private void updateResultView(final String resultStr, final String parsedResponse) {
        runOnUiThread(() -> {
            responseView.setText(resultStr);
            parsedView.setText(parsedResponse);
        });
    }

    private JSONObject handleEnrollment(final User user, final String responseString) throws JSONException {
        final JSONObject responseJson = new JSONObject(responseString);

        System.out.println("SB SB createCredentialResponse data");
        System.out.println(responseJson.toString(4));

        final String credentialId = responseJson.getString("id");
        user.hasPasskey = true;
        user.credentialId = credentialId;
        userManager.updateUser(user);
        return responseJson;
    }

}