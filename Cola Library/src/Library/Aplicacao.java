/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Library;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author tjsantos
 */
class Aplicacao {
    private final String nomeAplicacao;
    private final String versao;
    private final String hashAplicacao;
    private final String hashBiblioteca;
    
    public Aplicacao(String nomeAplicacao, String versao) throws IOException, NoSuchAlgorithmException {
        this.nomeAplicacao = nomeAplicacao;
        this.versao = versao;
        this.hashAplicacao = generateHash("src/calculator/application/CalculatorApplication.java");
        this.hashBiblioteca = generateHash("dist/lib/Cola_Library.jar");
    }

    public String getNomeAplicacao() {
        return nomeAplicacao;
    }

    public String getVersao() {
        return versao;
    }

    public String getHashAplicacao() {
        return hashAplicacao;
    }

    public String getHashBiblioteca() {
        return hashBiblioteca;
    }

    public final String generateHash(String path) throws IOException, NoSuchAlgorithmException {
        FileInputStream fis = new FileInputStream(path);
        byte[] b = new byte[fis.available()];
        fis.read(b);
        fis.close();
        
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(b);
        byte[] hash = md.digest();
        
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< hash.length ;i++)
        {
            sb.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }    
}
