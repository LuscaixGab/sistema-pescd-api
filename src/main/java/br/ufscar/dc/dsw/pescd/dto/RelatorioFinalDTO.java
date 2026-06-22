package br.ufscar.dc.dsw.pescd.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class RelatorioFinalDTO {

    @NotNull(message = "{validation.frequency.required}")
    @Min(value = 0, message = "{validation.frequency.min}")
    @Max(value = 100, message = "{validation.frequency.max}")
    private Integer frequenciaAluno;

    @NotNull(message = "{validation.reportFile.required}")
    private MultipartFile arquivo;

    // Getters e Setters
    public Integer getFrequenciaAluno() {
        return frequenciaAluno;
    }

    public void setFrequenciaAluno(Integer frequenciaAluno) {
        this.frequenciaAluno = frequenciaAluno;
    }

    public MultipartFile getArquivo() {
        return arquivo;
    }

    public void setArquivo(MultipartFile arquivo) {
        this.arquivo = arquivo;
    }
}