package com.jobportal.service;

import com.jobportal.config.AppProps;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.*;
import org.springframework.stereotype.Service;

/** AES-256-GCM encryption for stored secrets. Key is derived (SHA-256) from APP_ENCRYPTION_KEY. */
@Service
public class CryptoService {
    private final SecretKeySpec key;
    private final SecureRandom random = new SecureRandom();

    public CryptoService(AppProps props) {
        if (props.encryptionKey() == null || props.encryptionKey().isBlank())
            throw new IllegalStateException("APP_ENCRYPTION_KEY must be set");
        try {
            key = new SecretKeySpec(MessageDigest.getInstance("SHA-256")
                    .digest(props.encryptionKey().getBytes(StandardCharsets.UTF_8)), "AES");
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }

    public String encrypt(String plain) {
        try {
            byte[] iv = new byte[12];
            random.nextBytes(iv);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] ct = c.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return Base64.getEncoder().encodeToString(out);
        } catch (GeneralSecurityException e) { throw new IllegalStateException("Encryption failed", e); }
    }

    public String decrypt(String encoded) {
        try {
            byte[] all = Base64.getDecoder().decode(encoded);
            Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
            c.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, all, 0, 12));
            return new String(c.doFinal(all, 12, all.length - 12), StandardCharsets.UTF_8);
        } catch (GeneralSecurityException e) { throw new IllegalStateException("Decryption failed (wrong APP_ENCRYPTION_KEY?)", e); }
    }
}
