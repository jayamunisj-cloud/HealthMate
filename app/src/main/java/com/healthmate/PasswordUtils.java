package com.healthmate;

import java.security.SecureRandom;
import android.util.Base64;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;

public class PasswordUtils {

    public static String generateSalt() {

        byte[] salt = new byte[16];

        SecureRandom random = new SecureRandom();
        random.nextBytes(salt);

        return Base64.encodeToString(salt, Base64.NO_WRAP);
    }
    public static String hashPassword(String password, String salt) {

        PBEKeySpec spec = null;

        try {
            byte[] saltBytes = Base64.decode(salt, Base64.NO_WRAP);

            spec = new PBEKeySpec(
                    password.toCharArray(),
                    saltBytes,
                    600000,
                    256
            );

            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            byte[] hash = factory.generateSecret(spec).getEncoded();

            return Base64.encodeToString(hash, Base64.NO_WRAP);

        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);

        } finally {
            if (spec != null) {
                spec.clearPassword();
            }
        }
    }
    public static boolean verifyPassword(
            String enteredPassword,
            String storedSalt,
            String storedHash) {

        String enteredHash = hashPassword(
                enteredPassword,
                storedSalt
        );

        byte[] enteredHashBytes =
                Base64.decode(enteredHash, Base64.NO_WRAP);

        byte[] storedHashBytes =
                Base64.decode(storedHash, Base64.NO_WRAP);

        return MessageDigest.isEqual(
                enteredHashBytes,
                storedHashBytes
        );
    }
}