package br.ufscar.dc.dsw.pescd.dto;

import br.ufscar.dc.dsw.pescd.model.Oferta;

public class OfertaEncerramentoResponsavelResumo {
    private final Oferta oferta;
    private final boolean podeEncerrar;
    private final String motivoBloqueio;

    public OfertaEncerramentoResponsavelResumo(Oferta oferta, boolean podeEncerrar, String motivoBloqueio) {
        this.oferta = oferta;
        this.podeEncerrar = podeEncerrar;
        this.motivoBloqueio = motivoBloqueio;
    }

    public Oferta getOferta() {
        return oferta;
    }

    public boolean isPodeEncerrar() {
        return podeEncerrar;
    }

    public String getMotivoBloqueio() {
        return motivoBloqueio;
    }
}
