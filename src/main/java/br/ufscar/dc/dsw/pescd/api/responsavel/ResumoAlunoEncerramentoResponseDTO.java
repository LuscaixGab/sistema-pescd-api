package br.ufscar.dc.dsw.pescd.api.responsavel;

import br.ufscar.dc.dsw.pescd.dto.ResumoAlunoEncerramentoOferta;

public record ResumoAlunoEncerramentoResponseDTO(
        String nomeAluno,
        String tipoCredito,
        Integer frequencia,
        String nota
) {
    public static ResumoAlunoEncerramentoResponseDTO from(ResumoAlunoEncerramentoOferta resumo) {
        return new ResumoAlunoEncerramentoResponseDTO(
                resumo.getNomeAluno(),
                resumo.getTipoCredito(),
                resumo.getFrequencia(),
                resumo.getNota()
        );
    }
}
