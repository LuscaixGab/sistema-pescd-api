package br.ufscar.dc.dsw.pescd.dto;

import br.ufscar.dc.dsw.pescd.model.StatusInscricao;

import java.util.UUID;

public class SupervisaoAlunoResumo {
    private UUID inscricaoId;
    private UUID planoId;
    private String nomeOferta;
    private String semestre;
    private String nomeAluno;
    private String nomeDisciplina;
    private StatusInscricao status;
    private boolean podeAvaliarPlano;
    private boolean podeAvaliarRelatorio;

    public UUID getInscricaoId() {
        return inscricaoId;
    }

    public void setInscricaoId(UUID inscricaoId) {
        this.inscricaoId = inscricaoId;
    }

    public UUID getPlanoId() {
        return planoId;
    }

    public void setPlanoId(UUID planoId) {
        this.planoId = planoId;
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

    public String getNomeAluno() {
        return nomeAluno;
    }

    public void setNomeAluno(String nomeAluno) {
        this.nomeAluno = nomeAluno;
    }

    public String getNomeDisciplina() {
        return nomeDisciplina;
    }

    public void setNomeDisciplina(String nomeDisciplina) {
        this.nomeDisciplina = nomeDisciplina;
    }

    public StatusInscricao getStatus() {
        return status;
    }

    public void setStatus(StatusInscricao status) {
        this.status = status;
    }

    public boolean isPodeAvaliarPlano() {
        return podeAvaliarPlano;
    }

    public void setPodeAvaliarPlano(boolean podeAvaliarPlano) {
        this.podeAvaliarPlano = podeAvaliarPlano;
    }

    public boolean isPodeAvaliarRelatorio() {
        return podeAvaliarRelatorio;
    }

    public void setPodeAvaliarRelatorio(boolean podeAvaliarRelatorio) {
        this.podeAvaliarRelatorio = podeAvaliarRelatorio;
    }
}
