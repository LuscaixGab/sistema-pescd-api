package br.ufscar.dc.dsw.pescd.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "relatorios_finais")
public class RelatorioFinal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // estratégia mais segura
    private UUID id;

    @Column(nullable = false)
    private Integer frequenciaAluno; // 0 a 100%

    @Column(nullable = false)
    private String arquivoRelatorio; // Caminho do PDF do relatório

    // Dados da avaliação do professor
    @Column(columnDefinition = "TEXT")
    private String parecerSupervisor;

    @Column
    private Integer frequenciaSupervisor;

    @Column(length = 1)
    private String sugestaoNotaSupervisor; // A, B, C, D ou E

    @Column(columnDefinition = "TEXT")
    private String parecerResponsavel;

    @Column
    private Integer frequenciaFinal;

    @Column(length = 1)
    private String notaFinal; // A, B, C, D ou E

    @Column(name = "data_analise_responsavel")
    private LocalDateTime dataAnaliseResponsavel;

    @OneToOne // 1 relatório -> 1 inscrição
    @JoinColumn(name = "inscricao_id", nullable = false) // coluna extra com id da inscrição
    private Inscricao inscricao;

    // Construtor apenas com os dados iniciais do envio do aluno
    public RelatorioFinal(UUID id, Integer frequenciaAluno, String arquivoRelatorio, Inscricao inscricao) {
        this.id = id;
        this.frequenciaAluno = frequenciaAluno;
        this.arquivoRelatorio = arquivoRelatorio;
        this.inscricao = inscricao;
    }

    // Construtor necessário para o Hibernate
    protected RelatorioFinal() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Integer getFrequenciaAluno() {
        return frequenciaAluno;
    }

    public void setFrequenciaAluno(Integer frequenciaAluno) {
        this.frequenciaAluno = frequenciaAluno;
    }

    public String getArquivoRelatorio() {
        return arquivoRelatorio;
    }

    public void setArquivoRelatorio(String arquivoRelatorio) {
        this.arquivoRelatorio = arquivoRelatorio;
    }

    public String getParecerSupervisor() {
        return parecerSupervisor;
    }

    public void setParecerSupervisor(String parecerSupervisor) {
        this.parecerSupervisor = parecerSupervisor;
    }

    public Integer getFrequenciaSupervisor() {
        return frequenciaSupervisor;
    }

    public void setFrequenciaSupervisor(Integer frequenciaSupervisor) {
        this.frequenciaSupervisor = frequenciaSupervisor;
    }

    public String getSugestaoNotaSupervisor() {
        return sugestaoNotaSupervisor;
    }

    public void setSugestaoNotaSupervisor(String sugestaoNotaSupervisor) {
        this.sugestaoNotaSupervisor = sugestaoNotaSupervisor;
    }

    public String getParecerResponsavel() {
        return parecerResponsavel;
    }

    public void setParecerResponsavel(String parecerResponsavel) {
        this.parecerResponsavel = parecerResponsavel;
    }

    public Integer getFrequenciaFinal() {
        return frequenciaFinal;
    }

    public void setFrequenciaFinal(Integer frequenciaFinal) {
        this.frequenciaFinal = frequenciaFinal;
    }

    public String getNotaFinal() {
        return notaFinal;
    }

    public void setNotaFinal(String notaFinal) {
        this.notaFinal = notaFinal;
    }

    public Inscricao getInscricao() {
        return inscricao;
    }

    public void setInscricao(Inscricao inscricao) {
        this.inscricao = inscricao;
    }

    public LocalDateTime getDataAnaliseResponsavel() {
        return dataAnaliseResponsavel;
    }

    public void setDataAnaliseResponsavel(LocalDateTime dataAnaliseResponsavel) {
        this.dataAnaliseResponsavel = dataAnaliseResponsavel;
    }
}