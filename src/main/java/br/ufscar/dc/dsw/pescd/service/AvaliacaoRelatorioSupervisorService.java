package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.PlanoTrabalho;
import br.ufscar.dc.dsw.pescd.model.RelatorioFinal;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.PlanoTrabalhoRepository;
import br.ufscar.dc.dsw.pescd.repository.RelatorioFinalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AvaliacaoRelatorioSupervisorService {

    private static final String ACAO_APROVAR = "aprovar";
    private static final String ACAO_REPROVAR = "reprovar";

    private final InscricaoRepository inscricaoRepository;
    private final RelatorioFinalRepository relatorioFinalRepository;
    private final PlanoTrabalhoRepository planoTrabalhoRepository;
    private final LogStatusService logStatusService;

    public AvaliacaoRelatorioSupervisorService(InscricaoRepository inscricaoRepository,
                                               RelatorioFinalRepository relatorioFinalRepository,
                                               PlanoTrabalhoRepository planoTrabalhoRepository,
                                               LogStatusService logStatusService) {
        this.inscricaoRepository = inscricaoRepository;
        this.relatorioFinalRepository = relatorioFinalRepository;
        this.planoTrabalhoRepository = planoTrabalhoRepository;
        this.logStatusService = logStatusService;
    }

    @Transactional(readOnly = true)
    public DetalhesAvaliacaoRelatorio buscarDetalhesAvaliacao(UUID inscricaoId, Usuario professorSupervisor) {
        Inscricao inscricao = buscarInscricaoPendenteDoSupervisor(inscricaoId, professorSupervisor);
        RelatorioFinal relatorio = buscarRelatorio(inscricao);
        return new DetalhesAvaliacaoRelatorio(inscricao, relatorio);
    }

    @Transactional
    public RelatorioFinal avaliarRelatorio(UUID inscricaoId,
                                           String parecer,
                                           Integer frequencia,
                                           String nota,
                                           String acao,
                                           Usuario professorSupervisor) {
        validarCamposAvaliacao(parecer, frequencia, nota);

        Inscricao inscricao = buscarInscricaoPendenteDoSupervisor(inscricaoId, professorSupervisor);
        RelatorioFinal relatorio = buscarRelatorio(inscricao);

        relatorio.setParecerSupervisor(parecer.trim());
        relatorio.setFrequenciaSupervisor(frequencia);
        relatorio.setSugestaoNotaSupervisor(nota);

        StatusInscricao novoStatus = obterStatusPorAcao(acao);
        inscricao.setStatus(novoStatus);

        relatorioFinalRepository.save(relatorio);
        inscricaoRepository.save(inscricao);
        logStatusService.registrarLog(inscricao, novoStatus, professorSupervisor);

        return relatorio;
    }

    private Inscricao buscarInscricaoPendenteDoSupervisor(UUID inscricaoId, Usuario professorSupervisor) {
        Inscricao inscricao = inscricaoRepository.findById(inscricaoId)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição inválida"));

        PlanoTrabalho plano = planoTrabalhoRepository.findByInscricao(inscricao)
                .orElseThrow(() -> new IllegalArgumentException("Plano de trabalho não encontrado para esta inscrição."));

        if (!plano.getProfessorSupervisor().getId().equals(professorSupervisor.getId())) {
            throw new IllegalArgumentException("Você não é o professor supervisor desta inscrição.");
        }
        if (inscricao.getStatus() != StatusInscricao.RELATORIO_ENVIADO) {
            throw new IllegalArgumentException("O relatório não está pendente de avaliação pelo supervisor.");
        }

        return inscricao;
    }

    private RelatorioFinal buscarRelatorio(Inscricao inscricao) {
        return relatorioFinalRepository.findByInscricao(inscricao)
                .orElseThrow(() -> new IllegalArgumentException("Relatório não encontrado"));
    }

    private void validarCamposAvaliacao(String parecer, Integer frequencia, String nota) {
        if (parecer == null || parecer.trim().isEmpty()) {
            throw new IllegalArgumentException("O parecer é obrigatório.");
        }
        if (frequencia == null || frequencia < 0 || frequencia > 100) {
            throw new IllegalArgumentException("A frequência deve estar entre 0 e 100.");
        }
        if (nota == null || !nota.matches("[ABCDE]")) {
            throw new IllegalArgumentException("A sugestão de nota deve ser A, B, C, D ou E.");
        }
    }

    private StatusInscricao obterStatusPorAcao(String acao) {
        if (ACAO_APROVAR.equalsIgnoreCase(acao)) {
            return StatusInscricao.RELATORIO_APROVADO_PELO_SUPERVISOR;
        }
        if (ACAO_REPROVAR.equalsIgnoreCase(acao)) {
            return StatusInscricao.RELATORIO_REPROVADO;
        }
        throw new IllegalArgumentException("Ação de avaliação inválida. Use 'aprovar' ou 'reprovar'.");
    }

    public record DetalhesAvaliacaoRelatorio(Inscricao inscricao, RelatorioFinal relatorio) {
    }
}
