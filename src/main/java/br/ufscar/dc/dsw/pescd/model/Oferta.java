package br.ufscar.dc.dsw.pescd.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ofertas")
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nomeOferta;

    private String codigoDisciplina;

    private String nomeDisciplina;

    private String cursoDisciplina;

    @Column(nullable = false)
    private String semestre;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataFim;

    @ManyToOne
    @JoinColumn(name = "professor_responsavel_id", nullable = false)
    private Usuario professorResponsavel;

    @Column(name = "data_criacao", nullable = false, updatable = false)
    private LocalDateTime dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOferta statusOferta = StatusOferta.EM_ANDAMENTO;

    @Column(columnDefinition = "TEXT")
    private String licoesAprendidas;

    @Column(name = "data_encerramento_responsavel")
    private LocalDateTime dataEncerramentoResponsavel;

    @ManyToOne
    @JoinColumn(name = "usuario_criador_id", nullable = false, updatable = false)
    private Usuario usuarioCriador;

    @Column(name = "data_encerramento")
    private LocalDateTime dataEncerramento;

    @ManyToOne
    @JoinColumn(name = "usuario_encerramento_id")
    private Usuario usuarioEncerramento;

    public Oferta(UUID id, String nomeOferta, String semestre, LocalDate dataInicio, LocalDate dataFim,
            Usuario professorResponsavel) {
        this.id = id;
        this.nomeOferta = nomeOferta;
        this.semestre = semestre;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.professorResponsavel = professorResponsavel;
    }

    public Oferta() {
    }

    @PrePersist
    protected void prePersist() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }
        if (statusOferta == null) {
            statusOferta = StatusOferta.EM_ANDAMENTO;
        }
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

    public Usuario getProfessorResponsavel() {
        return professorResponsavel;
    }

    public void setProfessorResponsavel(Usuario professorResponsavel) {
        this.professorResponsavel = professorResponsavel;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Usuario getUsuarioCriador() {
        return usuarioCriador;
    }

    public void setUsuarioCriador(Usuario usuarioCriador) {
        this.usuarioCriador = usuarioCriador;
    }

    public LocalDateTime getDataEncerramento() {
        return dataEncerramento;
    }

    public void setDataEncerramento(LocalDateTime dataEncerramento) {
        this.dataEncerramento = dataEncerramento;
    }

    public Usuario getUsuarioEncerramento() {
        return usuarioEncerramento;
    }

    public void setUsuarioEncerramento(Usuario usuarioEncerramento) {
        this.usuarioEncerramento = usuarioEncerramento;
    }

    public StatusOferta getStatusOferta() {
        return statusOferta;
    }

    public void setStatusOferta(StatusOferta statusOferta) {
        this.statusOferta = statusOferta;
    }

    public String getLicoesAprendidas() {
        return licoesAprendidas;
    }

    public void setLicoesAprendidas(String licoesAprendidas) {
        this.licoesAprendidas = licoesAprendidas;
    }

    public LocalDateTime getDataEncerramentoResponsavel() {
        return dataEncerramentoResponsavel;
    }

    public void setDataEncerramentoResponsavel(LocalDateTime dataEncerramentoResponsavel) {
        this.dataEncerramentoResponsavel = dataEncerramentoResponsavel;
    }
}
