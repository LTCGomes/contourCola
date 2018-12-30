/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Library;

import com.sun.org.apache.xerces.internal.impl.dv.dtd.IDDatatypeValidator;
import pteidlib.PTEID_ID;
import pteidlib.PteidException;
import pteidlib.pteid;

/**
 *
 * @author tjsantos
 */
public class Utilizador {

    private String nome;
    private int identicacaoCivil; 

    
    public String getNome() {
        return nome;
    }

    public int getIdenticacaoCivil() {
        return identicacaoCivil;
    }

    public static boolean loadPteidLib() throws UnsatisfiedLinkError {
        try {
            System.loadLibrary("pteidlibj");
            return true;
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Middleware do Cartão de Cidadão não está instalado");
            return false;
        }

    }

    public boolean getData() throws PteidException {
        try {
            pteid.Init("");
            pteid.SetSODChecking(false);
            PTEID_ID idData = pteid.GetID();
            if (null != idData) {
                this.nome = idData.firstname + " " +idData.name;
                this.identicacaoCivil = Integer.parseInt(idData.numBI);

            }

        } catch (PteidException ex) {
            int errorNumber = Integer.parseInt(ex.getMessage().split("Error code : -")[1]);
            errorCC(errorNumber, ex.getMessage());
            return false;
        }
        return true;
    }

    public void errorCC(int errorNumber, String ex) {
        String message;
        switch (errorNumber) {
            case 1101:
                message = ("Erro desconhecido - Problemas com o serviço de leitor de cartões \nMessage: " + ex);
                System.err.println(message);
                System.exit(errorNumber);
                break;
            case 1104:
                message = ("Não foi possível aceder ao cartão.\nVerifique se está corretamente inserido no leitor." );
                System.err.println(message);
                System.exit(errorNumber);
                break;
            case 1109:
                message = ("Acão cancelada pelo utilizador");
                System.err.println(message);
                System.exit(errorNumber);
                break;
            case 12109:
                message = ("Não é permitido.");
                System.err.println(message);
                System.exit(errorNumber);
                break;
            case 1210:
                message = ("O cartão inserido não corresponde a um cartão de cidadão válido.");
                System.err.println(message);
                System.exit(errorNumber);
                break;
            case 1214:
                message = ("Pin inválido, não tente novamente.\n" + ex);
                System.err.println(message);
                System.exit(errorNumber);
                break;
            default:
                message = ("Erro desconhecido: " + ex);
                System.err.println(message);
                System.exit(errorNumber);
                break;
        }
    }
}

