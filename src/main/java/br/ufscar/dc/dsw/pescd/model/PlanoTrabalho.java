package br.ufscar.dc.dsw.pescd.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "planos_trabalho")
public class PlanoTrabalho {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Column(nullable = false)
    private String codigoDisciplina;

    @Column(nullable = false)
    private String nomeDisciplina;

    @Column(nullable = false)
    private String cursoDisciplina;

    @Column(nullable = false)
    private String arquivoPlano;

    @Column(columnDefinition = "TEXT")
    private String parecer;

    @ManyToOne
    @JoinColumn(name = "professor_supervisor_id", nullable = false)
    private Usuario professorSupervisor;

    @OneToOne(optional = true)
    @JoinColumn(name = "inscricao_id", nullable = true)
    private Inscricao inscricao;

    public PlanoTrabalho(UUID id, String codigoDisciplina, String nomeDisciplina, String cursoDisciplina,
                         String arquivoPlano, Usuario professorSupervisor, Inscricao inscricao) {
        this.id = id;
        this.codigoDisciplina = codigoDisciplina;
        this.nomeDisciplina = nomeDisciplina;
        this.cursoDisciplina = cursoDisciplina;
        this.arquivoPlano = arquivoPlano;
        this.professorSupervisor = professorSupervisor;
        this.inscricao = inscricao;
    }

    protected PlanoTrabalho() {
    }

    @PrePersist
    protected void prePersist() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
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

    public String getArquivoPlano() {
        return arquivoPlano;
    }

    public void setArquivoPlano(String arquivoPlano) {
        this.arquivoPlano = arquivoPlano;
    }

    public String getParecer() {
        return parecer;
    }

    public void setParecer(String parecer) {
        this.parecer = parecer;
    }

    public Usuario getProfessorSupervisor() {
        return professorSupervisor;
    }

    public void setProfessorSupervisor(Usuario professorSupervisor) {
        this.professorSupervisor = professorSupervisor;
    }

    public Inscricao getInscricao() {
        return inscricao;
    }

    public void setInscricao(Inscricao inscricao) {
        this.inscricao = inscricao;
    }
}
