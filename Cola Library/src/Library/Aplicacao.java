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
    private String hashAplicacao;
    private String hashBiblioteca;
    
    public Aplicacao(String nomeAplicacao, String versao, String hashAplicacao, String hashBiblioteca) {
        this.nomeAplicacao = nomeAplicacao;
        this.versao = versao;
        this.hashAplicacao = hashAplicacao;
        this.hashBiblioteca = hashBiblioteca;
    }

    public String getNomeAplicacao() {
        return nomeAplicacao;
    }

    public String getVersao() {
        return versao;
    }

    public String getHashAplicacao() {
        return hashAplicacao;
    }

    public String getHashBiblioteca() {
        return hashBiblioteca;
    }
    
}
