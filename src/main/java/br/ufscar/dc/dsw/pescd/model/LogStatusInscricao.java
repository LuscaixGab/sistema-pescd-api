package br.ufscar.dc.dsw.pescd.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "logs_status_inscricao")
public class LogStatusInscricao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "inscricao_id", nullable = false)
    private Inscricao inscricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusInscricao statusNovo;

    @Column(nullable = false)
    private LocalDateTime dataMudanca;

    @ManyToOne
    @JoinColumn(name = "usuario_responsavel_id")
    private Usuario usuarioResponsavel;

    public LogStatusInscricao() {}

    public LogStatusInscricao(Inscricao inscricao, StatusInscricao statusNovo, LocalDateTime dataMudanca, Usuario usuarioResponsavel) {
        this.inscricao = inscricao;
        this.statusNovo = statusNovo;
        this.dataMudanca = dataMudanca;
        this.usuarioResponsavel = usuarioResponsavel;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Inscricao getInscricao() { return inscricao; }
    public void setInscricao(Inscricao inscricao) { this.inscricao = inscricao; }
    public StatusInscricao getStatusNovo() { return statusNovo; }
    public void setStatusNovo(StatusInscricao statusNovo) { this.statusNovo = statusNovo; }
    public LocalDateTime getDataMudanca() { return dataMudanca; }
    public void setDataMudanca(LocalDateTime dataMudanca) { this.dataMudanca = dataMudanca; }
    public Usuario getUsuarioResponsavel() { return usuarioResponsavel; }
    public void setUsuarioResponsavel(Usuario usuarioResponsavel) { this.usuarioResponsavel = usuarioResponsavel; }
}