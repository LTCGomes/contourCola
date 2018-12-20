/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cola.management;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;

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

    public void generateLicence(String ficheiroDaChavePublica, String ficheiroDoPedido) {
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
    
    public static void main(String[] args) {
        // TODO code application logic here
        ColaManagement autor = new ColaManagement();
        
        //INSERT MENU HERE
        System.out.println("#------------------------------------------------#");
        System.out.println("#O que pretende fazer?                           #");
        System.out.println("#Gerar ficheiros de licenca                   (1)#");
        //System.out.println("#Verificar licenca para um utilizador  (2)#");
        System.out.println("#------------------------------------------------#");
        Scanner scan = new Scanner(System.in);
        String opcao = scan.nextLine();
        
        if(opcao.equals("1")) {
            
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
            autor.generateLicence(opcao2, opcao1);
        } else {
            System.out.println("Opção inválida ... a sair do programa...");
        }
        /** 
         * - Dados do sistema cifrados por uma chave simetrica
         * - chave simetrica cifrada pela chave privada assimetrica
         * - assinatura com o certificado do cartão de cidadão
         * - certificado do cartão de cidadão
         */
        
        //Buscar certificado do CC e verificar se não há mais nenhuma licenca para este utilizador
    }    
}
