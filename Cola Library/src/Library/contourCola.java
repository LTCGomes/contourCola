/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Library;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;

/**
 *
 * @author tjsantos
 */
public class contourCola {

    private Aplicacao aplicacao;
    private Utilizador utilizador;
    private Sistema sistema;
    private GenerateKeys chaves;
    private List<byte[]> list;

    public contourCola(String nomeAplicacao, String versao) throws NoSuchAlgorithmException, IOException, Exception {
        //utilizador = new Utilizador(nomeAplicacao, versao, nomeAplicacao);
        sistema = new Sistema();
        aplicacao = new Aplicacao(nomeAplicacao, versao, "", "");
        chaves = new GenerateKeys(1024);
        if (!new File("keyPair").isDirectory()) {
            new File("keyPair").mkdir();
            if (!new File("keyPair/publicKey.publick").exists() && !new File("keyPair/privateKey.privk").exists()) {
                chaves.createKeys();
                chaves.writeKeysToFile("KeyPair/publicKey.publick", chaves.getPublicKey().getEncoded());
                chaves.writeKeysToFile("KeyPair/privateKey.privk", chaves.getPrivateKey().getEncoded());
            }
        }
    }

    public boolean isRegister() {
        //read licence from file

        //if contourCola information equals licence information -> return true
        return false;
    }

    /**
     * O pedido de licença deve ter: - Dados do sistema cifrados por uma chave
     * simetrica - chave simetrica cifrada pela chave privada assimetrica -
     * assinatura com o certificado do cartão de cidadão - certificado do cartão
     * de cidadão
     */
    public boolean startRegistration() throws Exception {
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

        if (opcao.equals("1")) {
            System.out.println("#-----------------------------------------#");
            System.out.println(" Introduza o seu email:");
            Scanner email = new Scanner(System.in);
            String mail = email.nextLine();

            if (!(mail.equals(""))) {
                list = new ArrayList<byte[]>();
                //buscar informação da contourCola e guarda-la para o ficheiro
                String stringVars = "";
                stringVars += "sistemaMAC/" + sistema.getEnderecoMac() + "/sistemaNumSerie/" + sistema.getNumeroSerie() + "/sistemaUuid/" + sistema.getUuid()
                        + "/appNome/" + aplicacao.getNomeAplicacao() + "/appVersao/" + aplicacao.getVersao();

                byte[] byteVars = stringVars.getBytes();

                //cifrar ficheiro com chave simetrica
                KeyGenerator generator = KeyGenerator.getInstance("DES");
                Key chaveDeCifraSim = generator.generateKey();
                byte[] bytesChaveSimetrica = chaveDeCifraSim.getEncoded();
                Cipher cipher = Cipher.getInstance("DES");
                cipher.init(Cipher.ENCRYPT_MODE, chaveDeCifraSim);
                byte[] bytesVarsCifrados = cipher.doFinal(byteVars);
                System.out.println("bytesVarsCifrados: " + Arrays.toString(bytesVarsCifrados));    //GUARDAR ISTO
                list.add(bytesVarsCifrados);

                //cifrar chave simetrica com a chave assimetrica privada
                PrivateKey contourPriv = chaves.getPrivate("KeyPair/privateKey.privk");
                Cipher cifra = Cipher.getInstance("RSA");
                cifra.init(Cipher.ENCRYPT_MODE, contourPriv);
                byte[] bytesChaveSimCifrada = cifra.doFinal(bytesChaveSimetrica);
                System.out.println("bytesChaveSimCifrada: " + Arrays.toString(bytesChaveSimCifrada));    //GUARDAR ISTO
                list.add(bytesChaveSimCifrada);

                //assinar o array de bytes das variaveis do sistema com o certificado do cartao de cidadao
                Provider[] provs = Security.getProviders();
                KeyStore ks = null;
                try {
                    for (int i = 0; i < provs.length; i++) {
                        if (provs[i].getName().matches("(?i).*SunPKCS11.*")) {
                            ks = KeyStore.getInstance("PKCS11", provs[i].getName());
                        }
                    }
                    ks.load(null, null);
                    Key key = ks.getKey("CITIZEN AUTHENTICATION CERTIFICATE", null);
                    byte[] bytesCertCC = key.getEncoded();
                    System.out.println("key: " + Arrays.toString(bytesCertCC));    //GUARDAR ISTO
                    list.add(bytesCertCC);

                    Signature sig = Signature.getInstance("SHA256withRSA");
                    sig.initSign((PrivateKey) key);
                    sig.update(byteVars);
                    byte[] bytesSig = sig.sign();
                    System.out.println("sigBytes: " + Arrays.toString(bytesSig));    //GUARDAR ISTO
                    list.add(bytesSig);

                    //guardar ficheiro
                    writeToFile("Licence/PedidoDeLicenca.txt");

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
            }
        } else {

            return false;
        }
        System.out.println("Registo não pretendido");
        return false;
    }

    public void showLicenseInfo() {
        //read licence from file

        //print to console
    }

    private void writeToFile(String filename) throws FileNotFoundException, IOException {
        File f = new File(filename);
        f.getParentFile().mkdirs();
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename));
        out.writeObject(list);
        out.close();
    }
}
