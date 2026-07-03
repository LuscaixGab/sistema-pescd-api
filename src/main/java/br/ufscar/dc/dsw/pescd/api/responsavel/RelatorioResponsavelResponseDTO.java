package br.ufscar.dc.dsw.pescd.api.responsavel;

import br.ufscar.dc.dsw.pescd.model.RelatorioFinal;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;

import java.util.UUID;

public record RelatorioResponsavelResponseDTO(
        UUID id,
        UUID inscricaoId,
        String nomeAluno,
        String nomeOferta,
        String semestre,
        StatusInscricao status,
        String arquivoRelatorio,
        Integer frequenciaAluno,
        String parecerSupervisor,
        Integer frequenciaSupervisor,
        String sugestaoNotaSupervisor,
        String parecerResponsavel,
        Integer frequenciaFinal,
        String notaFinal
) {
    public static RelatorioResponsavelResponseDTO from(RelatorioFinal relatorio) {
        return new RelatorioResponsavelResponseDTO(
                relatorio.getId(),
                relatorio.getInscricao().getId(),
                relatorio.getInscricao().getAluno().getNomeCompleto(),
                relatorio.getInscricao().getOferta().getNomeOferta(),
                relatorio.getInscricao().getOferta().getSemestre(),
                relatorio.getInscricao().getStatus(),
                relatorio.getArquivoRelatorio(),
                relatorio.getFrequenciaAluno(),
                relatorio.getParecerSupervisor(),
                relatorio.getFrequenciaSupervisor(),
                relatorio.getSugestaoNotaSupervisor(),
                relatorio.getParecerResponsavel(),
                relatorio.getFrequenciaFinal(),
                relatorio.getNotaFinal()
        );
    }
}
