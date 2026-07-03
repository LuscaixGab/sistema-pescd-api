package br.ufscar.dc.dsw.pescd.api.responsavel;

import br.ufscar.dc.dsw.pescd.dto.OfertaEncerramentoResponsavelResumo;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.model.StatusOferta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record OfertaEncerramentoResponsavelResponseDTO(
        UUID id,
        String nomeOferta,
        String semestre,
        LocalDate dataInicio,
        LocalDate dataFim,
        StatusOferta statusOferta,
        UUID professorResponsavelId,
        String professorResponsavelNome,
        boolean podeEncerrar,
        String motivoBloqueio,
        String licoesAprendidas,
        LocalDateTime dataEncerramentoResponsavel
) {
    public static OfertaEncerramentoResponsavelResponseDTO from(OfertaEncerramentoResponsavelResumo resumo) {
        Oferta oferta = resumo.getOferta();
        return new OfertaEncerramentoResponsavelResponseDTO(
                oferta.getId(),
                oferta.getNomeOferta(),
                oferta.getSemestre(),
                oferta.getDataInicio(),
                oferta.getDataFim(),
                oferta.getStatusOferta(),
                oferta.getProfessorResponsavel() != null ? oferta.getProfessorResponsavel().getId() : null,
                oferta.getProfessorResponsavel() != null ? oferta.getProfessorResponsavel().getNomeCompleto() : null,
                resumo.isPodeEncerrar(),
                resumo.getMotivoBloqueio(),
                oferta.getLicoesAprendidas(),
                oferta.getDataEncerramentoResponsavel()
        );
    }
}
