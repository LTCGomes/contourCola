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
import java.nio.file.Files;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

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

    /**
     * @param args the command line arguments
     */
    public ColaManagement() {
        if (!new File("PedidosLicenca").isDirectory()) {
            new File("PedidosLicenca").mkdir();
            if (!new File("PedidosLicenca/Keys").isDirectory()) {
                new File("PedidosLicenca/Keys").mkdir();
            }
        }
        if (!new File("Licencas").isDirectory()) {
            new File("Licencas").mkdir();
        }
    }

    public void generateLicence(byte[] bytesVars) throws NoSuchAlgorithmException {
        
        //gerar hash de dados a partir
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytesVars);
        byte[] hash = md.digest();
        System.out.println("Hash: "+Arrays.toString(hash));    //GUARDAR ISTO
        licenca.add(bytesSig);
        
        
        
        
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
    
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, FileNotFoundException, ClassNotFoundException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, CertificateException, SignatureException {
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
