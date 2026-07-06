package br.ufscar.dc.dsw.pescd.api.supervisor;

import br.ufscar.dc.dsw.pescd.dto.SupervisaoAlunoResumo;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;

import java.util.UUID;

public record SupervisaoAlunoResponseDTO(
        UUID inscricaoId,
        UUID planoId,
        String nomeOferta,
        String semestre,
        String nomeAluno,
        String nomeDisciplina,
        StatusInscricao status,
        boolean podeAvaliarPlano,
        boolean podeAvaliarRelatorio
) {
    public static SupervisaoAlunoResponseDTO from(SupervisaoAlunoResumo resumo) {
        return new SupervisaoAlunoResponseDTO(
                resumo.getInscricaoId(),
                resumo.getPlanoId(),
                resumo.getNomeOferta(),
                resumo.getSemestre(),
                resumo.getNomeAluno(),
                resumo.getNomeDisciplina(),
                resumo.getStatus(),
                resumo.isPodeAvaliarPlano(),
                resumo.isPodeAvaliarRelatorio()
        );
    }
}
