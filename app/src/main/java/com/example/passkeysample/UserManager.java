package com.example.passkeysample;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class UserManager {
    private static final String SHARED_PREF_NAME = "passkey_users";

    private static final String KEY_DISPLAY_NAME = "displayName";
    private static final String KEY_HAS_PASSKEY = "hasPasskey";
    private static final String KEY_USER_ID = "userId";

    private static final String KEY_CREDENTIAL_ID = "credentialId";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    private static UserManager userManager;

    public static synchronized UserManager getInstance(final Context context) {
        if (userManager == null) {
            userManager = new UserManager(context);
        }
        return userManager;
    }

    private UserManager(final Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveNewUser(final String name, final String displayName) {
        saveUser(name,
                displayName,
                UUID.randomUUID().toString(),
                false,
                null);
    }

    private void saveUser(final String name,
                          final String displayName,
                          final String userId,
                          final boolean hasPasskey,
                          final String credentialId) {
        final Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put(KEY_DISPLAY_NAME, displayName);
        valuesMap.put(KEY_HAS_PASSKEY, String.valueOf(hasPasskey));
        valuesMap.put(KEY_USER_ID, userId);
        valuesMap.put(KEY_CREDENTIAL_ID, credentialId);

        //convert to string using gson
        final String dbValue = gson.toJson(valuesMap);
        sharedPreferences.edit().putString(name, dbValue).apply();
    }

    public void updateUser(final User user) {
        final Map<String, String> allValue = (Map<String, String>) sharedPreferences.getAll();
        for (final Map.Entry<String, String> entry: allValue.entrySet()) {
            final String name = entry.getKey();
            if (TextUtils.equals(name, user.name)) {
                saveUser(user.name, user.displayName, user.userId, user.hasPasskey, user.credentialId);
            }
        }
    }

    public List<User> getUsers() {
        final List<User> res = new ArrayList<>();
        final Map<String, String> allValue = (Map<String, String>) sharedPreferences.getAll();
        for (final Map.Entry<String, String> entry: allValue.entrySet()) {
            final String name = entry.getKey();
            final String value = entry.getValue();

            final Type type = new TypeToken<HashMap<String, String>>(){}.getType();
            final HashMap<String, String> userData = gson.fromJson(value, type);

            res.add(new User(name,
                    userData.get(KEY_DISPLAY_NAME),
                    Boolean.valueOf(userData.get(KEY_HAS_PASSKEY)),
                    userData.get(KEY_USER_ID),
                    userData.get(KEY_CREDENTIAL_ID)));
        }
        return res;
    }

    public User getUser(final String userName) {
        if (TextUtils.isEmpty(userName)) {
            return null;
        }

        final Map<String, String> allValue = (Map<String, String>) sharedPreferences.getAll();
        for (final Map.Entry<String, String> entry: allValue.entrySet()) {
            final String name = entry.getKey();
            if (TextUtils.equals(name, userName)) {
                final String value = entry.getValue();

                final Type type = new TypeToken<HashMap<String, String>>(){}.getType();
                final HashMap<String, String> userData = gson.fromJson(value, type);
                return new User(name,
                        userData.get(KEY_DISPLAY_NAME),
                        Boolean.valueOf(userData.get(KEY_HAS_PASSKEY)),
                        userData.get(KEY_USER_ID),
                        userData.get(KEY_CREDENTIAL_ID));
            }
        }
        return null;
    }

    public void reset() {
        sharedPreferences.edit().clear().apply();
    }

}
