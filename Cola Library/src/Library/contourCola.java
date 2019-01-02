/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Library;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import pteidlib.PteidException;

/**
 *
 * @author tjsantos
 */
public class contourCola {

    private Aplicacao aplicacao;
    private Sistema sistema;
    private Utilizador utilizador;
    private GenerateKeys chaves;
    private List<byte[]> list;
    private List<byte[]> licenca;

    public contourCola(String nomeAplicacao, String versao) throws NoSuchAlgorithmException, IOException, Exception, PteidException {
        Utilizador.loadPteidLib();
        utilizador = new Utilizador();
        if (utilizador.getData()) {
            utilizador.getNome();
            utilizador.getIdenticacaoCivil();
        } else {
            System.exit(1);
        }
        sistema = new Sistema();
        aplicacao = new Aplicacao(nomeAplicacao, versao, "", "");

        if (!new File("PedidosLicenca").isDirectory()) {
            new File("PedidosLicenca").mkdir();
        }
        if (!new File("PedidosLicenca/Keys").isDirectory()) {
            new File("PedidosLicenca/Keys").mkdir();
        }
        chaves = new GenerateKeys(1024);
        if (!new File("PedidosLicenca/Keys/publicKey.publick").exists() && !new File("PedidosLicenca/Keys/privateKey.privk").exists()) {
            chaves.createKeys();
            chaves.writeKeysToFile("PedidosLicenca/Keys/publicKey.publick", chaves.getPublicKey().getEncoded());
            chaves.writeKeysToFile("PedidosLicenca/Keys/privateKey.privk", chaves.getPrivateKey().getEncoded());

        }
        if (!new File("Licencas").isDirectory()) {
            new File("Licencas").mkdir();

        }
        if (!new File("Licencas/Keys").isDirectory()) {
            new File("Licencas/Keys").mkdir();

        }

    }

    public boolean isRegister() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, FileNotFoundException, ClassNotFoundException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, CertificateException, SignatureException {
        //read licence from file

        System.out.println("#--------------------------------------------#");
        System.out.println("#Certifique-se que tem os ficheiros de       #");
        System.out.println("#pedidos de licença na pasta 'Licenca'       #");
        System.out.println("#e a chave publica respetiva na pasta 'Keys' #");
        System.out.println("#--------------------------------------------#");
        System.out.println("#Qual o ficheiro de licenca?                 #");
        System.out.println("#--------------------------------------------#");
        Scanner scan = new Scanner(System.in);
        String opcao1 = scan.nextLine();
        System.out.println("#--------------------------------------------#");
        System.out.println("#Qual o ficheiro da chave publica do autor?  #");
        System.out.println("#--------------------------------------------#");
        String opcao2 = scan.nextLine();

        byte[] bytesChavePublicaAutor = readFromFile("Licencas/Keys/" + opcao2);
        KeyFactory keyfa = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec xek = new X509EncodedKeySpec(bytesChavePublicaAutor);
        PublicKey chavePublicaAutor = keyfa.generatePublic(xek);

        //buscar array list do pedido de licença
        readListFromFile("Licencas/" + opcao1);

        //buscar certificado
        PublicKey chavePublicaCertificado = getChaveCertificado();
        
        //verificar assinatura        
        if (getVerificacaoAssinatura(chavePublicaCertificado)) {

            //usar chave publica asimetrica para decifrar chave simetrica
            byte[] bytesChaveSimetrica = getSimKey(chavePublicaAutor);

            //usar chave simetrica para decifrar dados do utilizador
            SecretKey chaveDeCifraSim = new SecretKeySpec(bytesChaveSimetrica, "AES");
            byte[] bytesVars = getDadosDecifrados(chaveDeCifraSim);

            System.out.println(new String(bytesVars));
            return true;
        } else {
            //se falso, avisa...

            System.out.println("A assinatura não é válida! A sair do programa.");
        }

        //if contourCola information equals licence information -> return true
        return false;
    }

    /**
     * O pedido de licença deve ter: - Dados do sistema cifrados por uma chave
     * simetrica - chave simetrica cifrada pela chave privada assimetrica -
     * assinatura com o certificado do cartão de cidadão - certificado do cartão
     * de cidadão
     */
    public boolean startRegistration() throws Exception, PteidException {

        System.out.println("#-----------------------------------------#");
        System.out.println(" Introduza o seu email:");

        Scanner email = new Scanner(System.in);
        utilizador.setEmail(email.nextLine());

        if (!(utilizador.getEmail().equals(""))) {
            list = new ArrayList<byte[]>();

            //buscar informação da contourCola e guarda-la para o ficheiro
            System.out.println("#-----------------------------------------#\n");
            String stringVars = "";
            stringVars += " Dados do Utilizador \n Nome:" + utilizador.getNome() + "    Numero de Identificação Civil:" + utilizador.getIdenticacaoCivil() + "    Email:" + utilizador.getEmail()
                    + "\n Dados do Sistema \n Endereço MAC:" + sistema.getEnderecoMac() + "    Número de Série:" + sistema.getNumeroSerie() + "   Identificador Único Universal:" + sistema.getUuid()
                    + "\n Dados da Aplicação \n Nome da aplicação:" + aplicacao.getNomeAplicacao() + "     Versão da Aplicação:" + aplicacao.getVersao();
            System.out.println(stringVars);
            byte[] byteVars = stringVars.getBytes();

            //gerar chave simetrica
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(128);
            Key chaveDeCifraSim = generator.generateKey();
            byte[] bytesChaveSimetrica = chaveDeCifraSim.getEncoded();
            // cifrar o ficheiro
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, chaveDeCifraSim);
            byte[] bytesVarsCifrados = cipher.doFinal(byteVars);
            System.out.println("bytesVarsCifrados: " + Arrays.toString(bytesVarsCifrados));    //GUARDAR ISTO
            list.add(bytesVarsCifrados);

            //cifrar chave simetrica com a chave assimetrica privada
            PrivateKey contourPriv = chaves.getPrivate("PedidosLicenca/Keys/privateKey.privk");
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

                Certificate certificado = ks.getCertificate("CITIZEN AUTHENTICATION CERTIFICATE");
                byte[] bytesCertCC = certificado.getEncoded();
                System.out.println("certificado: " + Arrays.toString(bytesCertCC));    //GUARDAR ISTO
                list.add(bytesCertCC);

                Signature sig = Signature.getInstance("SHA256withRSA");
                sig.initSign((PrivateKey) key);
                sig.update(bytesVarsCifrados);
                byte[] bytesSig = sig.sign();
                System.out.println("assinatura: " + Arrays.toString(bytesSig));    //GUARDAR ISTO
                list.add(bytesSig);

                writeToFile("PedidosLicenca/PedidoDeLicenca.txt");                      //guardar ficheiro

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
            System.out.println("Pedido de licença gerado com sucesso.");
            return true;
        }
        System.out.println("Registo não pretendido");
        return false;
    }

    public void showLicenseInfo() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
        //read licence from file
        System.out.println("#--------------------------------------------#");
        System.out.println("#Certifique-se que tem os ficheiros de       #");
        System.out.println("#pedidos de licença na pasta 'Licenca'       #");
        System.out.println("#e a chave publica respetiva na pasta 'Keys' #");
        System.out.println("#--------------------------------------------#");
        System.out.println("#Qual o ficheiro de licenca?                 #");
        System.out.println("#--------------------------------------------#");
        Scanner scan = new Scanner(System.in);
        String opcao1 = scan.nextLine();
        File fileLicenca = new File("Licencas/Licenca.txt");
        boolean exists = fileLicenca.exists();
        if (fileLicenca.exists() && fileLicenca.isFile()) {
            System.out.println("O ficheiro Licenca existe ");
        } else {
            System.out.println("O ficheiro Licenca não se encontra na pasta 'Licencas/'. \n");
            System.exit(0);
        }
        System.out.println("#--------------------------------------------#");
        System.out.println("#Qual o ficheiro da chave publica do autor?  #");
        System.out.println("#--------------------------------------------#");
        String opcao2 = scan.nextLine();
        File fileKeys = new File("Licencas/Keys/publicKey.publick");
        if (fileKeys.exists() && fileKeys.isFile()) {
            System.out.println("O ficheiro com a chave publica existe ");
        } else {
            System.out.println("O ficheiro com a chave pública não se encontra na pasta 'Licencas/Keys/'. \n");
            System.exit(0);
        }

        //buscar chave publica ao ficheiro
        byte[] bytesChavePublicaUtilizador = readFromFile("Licencas/Keys/" + opcao2);
        KeyFactory keyfa = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec xek = new X509EncodedKeySpec(bytesChavePublicaUtilizador);
        PublicKey chavePublicaUtilizador = keyfa.generatePublic(xek);

        //buscar a chave simetrica a partir da chave publica
        //decifrar os dados com a chave simetrica
        //print to console
    }

    private void writeToFile(String filename) throws FileNotFoundException, IOException {
        File f = new File(filename);
        f.getParentFile().mkdirs();
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
        out.writeObject(list);
        out.close();
    }

    public byte[] readFromFile(String fileName) throws FileNotFoundException, IOException {
        File file = new File(fileName);
        byte[] ba = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(ba);
        fis.close();
        return ba;
    }

    public void readListFromFile(String filename) throws FileNotFoundException, IOException, ClassNotFoundException {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename));
        this.licenca = (List<byte[]>) in.readObject();
        in.close();
        /*bytesVarsCifrados = list.get(0);
        bytesChaveSimCifrada = list.get(1);
        bytesCertCC = list.get(2);
        bytesSig = list.get(3);*/
        System.out.println("============");
        System.out.println("array bytes bytesVarsCifrados: " + Arrays.toString(licenca.get(0)));
        System.out.println("array bytes bytesChaveSimCifrada: " + Arrays.toString(licenca.get(1)));
        System.out.println("array bytes bytesCertCC: " + Arrays.toString(licenca.get(2)));
        System.out.println("array bytes bytesSig: " + Arrays.toString(licenca.get(3)));
        System.out.println("============");
    }
    
    public byte[] getDadosDecifrados(SecretKey chaveDeCifraSim) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, chaveDeCifraSim);
        return cipher.doFinal(licenca.get(0));
    }

    private PublicKey getChaveCertificado() throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream is = new ByteArrayInputStream(licenca.get(2));
        X509Certificate certificado = (X509Certificate) cf.generateCertificate(is);
        return certificado.getPublicKey();
    }

    private boolean getVerificacaoAssinatura(PublicKey chavePublicaCertificado) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(chavePublicaCertificado);
        sig.update(licenca.get(0));
        return sig.verify(licenca.get(3));
    }
    
    public byte[] getSimKey(PublicKey chavePublicaUtilizador) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cifra = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding");
        cifra.init(Cipher.DECRYPT_MODE, chavePublicaUtilizador);
        return cifra.doFinal(licenca.get(1));
    }
}
