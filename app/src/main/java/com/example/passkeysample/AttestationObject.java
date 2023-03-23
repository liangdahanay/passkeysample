package com.example.passkeysample;

import com.google.iot.cbor.CborMap;

import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Base64;

import lombok.AllArgsConstructor;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class AttestationObject {
    public String fmt;
    public String attStmt;
    //public String credentialId;

    public static AttestationObject parseResponse(final JSONObject responseJson) {
        try {
            final JSONObject response = responseJson.getJSONObject("response");
            String attestationObject = response.getString("attestationObject");

            //String attestationObject = "o2NmbXRkbm9uZWdhdHRTdG10oGhhdXRoRGF0YViUT8A7K8HQShWA5GPBwKrraiQUpCLY_1OrWgkNQQlT7GNdAAAAAAAAAAAAAAAAAAAAAAAAAAAAEPVWCCxRvLxpc-pG82pS9NqlAQIDJiABIVggtaU622h9RFbYGchmsHOH3lS0S-AiSfnrmqg_A83HsoIiWCDD8Fa_cgXYt-UOwcnauGux3AIkqj-unolsHB8oNgfoeA";
            attestationObject = attestationObject
                    .replace('-', '+')
                    .replace('_', '/') + "==".substring(0, (3 * attestationObject.length()) % 4);
            final byte[] attestationObjectArr = Base64.getDecoder().decode(attestationObject);

            CborMap cborMap = CborMap.createFromCborByteArray(attestationObjectArr);

            String fmt = cborMap.get("fmt").toJsonString();
            String attStmt = cborMap.get("attStmt").toJsonString();
            byte[] authDataByte = cborMap.get("authData").toCborByteArray();

            System.out.println("SB SB parseResponse");
            System.out.println("SB SB fmt: " + fmt);
            System.out.println("SB SB attStmt: " + attStmt);
            System.out.println(authDataByte.length); // 150

            final byte[] idLenArr = new byte[2];
            System.arraycopy(authDataByte, 53, idLenArr, 0, 2);
            short idLen = ByteBuffer.wrap(idLenArr).getShort();
            System.out.println(authDataByte[53]);
            System.out.println(authDataByte[54]);
            System.out.println(idLenArr[0]);
            System.out.println(idLenArr[1]);
            System.out.println("SB SB id length: " + idLen);

            return new AttestationObject(fmt, attStmt);
        }  catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
