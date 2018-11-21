/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Library;

/**
 *
 * @author tjsantos
 */
public class contourCola {

private Aplicacao aplicacao;
private Utilizador utilizador;
private Sistema sistema;



    public contourCola(String nomeAplicacao, String versao) {
        //utilizador = new Utilizador(nomeAplicacao, versao, nomeAplicacao);
        sistema = new Sistema();
        aplicacao = new Aplicacao(nomeAplicacao, versao);
    }
    
    public boolean isRegister() {
        //TODO
        return false;
    }
    
    public boolean startRegistration() {
        //TODO
        return false;
    }
    
    public void showLicenseInfo() {
        //TODO
    }
}
