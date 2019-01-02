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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
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
import javax.xml.bind.DatatypeConverter;
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
        aplicacao = new Aplicacao(nomeAplicacao, versao);
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

    public boolean isRegister() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, FileNotFoundException, ClassNotFoundException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, CertificateException, SignatureException, Exception {
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

        
        //buscar chave publica ao ficheiro
        PublicKey chavePublicaAutor = chaves.getPublic("Licencas/Keys/" + opcao2);

        //buscar array list da licença
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
            
            //Para testes por causa do erro ao decifrar!!!
            //byte[] bytesVars = readFromFile("Licencas/teste.txt");
            
            //verificar dados com os do sistema
            String[] dados = new String(bytesVars).split("\n");

            //Dados Utilizador
            if (!dados[2].contains(utilizador.getNome()) && !dados[3].contains(""+utilizador.getIdenticacaoCivil())) {
                System.out.println("Licenca Inválida: Utilizador diferente para o qual a licença foi imprimida");
                return false;
            }

            //Dados Sistema
            if (!dados[7].contains(sistema.getEnderecoMac()) && !dados[8].contains(sistema.getNumeroSerie()) && !dados[9].contains(sistema.getUuid())) {
                System.out.println("Licenca Inválida: Sistema diferente para o qual a licença foi imprimida");
                return false;
            }

            //Dados Aplicacao
            if (!dados[14].contains(aplicacao.getHashAplicacao())) {
                System.out.println("Licenca Inválida: Aplicação diferente para o qual a licença foi imprimida");
                return false;
            }

            //Validade
            String[] validadeLicenca = dados[17].split(":");
            validadeLicenca[1] = validadeLicenca[1].substring(0, validadeLicenca[1].indexOf(" "));
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            Date current = new Date();
            Date from = formatter.parse(validadeLicenca[1]);
            Date to = formatter.parse(validadeLicenca[2]);
            if (current.after(from) && current.before(to)) {
                System.out.println("Licença Válida - A iniciar programa");
                return true;
            }
        } else {
            //se falso, avisa...

            System.out.println("A assinatura não é válida! A sair do programa.");
        }
        
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
            stringVars += "Dados do Utilizador\n\nNome:" + utilizador.getNome() + "\nNumero de Identificação Civil:" + utilizador.getIdenticacaoCivil() + "\nEmail:" + utilizador.getEmail()
                    + "\nDados do Sistema\n\nEndereço MAC:" + sistema.getEnderecoMac() + "\nNúmero de Série:" + sistema.getNumeroSerie() + "\nIdentificador Único Universal:" + sistema.getUuid()
                    + "\nDados da Aplicação\n\nNome da aplicação:" + aplicacao.getNomeAplicacao() + "\nVersão da Aplicação:" + aplicacao.getVersao() + "\nHash da Aplicação:" + aplicacao.getHashAplicacao() + "\nHash da Biblioteca:" + aplicacao.getHashBiblioteca();
            
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
        
    // metodo para ir buscar a chave Simetrica a partir da chave pública
    public byte[] getSimKey(PublicKey chavePublicaUtilizador) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cifra = Cipher.getInstance("RSA");
        cifra.init(Cipher.DECRYPT_MODE, chavePublicaUtilizador);
        return cifra.doFinal(licenca.get(1));
    }
    
    //Metodo para retornar os dados do sistema
    public byte[] getDadosDecifrados(SecretKey chaveDeCifraSim) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, chaveDeCifraSim);
        return cipher.doFinal(licenca.get(0));
    }
    
    //Método para ir buscar o certificado
    public PublicKey getChaveCertificado() throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream is = new ByteArrayInputStream(licenca.get(2));
        X509Certificate certificado = (X509Certificate) cf.generateCertificate(is);
        return certificado.getPublicKey();
    }
    //Metodo para Verificar se a assinatura é válida
    public boolean getVerificacaoAssinatura(PublicKey chavePublicaCertificado) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(chavePublicaCertificado);
        sig.update(licenca.get(0));
        return sig.verify(licenca.get(3));
    }

    public void showLicenseInfo() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, ClassNotFoundException, CertificateException, InvalidKeyException, SignatureException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, BadPaddingException, BadPaddingException, Exception {
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
        if (fileLicenca.exists() && fileLicenca.isFile()) {
            System.out.println("#--------------------------------------------#");
            System.out.println("#Qual o ficheiro da chave publica do autor?  #");
            System.out.println("#--------------------------------------------#");
            String opcao2 = scan.nextLine();
            File fileKeys = new File("Licencas/Keys/publicKey.publick");
            if (fileKeys.exists() && fileKeys.isFile()) {
                //buscar chave publica ao ficheiro
                PublicKey chavePublicaUtilizador = chaves.getPublic("Licencas/Keys/" + opcao2);

                //buscar array list do pedido de licença
                readListFromFile("Licencas/" + opcao1);

                //buscar certificado
                PublicKey chavePublicaCertificado = getChaveCertificado();
                //Verificar se o certificado é válido
                if (getVerificacaoAssinatura(chavePublicaCertificado)) {
                    //usar chave publica asimetrica para decifrar chave simetrica
                    byte[] bytesChaveSimetrica = getSimKey(chavePublicaUtilizador);

                    //usar chave simetrica para decifrar dados do utilizador
                    SecretKey chaveDeCifraSim = new SecretKeySpec(bytesChaveSimetrica, "AES");
                    byte[] bytesVars = getDadosDecifrados(chaveDeCifraSim);
                    
                    String dados = new String(bytesVars);
                    //imprimir para a consola
                    System.out.println("========================================================");
                    System.out.println("Ficheiro Licença\n" + dados);
                    System.out.println("========================================================");
                } else {
                    //se falso, avisa...
                    System.out.println("A assinatura não é válida! A sair do programa.");
                }

            } else {
                System.out.println("A chave pública que introduziu não é válida. Tente novamente. \n");
            }
        } else {
            System.out.println("O ficheiro Licença que introduziu não é válido ou não possuí um ficheiro de Licença. Tente novamente \n");
        }

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
    }
}
