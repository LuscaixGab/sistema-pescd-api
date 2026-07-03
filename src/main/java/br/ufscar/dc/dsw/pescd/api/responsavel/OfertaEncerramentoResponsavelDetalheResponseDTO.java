package br.ufscar.dc.dsw.pescd.api.responsavel;

import br.ufscar.dc.dsw.pescd.dto.ResumoAlunoEncerramentoOferta;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.model.StatusOferta;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record OfertaEncerramentoResponsavelDetalheResponseDTO(
        UUID id,
        String nomeOferta,
        String semestre,
        LocalDate dataInicio,
        LocalDate dataFim,
        StatusOferta statusOferta,
        UUID professorResponsavelId,
        String professorResponsavelNome,
        String licoesAprendidas,
        LocalDateTime dataEncerramentoResponsavel,
        double mediaFrequencia,
        long quantidadeEstagio,
        long quantidadeDocumentacao,
        Map<String, Long> quantidadeNotas,
        List<ResumoAlunoEncerramentoResponseDTO> alunos
) {
    public static OfertaEncerramentoResponsavelDetalheResponseDTO from(
            Oferta oferta,
            List<ResumoAlunoEncerramentoOferta> resumoAlunos,
            double mediaFrequencia,
            long quantidadeEstagio,
            long quantidadeDocumentacao,
            Map<String, Long> quantidadeNotas) {

        return new OfertaEncerramentoResponsavelDetalheResponseDTO(
                oferta.getId(),
                oferta.getNomeOferta(),
                oferta.getSemestre(),
                oferta.getDataInicio(),
                oferta.getDataFim(),
                oferta.getStatusOferta(),
                oferta.getProfessorResponsavel() != null ? oferta.getProfessorResponsavel().getId() : null,
                oferta.getProfessorResponsavel() != null ? oferta.getProfessorResponsavel().getNomeCompleto() : null,
                oferta.getLicoesAprendidas(),
                oferta.getDataEncerramentoResponsavel(),
                mediaFrequencia,
                quantidadeEstagio,
                quantidadeDocumentacao,
                quantidadeNotas,
                resumoAlunos.stream()
                        .map(ResumoAlunoEncerramentoResponseDTO::from)
                        .toList()
        );
    }
}
