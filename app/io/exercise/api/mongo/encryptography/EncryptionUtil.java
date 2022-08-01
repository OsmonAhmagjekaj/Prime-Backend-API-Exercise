package io.exercise.api.mongo.encryptography;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class EncryptionUtil {

    private Cipher cipher;

    /**
     * Construct Encryption with Cipher instance of EncryptedType and EncryptedType
     *
     * @param encryptedType encryptedType ex "RSA"
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     * @throws NoSuchPaddingException   NoSuchPaddingException
     */
    public EncryptionUtil(String encryptedType) throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.cipher = Cipher.getInstance(encryptedType);
    }

    /**
     * Get the PrivateKey
     *
     * @return keyFactory generated private file
     * @throws Exception IoException
     * Document https://docs.oracle.com/javase/8/docs/api/java/security/spec/PKCS8EncodedKeySpec.html
     */
    public PrivateKey getPrivateKeyFromFile(String privateKeyFile, String encryptionType) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(privateKeyFile).toPath());
        return this.getPrivateKeyFromBytes(keyBytes, encryptionType);
    }

    /**
     * Get the PrivateKey
     *
     * @return keyFactory generated private file
     * @throws Exception IoException
     * Document https://docs.oracle.com/javase/8/docs/api/java/security/spec/PKCS8EncodedKeySpec.html
     */
    public PrivateKey getPrivateKeyFromBytes(byte[] privateKey, String encryptionType) throws Exception {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance(encryptionType);
        return keyFactory.generatePrivate(spec);
    }

    /**
     * Get the PublicKey
     *
     * @return keyFactory generated public file
     * @throws Exception Exception
     * Document https://docs.oracle.com/javase/8/docs/api/java/security/spec/X509EncodedKeySpec.html
     * byte[] data1 = "0123456789".getBytes("UTF-8");
     */
    public PublicKey getPublicKeyFromFile(String publicKey, String encryptionType) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(publicKey).toPath());
        return this.getPublicKeyFromBytes(keyBytes, encryptionType);
    }

    /**
     * Get the PublicKey
     *
     * @return keyFactory generated public file
     * @throws Exception Exception
     * Document https://docs.oracle.com/javase/8/docs/api/java/security/spec/X509EncodedKeySpec.html
     * byte[] data1 = "0123456789".getBytes("UTF-8");
     */
    public PublicKey getPublicKeyFromBytes(byte[] publicKey, String encryptionType) throws Exception {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory keyFactory = KeyFactory.getInstance(encryptionType);
        return keyFactory.generatePublic(x509EncodedKeySpec);
    }

    /**
     * Used to encrypt message
     *
     * @param message String message ex: password
     * @param key     keyFactory generated as public key
     * @return String encrypted message
     * @throws InvalidKeyException       invalid key
     * @throws IllegalBlockSizeException illegalException
     * @throws BadPaddingException       BadPaddingException
     */
    public String encryptText(String message, PublicKey key) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        this.cipher.init(Cipher.ENCRYPT_MODE, key);
        return Base64.getMimeEncoder().encodeToString(cipher.doFinal(message.getBytes(StandardCharsets.UTF_8)));
    }

    /**
     * Used to decrypt message
     *
     * @param message String message ex: password
     * @param key     keyFactory generated as private key
     * @return String decrypted message
     * @throws InvalidKeyException       invalid key
     * @throws IllegalBlockSizeException illegalException
     * @throws BadPaddingException       BadPaddingException
     */
    public String decryptText(String message, PrivateKey key) throws InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        this.cipher.init(Cipher.DECRYPT_MODE, key);
        return new String(cipher.doFinal(Base64.getMimeDecoder().decode(message)), StandardCharsets.UTF_8);
    }

}
