/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cola.management;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author tjsantos
 */
/**
 * A licença deve ter: - certificado - assinatura do hash dos dados cifrados -
 * chaves simetricas cifradas com chave assimetrica - validade cifrada com chave
 * simetrica - hash de dados cifrados com chave simetrica
 */
public class ColaManagement {

    private List<byte[]> list;
    private List<byte[]> licenca;
    private byte[] bytesVarsCifrados;
    private byte[] bytesChaveSimCifrada;
    private byte[] bytesCertCC;
    private byte[] bytesSig;
    private GenerateKeys chaves;

    /**
     * @param args the command line arguments
     */
    public ColaManagement() throws IOException, Exception {
                
        if (!new File("PedidosLicenca").isDirectory()) {
            new File("PedidosLicenca").mkdir();
        }
        if (!new File("PedidosLicenca/Keys").isDirectory()) {
            new File("PedidosLicenca/Keys").mkdir();
        }
        if (!new File("Licencas").isDirectory()) {
            new File("Licencas").mkdir();
        }
        if (!new File("Licencas/Keys").isDirectory()) {
            new File("Licencas/Keys").mkdir();
        }
        chaves = new GenerateKeys(1024);
        if (!new File("Licencas/Keys/publicKey.publick").exists() && !new File("Licencas/Keys/privateKey.privk").exists()) {
            chaves.createKeys();
            chaves.writeKeysToFile("Licencas/Keys/publicKey.publick", chaves.getPublicKey().getEncoded());
            chaves.writeKeysToFile("Licencas/Keys/privateKey.privk", chaves.getPrivateKey().getEncoded());
        }

        if (!new File("Licencas/BD").isDirectory()) {
            new File("Licencas/BD").mkdir();
            
            File u = new File("Licencas/BD/utilizadoresRegistados.txt");
            u.getParentFile().mkdirs(); 
            u.createNewFile();
            
            byte[] usersToSave = generateAndSaveSimKey("\n".getBytes(), "usersKey.simKey");
            FileOutputStream fosUsers = new FileOutputStream(new File("Licencas/BD/utilizadoresRegistados.txt"));
            fosUsers.write(usersToSave);
            fosUsers.close();
            
            File s = new File("Licencas/BD/sistemasRegistados.txt");
            s.getParentFile().mkdirs(); 
            s.createNewFile();
            
            byte[] sistemasToSave = generateAndSaveSimKey("\n".getBytes(), "sistemasKey.simKey");
            FileOutputStream fosSistemas = new FileOutputStream(new File("Licencas/BD/sistemasRegistados.txt"), false);
            fosSistemas.write(sistemasToSave);
            fosSistemas.close();
        }
        
    }

    private void writeToFile(String filename) throws FileNotFoundException, IOException {
        File f = new File(filename);
        f.getParentFile().mkdirs();
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(f));
        out.writeObject(list);
        out.close();
    }

    public void generateLicence(byte[] bytesVars, String filePublicKey) throws NoSuchAlgorithmException, Exception {

        licenca = new ArrayList<byte[]>();

        //criar o intervalo de tempo e guarda-lo
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Date dataFrom = new Date();
        Date dataTo = new Date(dataFrom.getTime() + 365 * 86400000l);

        String dados = new String(bytesVars);
        dados += "\n\nLicença válida de:" + df.format(dataFrom) + " até:" + df.format(dataTo);
        System.out.println("========================================================");
        System.out.println("Ficheiro Licença\n " + dados);
        System.out.println("========================================================");
        byte[] byteVars = dados.getBytes();
        
        //Para testes por causa do erro ao decifrar!!!
        //chaves.writeKeysToFile("teste.txt", byteVars);

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

        //cifrar chave simetrica com a chave assimetrica publica do utilizador
        PublicKey utilizadorPublica = chaves.getPublic("PedidosLicenca/Keys/"+filePublicKey);
        Cipher cifra = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cifra.init(Cipher.ENCRYPT_MODE, utilizadorPublica);
        byte[] bytesChaveSimCifrada = cifra.doFinal(bytesChaveSimetrica);
        System.out.println("bytesChaveSimCifrada: " + Arrays.toString(bytesChaveSimCifrada));    //GUARDAR ISTO
        list.add(bytesChaveSimCifrada);

        //assinar o array de bytes das variaveis do sistema com o certificado do cartao de cidadao
        Provider[] provs = Security.getProviders();
        KeyStore ks = null;
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
        
        writeToFile("Licencas/Licenca_"+dataFrom.getTime()+".txt");
        File fileLicenca = new File("Licencas/Licenca_"+dataFrom.getTime()+".txt");
        if (fileLicenca.exists() && fileLicenca.isFile()) {
            System.out.println("A licença foi criada com sucesso.");
        }
    }

    public PublicKey getPublic(String filename) throws Exception {
        byte[] keyBytes = Files.readAllBytes(new File(filename).toPath());
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
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
        this.list = (List<byte[]>) in.readObject();
        in.close();
        bytesVarsCifrados = list.get(0);
        bytesChaveSimCifrada = list.get(1);
        bytesCertCC = list.get(2);
        bytesSig = list.get(3);

    }

    public byte[] getSimKey(PrivateKey chavePrivAutor) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cifra = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cifra.init(Cipher.DECRYPT_MODE, chavePrivAutor);
        return cifra.doFinal(list.get(1));
    }

    public byte[] getDadosDecifrados(SecretKey chaveDeCifraSim) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, chaveDeCifraSim);
        return cipher.doFinal(list.get(0));
    }

    private PublicKey getChaveCertificado() throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream is = new ByteArrayInputStream(list.get(2));
        X509Certificate certificado = (X509Certificate) cf.generateCertificate(is);
        return certificado.getPublicKey();
    }

    private boolean getVerificacaoAssinatura(PublicKey chavePublicaCertificado) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(chavePublicaCertificado);
        sig.update(list.get(0));
        return sig.verify(list.get(3));
    }

    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, FileNotFoundException, ClassNotFoundException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, CertificateException, SignatureException, Exception {
        // TODO code application logic here

        ColaManagement autor = new ColaManagement();
        
        //INSERT MENU HERE
        String opcao = "";
        while (!opcao.equals("1") && !opcao.equals("2")) {
            //INSERT MENU HERE
            System.out.println("#------------------------------------------------#");
            System.out.println("#Prentede gerar um ficheiro de licença?          #");
            System.out.println("#1 - Sim                                         #");
            System.out.println("#2 - Não                                         #");
            System.out.println("#------------------------------------------------#");

            Scanner scan = new Scanner(System.in);
            opcao = scan.nextLine();
        }

        if (opcao.equals("1")) {

            System.out.println("#-----------------------------------------------#");
            System.out.println("#Certifique-se que tem os ficheiros de          #");
            System.out.println("#pedidos de licença na pasta 'PedidosLicenca'   #");
            System.out.println("#e a chave publica respetiva na pasta 'Keys'    #");
            System.out.println("#-----------------------------------------------#");
            System.out.println("#Qual o ficheiro de pedido de licença?          #");
            System.out.println("#-----------------------------------------------#");
            Scanner scan = new Scanner(System.in);
            String opcao1 = scan.nextLine();
            File fileLicenca = new File("PedidosLicenca/PedidoDeLicenca.txt");
            if (fileLicenca.exists() && fileLicenca.isFile()) {
                System.out.println("#-----------------------------------------------#");
                System.out.println("#Qual o ficheiro da chave publica do utilizador?#");
                System.out.println("#-----------------------------------------------#");
                String opcao2 = scan.nextLine();
                File fileKeys = new File("PedidosLicenca/Keys/publicKey.publick");
                if (fileKeys.exists() && fileKeys.isFile()) {

                    //buscar chave publica ao ficheiro
                    byte[] bytesChavePrivAutor = autor.readFromFile("Licencas/Keys/privateKey.privk");
                    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytesChavePrivAutor);
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    PrivateKey chavePrivAutor = kf.generatePrivate(spec);

                    //buscar array list do pedido de licença
                    autor.readListFromFile("PedidosLicenca/" + opcao1);

                    //buscar certificado
                    PublicKey chavePublicaCertificado = autor.getChaveCertificado();
                    if (autor.getVerificacaoAssinatura(chavePublicaCertificado)) {
                        //Se verdadeiro, continua e vai gerar a licença

                        //usar chave privada asimetrica para decifrar chave simetrica
                        byte[] bytesChaveSimetrica = autor.getSimKey(chavePrivAutor);

                        //usar chave simetrica para decifrar dados do utilizador
                        SecretKey chaveDeCifraSim = new SecretKeySpec(bytesChaveSimetrica, "AES");
                        byte[] bytesVars = autor.getDadosDecifrados(chaveDeCifraSim);
                        
                        byte[] bytesUsersKey = autor.readFromFile("Licencas/BD/usersKey.simKey");
                        byte[] users = autor.readFromFile("Licencas/BD/utilizadoresRegistados.txt");
                        String stringUsers = autor.getStringDeFicheiroBD(bytesUsersKey, chavePrivAutor, users);
                        
                        byte[] bytesSistemasKey = autor.readFromFile("Licencas/BD/sistemasKey.simKey");
                        byte[] sistemas = autor.readFromFile("Licencas/BD/sistemasRegistados.txt");
                        String stringSistemas =  autor.getStringDeFicheiroBD(bytesSistemasKey, chavePrivAutor, sistemas);
                        
                        if (!stringUsers.contains(autor.getVariavel("Numero de Identificação Civil", bytesVars)) && !stringSistemas.contains(autor.getVariavel("Identificador Único Universal", bytesVars))) {
                            autor.generateLicence(bytesVars, opcao2);
                            
                            //guardar novas variaveis na BD
                            stringUsers += autor.getVariavel("Numero de Identificação Civil", bytesVars) + "\n";                          
                            stringSistemas += autor.getVariavel("Identificador Único Universal", bytesVars) + "\n";
                            
                            byte[] usersToSave = autor.generateAndSaveSimKey(stringUsers.getBytes(), "usersKey.simKey");
                            FileOutputStream fosUsers = new FileOutputStream("Licencas/BD/utilizadoresRegistados.txt");
                            fosUsers.write(usersToSave);
                            fosUsers.close();
                            byte[] sistemasToSave = autor.generateAndSaveSimKey(stringSistemas.getBytes(), "sistemasKey.simKey");
                            FileOutputStream fosSistemas = new FileOutputStream("Licencas/BD/sistemasRegistados.txt");
                            fosSistemas.write(sistemasToSave);
                            fosSistemas.close();
                        } else {
                            System.out.println("Utilizador ou Sistema já estão presentes na nossa base de dados, não é possível imprimir outra licença");
                        }
                    } else {
                        //se falso, avisa...

                        System.out.println("A assinatura não é válida! A sair do programa.");
                    }
                } else {
                    System.out.println("A chave pública do utilizador que introduziu não é válida ou não se encontra na diretoria certa. Tente novamente. \n");
                }
            } else {
                System.out.println("O pedido de licenca que introduziu não é válida ou não se encontra na diretoria certa. Tente novamente. \n");
            }
            /**
             * - Dados do sistema cifrados por uma chave simetrica - chave
             * simetrica cifrada pela chave privada assimetrica - assinatura com
             * o certificado do cartão de cidadão - certificado do cartão de
             * cidadão
             */

            //Buscar certificado do CC e verificar se não há mais nenhuma licenca para este utilizador
        } else {
            System.out.println("A sair do programa.");
            System.exit(0);
        }
    }

    public String getVariavel(String variavel, byte[] bytesVars) {
        String[] dados = new String(bytesVars).split(variavel+":");
        dados[1] = dados[1].substring(0, dados[1].indexOf("\n"));
        return dados[1];
    }
    
    private byte[] generateAndSaveSimKey(byte[] ficheiroParaCifrar, String name) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, Exception {
        //gerar chave simetrica
        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(128);
        Key chaveSim = generator.generateKey();
        byte[] bytesChaveSim = chaveSim.getEncoded();
        // cifrar o ficheiro
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, chaveSim);
        byte[] bytesFicheiroCifrado = cipher.doFinal(ficheiroParaCifrar);
        
        //cifrar chave simetrica com a chave assimetrica privada
        PublicKey colaPublic = chaves.getPublic("Licencas/Keys/publicKey.publick");
        Cipher cifra = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cifra.init(Cipher.ENCRYPT_MODE, colaPublic);
        byte[] bytesChavesimetricaCifrada = cifra.doFinal(bytesChaveSim);
                
        FileOutputStream fos = new FileOutputStream(new File("Licencas/BD/"+name), false);
        fos.write(bytesChavesimetricaCifrada);
        fos.close();
        
        return bytesFicheiroCifrado;
    }

    private String getStringDeFicheiroBD(byte[] bytesKey, PrivateKey chavePrivAutor, byte[] bytesData) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cifra = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cifra.init(Cipher.DECRYPT_MODE, chavePrivAutor);
        byte[] usersKeyDecifrada = cifra.doFinal(bytesKey);
        SecretKeySpec chaveUsersSim = new SecretKeySpec(usersKeyDecifrada, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, chaveUsersSim);
        byte[] bytesUsersDecifrados = cipher.doFinal(bytesData);
        return  new String(bytesUsersDecifrados);
    }
}
