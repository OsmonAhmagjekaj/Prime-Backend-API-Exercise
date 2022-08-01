package io.exercise.api.mongo.encryptography;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.*;
import java.util.Base64;

public class EncryptionKeysGenerator {

    private KeyPairGenerator keyGen;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public static void main(String[] args) {
        EncryptionKeysGenerator gk;
        try {
            gk = new EncryptionKeysGenerator(1024, "RSA");
            gk.createKeys();
            System.out.println("Public Key:");
            String publicKey = Base64.getEncoder().encodeToString(gk.getPublicKey().getEncoded());
            System.out.println(publicKey);
            System.out.println("Private Key:");
            String privateKey = Base64.getEncoder().encodeToString(gk.getPrivateKey().getEncoded());
            System.out.println(privateKey);
        } catch (NoSuchAlgorithmException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Construct generateKeys: keyPairGenerator with instance of encryptedType name and initialized with keyLength
     *
     * @param keyLength     length of key ex: 1024
     * @param encryptedType the encryption type ex: RSA
     * @throws NoSuchAlgorithmException NoSuchAlgorithmException
     */
    public EncryptionKeysGenerator(int keyLength, String encryptedType) throws NoSuchAlgorithmException {
        this.keyGen = KeyPairGenerator.getInstance(encryptedType);
        this.keyGen.initialize(keyLength);
    }

    /**
     * create generated keys:
     * **Private**
     * **Public**
     */
    public void createKeys() {
        KeyPair pair = this.keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }
    /**
     * Write to file
     *
     * @param path path  privateKey or  publicKey  of a file
     * @param key  keys of privateKey or  publicKey
     * @return true or false but to be honest returned value of the method is never used!
     * @throws IOException IOException
     */
    public boolean writeToFile(String path, byte[] key) throws IOException {
        File file = new File(path);
        boolean write = file.getParentFile().mkdirs();
        FileOutputStream fileOutputStream = new FileOutputStream(file);
        fileOutputStream.write(key);
        fileOutputStream.flush();
        fileOutputStream.close();
        return write;
    }

}
