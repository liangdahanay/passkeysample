package com.example.passkeysample;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class User {
    String name;
    String displayName;
    boolean hasPasskey;
    String userId;
    String credentialId;
}
