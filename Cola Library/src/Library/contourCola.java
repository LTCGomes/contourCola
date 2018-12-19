/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Library;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tjsantos
 */
public class contourCola {

    private Aplicacao aplicacao;
    private Utilizador utilizador;
    private Sistema sistema;
    private GenerateKeys chaves;



    public contourCola(String nomeAplicacao, String versao) throws NoSuchAlgorithmException, IOException, Exception {
        //utilizador = new Utilizador(nomeAplicacao, versao, nomeAplicacao);
        sistema = new Sistema();
        aplicacao = new Aplicacao(nomeAplicacao, versao);
        chaves = new GenerateKeys(1024);
        if (!new File("keyPair").isDirectory()) {
            new File("keyPair").mkdir();
            if (!new File("keyPair/publicKey.publick").exists() && !new File("keyPair/privateKey.privk").exists()) {
                chaves.createKeys();
                chaves.writeToFile("KeyPair/publicKey.publick", chaves.getPublicKey().getEncoded());
                chaves.writeToFile("KeyPair/privateKey.privk", chaves.getPrivateKey().getEncoded());
            }
        } 
    }
    
    public boolean isRegister() {
        //read licence from file
        
        //if contourCola information equals licence information -> return true
        return false;
    }
    
    public boolean startRegistration() {
        //apresentar opções de inicio de registo de aplicacao
        System.out.println("#-----------------------------------------#");
        System.out.println("#Esta aplicação não se encontra registada.#");
        System.out.println("#Pretende iniciar o seu registo?          #");
        System.out.println("#-----------------------------------------#");
        System.out.println("1 - Sim");
        System.out.println("2 - Não (Ou qualquer outra opção)");
        System.out.println("#-----------------------------------------#");
        Scanner scan = new Scanner(System.in);
        String opcao = scan.nextLine();
        
        if(opcao.equals("1")) {
            //buscar informação da contourCola e guarda-la para o ficheiro
            String file = "";
            file += "sistemaMAC/"+sistema.getEnderecoMac()+"/sistemaNumSerie/"+sistema.getNumeroSerie()+"/sistemaUuid/"+sistema.getUuid()+
                    "/appNome/"+aplicacao.getNomeAplicacao()+"/appVersao/"+aplicacao.getVersao();

            byte[] vars = file.getBytes();
            
            System.out.println(vars);
            //assinar o ficheiro criado anteriormente com a chave do cartão de cidadão do utilizador
            Provider[] provs = Security.getProviders();
            KeyStore ks = null;
            try {
                for (int i = 0; i < provs.length; i++) {
                    if (provs[i].getName().matches("(?i).*SunPKCS11.*")) {
                        ks = KeyStore.getInstance("PKCS11", provs[i].getName());
                    }
                }
                ks.load( null, null );
                Key key = ks.getKey("CITIZEN AUTHENTICATION CERTIFICATE", null);
                
                Signature sig = Signature.getInstance("SHA256withRSA");
                sig.initSign((PrivateKey) key);
                sig.update(vars);
                byte[] sigBytes = sig.sign();
                //System.out.println("Singature (Encoded): " + new BASE64Encoder().encode(sigBytes));
                
                //criar pare de chaves assimetricas e cifrar o sigBytes
                
                //guardar ficheiros e chaves
            } catch (KeyStoreException ex) {
                Logger.getLogger(contourCola.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(contourCola.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(contourCola.class.getName()).log(Level.SEVERE, null, ex);
            } catch (CertificateException ex) {
                Logger.getLogger(contourCola.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnrecoverableKeyException ex) {
                Logger.getLogger(contourCola.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvalidKeyException ex) {
                Logger.getLogger(contourCola.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SignatureException ex) {
                Logger.getLogger(contourCola.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchProviderException ex) {
                Logger.getLogger(contourCola.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //cifrar assinatura e o ficheiro
            
            System.out.println("Encontre o ficheiro gerado na raiz do seu programa");
            return true;
        } else {
            System.out.println("Registo não pretendido");
            return false;
        }
    }
    
    public void showLicenseInfo() {
        //read licence from file
        
        //print to console
    }
}
