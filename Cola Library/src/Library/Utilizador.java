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
class Utilizador {
    private String nome;
    private String email;
    private String identicacaoCivil;
    // private certificado de chave publica

    public Utilizador(String nome, String email, String identicacaoCivil) {
        this.nome = nome;
        this.email = email;
        this.identicacaoCivil = identicacaoCivil;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getIdenticacaoCivil() {
        return identicacaoCivil;
    }
    
}
