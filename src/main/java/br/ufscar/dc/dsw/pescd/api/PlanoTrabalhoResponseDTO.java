package br.ufscar.dc.dsw.pescd.api;

import br.ufscar.dc.dsw.pescd.model.PlanoTrabalho;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;

import java.time.LocalDateTime;
import java.util.UUID;

public class PlanoTrabalhoResponseDTO {

    private UUID id;
    private LocalDateTime dataCriacao;
    private String codigoDisciplina;
    private String nomeDisciplina;
    private String cursoDisciplina;
    private UUID inscricaoId;
    private UUID professorSupervisorId;
    private String professorSupervisorNome;
    private StatusInscricao statusInscricao;

    public static PlanoTrabalhoResponseDTO from(PlanoTrabalho planoTrabalho) {
        PlanoTrabalhoResponseDTO dto = new PlanoTrabalhoResponseDTO();
        dto.setId(planoTrabalho.getId());
        dto.setDataCriacao(planoTrabalho.getDataCriacao());
        dto.setCodigoDisciplina(planoTrabalho.getCodigoDisciplina());
        dto.setNomeDisciplina(planoTrabalho.getNomeDisciplina());
        dto.setCursoDisciplina(planoTrabalho.getCursoDisciplina());
        dto.setInscricaoId(planoTrabalho.getInscricao() != null ? planoTrabalho.getInscricao().getId() : null);
        dto.setProfessorSupervisorId(planoTrabalho.getProfessorSupervisor() != null
                ? planoTrabalho.getProfessorSupervisor().getId()
                : null);
        dto.setProfessorSupervisorNome(planoTrabalho.getProfessorSupervisor() != null
                ? planoTrabalho.getProfessorSupervisor().getNomeCompleto()
                : null);
        dto.setStatusInscricao(planoTrabalho.getInscricao() != null ? planoTrabalho.getInscricao().getStatus() : null);
        return dto;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getCodigoDisciplina() {
        return codigoDisciplina;
    }

    public void setCodigoDisciplina(String codigoDisciplina) {
        this.codigoDisciplina = codigoDisciplina;
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

    public UUID getInscricaoId() {
        return inscricaoId;
    }

    public void setInscricaoId(UUID inscricaoId) {
        this.inscricaoId = inscricaoId;
    }

    public UUID getProfessorSupervisorId() {
        return professorSupervisorId;
    }

    public void setProfessorSupervisorId(UUID professorSupervisorId) {
        this.professorSupervisorId = professorSupervisorId;
    }

    public String getProfessorSupervisorNome() {
        return professorSupervisorNome;
    }

    public void setProfessorSupervisorNome(String professorSupervisorNome) {
        this.professorSupervisorNome = professorSupervisorNome;
    }

    public StatusInscricao getStatusInscricao() {
        return statusInscricao;
    }

    public void setStatusInscricao(StatusInscricao statusInscricao) {
        this.statusInscricao = statusInscricao;
    }
}
