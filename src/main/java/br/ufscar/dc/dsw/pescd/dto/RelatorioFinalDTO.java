package br.ufscar.dc.dsw.pescd.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class RelatorioFinalDTO {

    @NotNull(message = "A frequência é obrigatória.")
    @Min(value = 0, message = "A frequência mínima é 0%.")
    @Max(value = 100, message = "A frequência máxima é 100%.")
    private Integer frequenciaAluno;

    @NotNull(message = "O arquivo do relatório é obrigatório.")
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