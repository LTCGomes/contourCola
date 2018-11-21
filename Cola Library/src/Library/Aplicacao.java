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
class Aplicacao {
    private String nomeAplicacao;
    private String versao;
    
    public Aplicacao(String nomeAplicacao, String versao) {
        this.nomeAplicacao = nomeAplicacao;
        this.versao = versao;
    }

    public String getNomeAplicacao() {
        return nomeAplicacao;
    }

    public String getVersao() {
        return versao;
    }
    
    
}
