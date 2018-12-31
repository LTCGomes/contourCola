/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cola.management;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 *
 * @author Luis
 */
public class GenerateKeys {
    private KeyPairGenerator genChave;
    private KeyPair pare;
    private PrivateKey privada;
    private PublicKey publica;

    public GenerateKeys(int comprimento) throws NoSuchAlgorithmException {
        this.genChave = KeyPairGenerator.getInstance("RSA");
        this.genChave.initialize(comprimento);
    }
    
    public void createKeys() {
        this.pare = this.genChave.generateKeyPair();
        this.privada = pare.getPrivate();
        this.publica = pare.getPublic();
    }
    
    public PrivateKey getPrivateKey() {
        return this.privada;
    }

    public PublicKey getPublicKey() {
        return this.publica;
    }

    public void writeKeysToFile(String fileName, byte[] text) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(new File(fileName), false);
        fos.write(text);
        fos.close();
    }
    
    public PrivateKey getPrivate(String filename) throws Exception {
        FileInputStream fis = new FileInputStream(filename);
        byte[] keyBytes = new byte[fis.available()];
        fis.read(keyBytes);
        fis.close();
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    public PublicKey getPublic(String filename) throws Exception {
        FileInputStream fis = new FileInputStream(filename);
        byte[] keyBytes = new byte[fis.available()];
        fis.read(keyBytes);
        fis.close();
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }
}
