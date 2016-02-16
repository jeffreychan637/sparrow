package com.tmochida.sparrow;

import android.util.Base64;

import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class for encryption methods.
 */
public class Encryption {
    private static final String initVector = "RandomInitVector";

    public static byte[] encrypt(byte[] clear) throws Exception {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            byte[] key = sha.digest(clear);
            key = Arrays.copyOf(clear, 16); // use only first 128 bit

            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, iv);

            byte[] encrypted = cipher.doFinal(clear);
            System.out.println("encrypted string: "
                    + Base64.encode(encrypted, Base64.DEFAULT));

            return Base64.encode(encrypted, Base64.DEFAULT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static byte[] decrypt(byte[] encrypted, byte[] key) throws Exception {
        try {

            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));

            return original;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static byte[] encryptRSA(byte[] text, Key key) {
        byte[] cipherText = null;
        try {
            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            cipherText = cipher.doFinal(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return cipherText;
    }

    public static byte[] decryptRSA(byte[] text, Key key) {
        byte[] decryptedData = null;
        try {
            final Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            decryptedData = cipher.doFinal(text);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return decryptedData;
    }
}
