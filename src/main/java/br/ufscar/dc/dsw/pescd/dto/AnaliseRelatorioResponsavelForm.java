package br.ufscar.dc.dsw.pescd.dto;

import jakarta.validation.constraints.*;

public class AnaliseRelatorioResponsavelForm {
    @NotBlank(message = "{validation.opinion.required}")
    private String parecer;

    @NotNull(message = "{validation.frequencyIndicator.required}")
    @Min(value = 0, message = "{validation.frequencyIndicator.min}")
    @Max(value = 100, message = "{validation.frequencyIndicator.max}")
    private Integer indicadorFrequencia;

    @NotBlank(message = "{validation.grade.required}")
    @Pattern(regexp = "[ABCDE]", message = "{validation.grade.pattern}")
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
