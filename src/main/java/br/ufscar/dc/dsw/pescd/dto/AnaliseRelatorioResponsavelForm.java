package br.ufscar.dc.dsw.pescd.dto;

import jakarta.validation.constraints.*;

public class AnaliseRelatorioResponsavelForm {
    @NotBlank(message = "O parecer é obrigatório.")
    private String parecer;

    @NotNull(message = "O indicador de frequência é obrigatório.")
    @Min(value = 0, message = "O indicador de frequência deve ser no mínimo 0%.")
    @Max(value = 100, message = "O indicador de frequência deve ser no máximo 100%.")
    private Integer indicadorFrequencia;

    @NotBlank(message = "A nota é obrigatória.")
    @Pattern(regexp = "[ABCDE]", message = "A nota deve ser A, B, C, D ou E.")
    private String nota;

    public String getParecer() {
        return parecer;
    }

    public void setParecer(String parecer) {
        this.parecer = parecer;
    }

    public Integer getIndicadorFrequencia() {
        return indicadorFrequencia;
    }

    public void setIndicadorFrequencia(Integer indicadorFrequencia) {
        this.indicadorFrequencia = indicadorFrequencia;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
}
