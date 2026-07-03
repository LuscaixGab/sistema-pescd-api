package br.ufscar.dc.dsw.pescd.api;

import br.ufscar.dc.dsw.pescd.model.Usuario;

import java.util.UUID;

public class LoginResponseDTO {

    private UUID id;
    private String nomeCompleto;
    private String nomeUsuario;
    private String perfil;
    private String redirectUrl;

    public static LoginResponseDTO from(Usuario usuario, String redirectUrl) {
        LoginResponseDTO dto = new LoginResponseDTO();
        dto.setId(usuario.getId());
        dto.setNomeCompleto(usuario.getNomeCompleto());
        dto.setNomeUsuario(usuario.getNomeUsuario());
        dto.setPerfil(usuario.getPerfil().name());
        dto.setRedirectUrl(redirectUrl);
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
