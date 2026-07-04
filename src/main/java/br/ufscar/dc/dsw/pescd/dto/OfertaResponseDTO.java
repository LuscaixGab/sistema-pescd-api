package br.ufscar.dc.dsw.pescd.dto;

import br.ufscar.dc.dsw.pescd.model.Oferta;
import java.util.UUID;

public class OfertaResponseDTO {
    private UUID id;
    private String nomeOferta;
    private String codigoDisciplina;
    private String nomeDisciplina;
    private String cursoDisciplina;
    private String semestre;
    private String dataInicio;
    private String dataFim;
    private String statusOferta;
    private String licoesAprendidas;
    private String dataEncerramento;

    public static OfertaResponseDTO from(Oferta oferta) {
        OfertaResponseDTO dto = new OfertaResponseDTO();
        dto.id = oferta.getId();
        dto.nomeOferta = oferta.getNomeOferta();
        dto.codigoDisciplina = oferta.getCodigoDisciplina();
        dto.nomeDisciplina = oferta.getNomeDisciplina();
        dto.cursoDisciplina = oferta.getCursoDisciplina();
        dto.semestre = oferta.getSemestre();
        dto.dataInicio = oferta.getDataInicio() != null ? oferta.getDataInicio().toString() : null;
        dto.dataFim = oferta.getDataFim() != null ? oferta.getDataFim().toString() : null;
        dto.statusOferta = oferta.getStatusOferta() != null ? oferta.getStatusOferta().name() : null;
        dto.licoesAprendidas = oferta.getLicoesAprendidas();
        dto.dataEncerramento = oferta.getDataEncerramento() != null ? oferta.getDataEncerramento().toString() : null;
        return dto;
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getNomeOferta() { return nomeOferta; }
    public void setNomeOferta(String nomeOferta) { this.nomeOferta = nomeOferta; }
    public String getCodigoDisciplina() { return codigoDisciplina; }
    public void setCodigoDisciplina(String codigoDisciplina) { this.codigoDisciplina = codigoDisciplina; }
    public String getNomeDisciplina() { return nomeDisciplina; }
    public void setNomeDisciplina(String nomeDisciplina) { this.nomeDisciplina = nomeDisciplina; }
    public String getCursoDisciplina() { return cursoDisciplina; }
    public void setCursoDisciplina(String cursoDisciplina) { this.cursoDisciplina = cursoDisciplina; }
    public String getSemestre() { return semestre; }
    public void setSemestre(String semestre) { this.semestre = semestre; }
    public String getDataInicio() { return dataInicio; }
    public void setDataInicio(String dataInicio) { this.dataInicio = dataInicio; }
    public String getDataFim() { return dataFim; }
    public void setDataFim(String dataFim) { this.dataFim = dataFim; }
    public String getStatusOferta() { return statusOferta; }
    public void setStatusOferta(String statusOferta) { this.statusOferta = statusOferta; }
    public String getLicoesAprendidas() { return licoesAprendidas; }
    public void setLicoesAprendidas(String licoesAprendidas) { this.licoesAprendidas = licoesAprendidas; }
    public String getDataEncerramento() { return dataEncerramento; }
    public void setDataEncerramento(String dataEncerramento) { this.dataEncerramento = dataEncerramento; }
}
