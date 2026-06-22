package br.ufscar.dc.dsw.pescd.dto;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public class OfertaForm {

    private String nomeOferta;

    @NotBlank(message = "{validation.semester.required}")
    private String semestre;

    @NotNull(message = "{validation.startDate.required}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataInicio;

    @NotNull(message = "{validation.endDate.required}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataFim;

    @NotNull(message = "{validation.responsibleProfessor.required}")
    private UUID professorResponsavelId;

    public String getNomeOferta() {
        return nomeOferta;
    }

    public void setNomeOferta(String nomeOferta) {
        this.nomeOferta = nomeOferta;
    }

    public String getSemestre() {
        return semestre;
    }

    public void setSemestre(String semestre) {
        this.semestre = semestre;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public UUID getProfessorResponsavelId() {
        return professorResponsavelId;
    }

    public void setProfessorResponsavelId(UUID professorResponsavelId) {
        this.professorResponsavelId = professorResponsavelId;
    }
}
