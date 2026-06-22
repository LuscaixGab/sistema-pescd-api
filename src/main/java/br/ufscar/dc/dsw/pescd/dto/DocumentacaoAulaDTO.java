package br.ufscar.dc.dsw.pescd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.web.multipart.MultipartFile;

public class DocumentacaoAulaDTO {

    @NotBlank(message = "{validation.institution.required}")
    private String nomeInstituicao;

    @NotBlank(message = "{validation.discipline.required}")
    private String nomeDisciplina;

    @NotBlank(message = "{validation.course.required}")
    private String cursoDisciplina;

    @NotNull(message = "{validation.workload.required}")
    @Positive(message = "{validation.workload.positive}")
    private Integer cargaHoraria;

    @NotNull(message = "{validation.documentationFile.required}")
    private MultipartFile arquivo;

    public String getNomeInstituicao() {
        return nomeInstituicao;
    }

    public void setNomeInstituicao(String nomeInstituicao) {
        this.nomeInstituicao = nomeInstituicao;
    }

    public String getNomeDisciplina() {
        return nomeDisciplina;
    }

    public void setNomeDisciplina(String nomeDisciplina) {
        this.nomeDisciplina = nomeDisciplina;
    }

    public String getCursoDisciplina() {
        return cursoDisciplina;
    }

    public void setCursoDisciplina(String cursoDisciplina) {
        this.cursoDisciplina = cursoDisciplina;
    }

    public Integer getCargaHoraria() {
        return cargaHoraria;
    }

    public void setCargaHoraria(Integer cargaHoraria) {
        this.cargaHoraria = cargaHoraria;
    }

    public MultipartFile getArquivo() {
        return arquivo;
    }

    public void setArquivo(MultipartFile arquivo) {
        this.arquivo = arquivo;
    }

}