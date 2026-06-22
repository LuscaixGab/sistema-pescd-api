package br.ufscar.dc.dsw.pescd.dto;

import jakarta.validation.constraints.NotBlank;

public class EncerramentoResponsavelForm {
    @NotBlank(message = "{validation.lessons.required}")
    private String licoesAprendidas;

    public String getLicoesAprendidas() {
        return licoesAprendidas;
    }

    public void setLicoesAprendidas(String licoesAprendidas) {
        this.licoesAprendidas = licoesAprendidas;
    }
}
