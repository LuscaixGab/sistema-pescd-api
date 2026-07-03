package br.ufscar.dc.dsw.pescd.api.responsavel;

import br.ufscar.dc.dsw.pescd.model.DocumentacaoAula;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;

import java.util.UUID;

public record DocumentacaoResponsavelResponseDTO(
        UUID id,
        UUID inscricaoId,
        String nomeAluno,
        String nomeOferta,
        String semestre,
        StatusInscricao status,
        String arquivoDocumentacao,
        String parecer,
        Integer indicadorFrequencia,
        String nota
) {
    public static DocumentacaoResponsavelResponseDTO from(DocumentacaoAula documentacao) {
        return new DocumentacaoResponsavelResponseDTO(
                documentacao.getId(),
                documentacao.getInscricao().getId(),
                documentacao.getInscricao().getAluno().getNomeCompleto(),
                documentacao.getInscricao().getOferta().getNomeOferta(),
                documentacao.getInscricao().getOferta().getSemestre(),
                documentacao.getInscricao().getStatus(),
                documentacao.getArquivoDocumentacao(),
                documentacao.getParecer(),
                documentacao.getIndicadorFrequencia(),
                documentacao.getNota()
        );
    }
}