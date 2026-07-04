package br.ufscar.dc.dsw.pescd.dto;

import br.ufscar.dc.dsw.pescd.model.PlanoTrabalho;
import java.util.UUID;

public class PlanoTrabalhoResponseDTO {
    private UUID id;
    private String codigoDisciplina;
    private String nomeDisciplina;
    private String cursoDisciplina;
    private String arquivoPlano;
    private String parecer;
    private String statusInscricao;

    public static PlanoTrabalhoResponseDTO from(PlanoTrabalho plano) {
        PlanoTrabalhoResponseDTO dto = new PlanoTrabalhoResponseDTO();
        dto.id = plano.getId();
        dto.codigoDisciplina = plano.getCodigoDisciplina();
        dto.nomeDisciplina = plano.getNomeDisciplina();
        dto.cursoDisciplina = plano.getCursoDisciplina();
        dto.arquivoPlano = plano.getArquivoPlano();
        dto.parecer = plano.getParecer();
        
        if (plano.getInscricao() != null) {
            dto.statusInscricao = plano.getInscricao().getStatus() != null ? plano.getInscricao().getStatus().name() : null;
        }
        return dto;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getCodigoDisciplina() { return codigoDisciplina; }
    public void setCodigoDisciplina(String codigoDisciplina) { this.codigoDisciplina = codigoDisciplina; }
    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }
    public String getCursoDisciplina() { return cursoDisciplina; }
    public void setCursoDisciplina(String cursoDisciplina) { this.cursoDisciplina = cursoDisciplina; }
    public String getArquivoPlano() { return arquivoPlano; }
    public void setArquivoPlano(String arquivoPlano) { this.arquivoPlano = arquivoPlano; }
    public String getParecer() { return parecer; }
    public void setParecer(String parecer) { this.parecer = parecer; }
    public String getStatusInscricao() { return statusInscricao; }
    public void setStatusInscricao(String statusInscricao) { this.statusInscricao = statusInscricao; }
}
