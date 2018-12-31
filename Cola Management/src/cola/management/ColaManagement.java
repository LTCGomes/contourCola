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
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import sun.security.x509.AlgorithmId;
import sun.security.x509.CertificateAlgorithmId;
import sun.security.x509.CertificateIssuerName;
import sun.security.x509.CertificateSerialNumber;
import sun.security.x509.CertificateSubjectName;
import sun.security.x509.CertificateValidity;
import sun.security.x509.CertificateVersion;
import sun.security.x509.CertificateX509Key;
import sun.security.x509.X500Name;
import sun.security.x509.X509CertImpl;
import sun.security.x509.X509CertInfo;

/**
 *
 * @author tjsantos
 */
/**
 * A licença deve ter: 
 * - Hashs de dados assinados pelo autor 
 * - certificado do autor 
 * - hash de dados
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
    public ColaManagement() throws NoSuchAlgorithmException, IOException {
        if (!new File("PedidosLicenca").isDirectory()) {
            new File("PedidosLicenca").mkdir();
            if (!new File("PedidosLicenca/Keys").isDirectory()) {
                new File("PedidosLicenca/Keys").mkdir();
            }
        }
        if (!new File("Licencas").isDirectory()) {
            new File("Licencas").mkdir();
        }
        
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
    
    public X509Certificate generateCertificate() throws Exception {
        PrivateKey privkey = chaves.getPrivate("KeyPair/privateKey.privk");
        X509CertInfo info = new X509CertInfo();
        Date from = new Date();
        //validade de certificados de 1 ano
        Date to = new Date(from.getTime() + 365 * 86400000l);
        CertificateValidity interval = new CertificateValidity(from, to);
        BigInteger sn = new BigInteger(64, new SecureRandom());
        X500Name owner = new X500Name("Contour Management");

        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
        info.set(X509CertInfo.SUBJECT, new CertificateSubjectName(owner));
        info.set(X509CertInfo.ISSUER, new CertificateIssuerName(owner));
        info.set(X509CertInfo.KEY, new CertificateX509Key(chaves.getPublic("KeyPair/publicKey.publick")));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(privkey, "SHA1withRSA");

        // Update the algorith, and resign.
        algo = (AlgorithmId)cert.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
        cert = new X509CertImpl(info);
        cert.sign(privkey, "SHA1withRSA");
        return cert;
    }  

    public void generateLicence(byte[] bytesVars) throws NoSuchAlgorithmException, Exception {
        
        //gerar hash de dados a partir
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytesVars);
        byte[] hash = md.digest();
        System.out.println("Hash: "+Arrays.toString(hash));    //GUARDAR ISTO
        licenca.add(bytesSig);
        
        //gerar certificado com validade de 1 ano
        X509Certificate cert = generateCertificate();
        
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
        System.out.println("============");
        System.out.println("array bytes bytesVarsCifrados: "+Arrays.toString(list.get(0)));
        System.out.println("array bytes bytesChaveSimCifrada: "+Arrays.toString(list.get(1)));
        System.out.println("array bytes certificado: "+Arrays.toString(list.get(2)));
        System.out.println("array bytes assinatura: "+Arrays.toString(list.get(3)));
        System.out.println("============");
    }

    public byte[] getSimKey(PublicKey chavePublicaUtilizador) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cifra = Cipher.getInstance("RSA");
        cifra.init(Cipher.DECRYPT_MODE, chavePublicaUtilizador);
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
            X509Certificate certificado = (X509Certificate)cf.generateCertificate(is);
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
        System.out.println("#------------------------------------------------#");
        System.out.println("#O que pretende fazer?                           #");
        System.out.println("#1 - Gerar ficheiros de licenca                  #");
        //System.out.println("#Verificar licenca para um utilizador  (2)#");
        System.out.println("#------------------------------------------------#");
        Scanner scan = new Scanner(System.in);
        String opcao = scan.nextLine();

        if (opcao.equals("1")) {

            System.out.println("#------------------------------------------------#");
            System.out.println("#Certifique-se que tem os ficheiros de       #");
            System.out.println("#pedidos de licença na pasta 'PedidosLicenca'#");
            System.out.println("#e a chave publica respetiva na pasta 'Keys' #");
            System.out.println("#--------------------------------------------#");
            System.out.println("#Qual o ficheiro de pedido de licença?       #");
            System.out.println("#--------------------------------------------#");
            String opcao1 = scan.nextLine();
            System.out.println("#Qual o ficheiro da chave publica?           #");
            System.out.println("#--------------------------------------------#");
            String opcao2 = scan.nextLine();

            //buscar chave publica ao ficheiro
            byte[] bytesChavePublicaUtilizador = autor.readFromFile("PedidosLicenca/Keys/" + opcao2);
            KeyFactory keyfa = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec xek = new X509EncodedKeySpec(bytesChavePublicaUtilizador);
            PublicKey chavePublicaUtilizador = keyfa.generatePublic(xek);

            //buscar array list do pedido de licença
            autor.readListFromFile("PedidosLicenca/" + opcao1);
            
            //buscar certificado
            PublicKey chavePublicaCertificado = autor.getChaveCertificado();
            boolean verificacao = autor.getVerificacaoAssinatura(chavePublicaCertificado);
            if(autor.getVerificacaoAssinatura(chavePublicaCertificado)) {
                //Se verdadeiro, continua e vai gerar a licença
                
                //usar chave publica asimetrica para decifrar chave simetrica
                byte[] bytesChaveSimetrica = autor.getSimKey(chavePublicaUtilizador);

                //usar chave simetrica para decifrar dados do utilizador
                SecretKey chaveDeCifraSim = new SecretKeySpec(bytesChaveSimetrica, "AES");
                byte[] bytesVars = autor.getDadosDecifrados(chaveDeCifraSim);

                //TODO: VALIDAR SE OS DADOS JÁ NÃO ESTÃO EM USO NOUTRA LICENÇA
                
                autor.generateLicence(bytesVars);
            } else {
                //se falso, avisa...
                
                System.out.println("A assinatura não é válida! A sair do programa.");
            }            
        } else {
            System.out.println("Opção inválida ... a sair do programa...");
        }
        /**
         * - Dados do sistema cifrados por uma chave simetrica - chave simetrica
         * cifrada pela chave privada assimetrica - assinatura com o certificado
         * do cartão de cidadão - certificado do cartão de cidadão
         */

        //Buscar certificado do CC e verificar se não há mais nenhuma licenca para este utilizador
    }
}
