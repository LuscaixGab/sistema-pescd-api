package br.ufscar.dc.dsw.pescd.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "ofertas")
public class Oferta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String nomeOferta;

    @Column(nullable = false)
    private String semestre;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataFim;

    @ManyToOne
    @JoinColumn(name = "professor_responsavel_id", nullable = false)
    private Usuario professorResponsavel;

    // Construtor com atributos atualizado
    public Oferta(UUID id, String nomeOferta, String semestre, LocalDate dataInicio, LocalDate dataFim,
            Usuario professorResponsavel) {
        this.id = id;
        this.nomeOferta = nomeOferta;
        this.semestre = semestre;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.professorResponsavel = professorResponsavel;
    }

    // Construtor vazio para o Hibernate
    protected Oferta() {
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

    public Usuario getProfessorResponsavel() {
        return professorResponsavel;
    }

    public void setProfessorResponsavel(Usuario professorResponsavel) {
        this.professorResponsavel = professorResponsavel;
    }
}