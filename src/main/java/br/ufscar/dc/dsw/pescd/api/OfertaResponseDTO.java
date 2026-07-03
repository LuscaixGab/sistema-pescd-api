package br.ufscar.dc.dsw.pescd.api;

import br.ufscar.dc.dsw.pescd.model.Oferta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class OfertaResponseDTO {

    private UUID id;
    private String nomeOferta;
    private String semestre;
    private LocalDate dataInicio;
    private LocalDate dataFim;
    private UUID professorResponsavelId;
    private String professorResponsavelNome;
    private UUID usuarioCriadorId;
    private String usuarioCriadorNome;
    private LocalDateTime dataCriacao;
    private String statusOferta;

    public static OfertaResponseDTO from(Oferta oferta) {
        OfertaResponseDTO dto = new OfertaResponseDTO();
        dto.setId(oferta.getId());
        dto.setNomeOferta(oferta.getNomeOferta());
        dto.setSemestre(oferta.getSemestre());
        dto.setDataInicio(oferta.getDataInicio());
        dto.setDataFim(oferta.getDataFim());
        dto.setProfessorResponsavelId(oferta.getProfessorResponsavel() != null ? oferta.getProfessorResponsavel().getId() : null);
        dto.setProfessorResponsavelNome(oferta.getProfessorResponsavel() != null ? oferta.getProfessorResponsavel().getNomeCompleto() : null);
        dto.setUsuarioCriadorId(oferta.getUsuarioCriador() != null ? oferta.getUsuarioCriador().getId() : null);
        dto.setUsuarioCriadorNome(oferta.getUsuarioCriador() != null ? oferta.getUsuarioCriador().getNomeCompleto() : null);
        dto.setDataCriacao(oferta.getDataCriacao());
        dto.setStatusOferta(oferta.getStatusOferta() != null ? oferta.getStatusOferta().name() : null);
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

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

    public String getProfessorResponsavelNome() {
        return professorResponsavelNome;
    }

    public void setProfessorResponsavelNome(String professorResponsavelNome) {
        this.professorResponsavelNome = professorResponsavelNome;
    }

    public UUID getUsuarioCriadorId() {
        return usuarioCriadorId;
    }

    public void setUsuarioCriadorId(UUID usuarioCriadorId) {
        this.usuarioCriadorId = usuarioCriadorId;
    }

    public String getUsuarioCriadorNome() {
        return usuarioCriadorNome;
    }

    public void setUsuarioCriadorNome(String usuarioCriadorNome) {
        this.usuarioCriadorNome = usuarioCriadorNome;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getStatusOferta() {
        return statusOferta;
    }

    public void setStatusOferta(String statusOferta) {
        this.statusOferta = statusOferta;
    }
}
