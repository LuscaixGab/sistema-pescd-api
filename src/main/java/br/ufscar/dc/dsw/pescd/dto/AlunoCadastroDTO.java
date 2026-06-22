package br.ufscar.dc.dsw.pescd.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AlunoCadastroDTO {

    @NotBlank(message = "{validation.fullName.required}")
    private String nomeCompleto;

    @NotBlank(message = "{validation.email.required}")
    @Email(message = "{validation.email.valid}")
    private String email;

    @NotBlank(message = "{validation.usernameRa.required}")
    private String nomeUsuario;

    @NotBlank(message = "{validation.password.required}")
    @Size(min = 6, message = "{validation.password.size}")
    private String senha;

    // Construtor vazio padrão do Spring
    public AlunoCadastroDTO() {
    }

    // Getters e Setters
    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}