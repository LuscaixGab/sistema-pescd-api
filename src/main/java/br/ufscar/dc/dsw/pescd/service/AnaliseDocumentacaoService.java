package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.dto.AnaliseDocumentacaoForm;
import br.ufscar.dc.dsw.pescd.model.DocumentacaoAula;
import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.DocumentacaoAulaRepository;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AnaliseDocumentacaoService {

    private final DocumentacaoAulaRepository documentacaoAulaRepository;
    private final InscricaoRepository inscricaoRepository;
    private final LogStatusService logStatusService;

    public AnaliseDocumentacaoService(DocumentacaoAulaRepository documentacaoAulaRepository,
                                      InscricaoRepository inscricaoRepository,
                                      LogStatusService logStatusService) {
        this.documentacaoAulaRepository = documentacaoAulaRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.logStatusService = logStatusService;
    }

    @Transactional(readOnly = true)
    public List<DocumentacaoAula> listarPendentesDoProfessor(Usuario professorResponsavel) {
        return documentacaoAulaRepository.findByInscricaoOfertaProfessorResponsavelAndInscricaoStatus(
                professorResponsavel,
                StatusInscricao.DOCUMENTACAO_ENVIADA);
    }

    @Transactional(readOnly = true)
    public DocumentacaoAula buscarParaAnalise(UUID inscricaoId, Usuario professorResponsavel) {
        DocumentacaoAula documentacao = documentacaoAulaRepository.findByInscricaoId(inscricaoId)
                .orElseThrow(() -> new IllegalArgumentException("Documentação não encontrada."));

        validarProfessorResponsavel(documentacao, professorResponsavel);
        validarStatusDocumentacaoEnviada(documentacao);

        return documentacao;
    }

    @Transactional
    public void finalizarAnalise(UUID inscricaoId, AnaliseDocumentacaoForm form, Usuario professorResponsavel) {
        DocumentacaoAula documentacao = buscarParaAnalise(inscricaoId, professorResponsavel);
        Inscricao inscricao = documentacao.getInscricao();

        documentacao.setParecer(form.getParecer().trim());
        documentacao.setIndicadorFrequencia(form.getIndicadorFrequencia());
        documentacao.setNota(form.getNota());
        documentacao.setDataAnalise(LocalDateTime.now());
        documentacaoAulaRepository.save(documentacao);

        inscricao.setStatus(StatusInscricao.CONCLUIDO_PELO_RESPONSAVEL);
        inscricaoRepository.save(inscricao);

        // Dispara o log registrando o professor responsável
        logStatusService.registrarLog(inscricao, StatusInscricao.CONCLUIDO_PELO_RESPONSAVEL, professorResponsavel);
    }

    private void validarProfessorResponsavel(DocumentacaoAula documentacao, Usuario professorResponsavel) {
        Usuario responsavelDaOferta = documentacao.getInscricao().getOferta().getProfessorResponsavel();
        if (!responsavelDaOferta.getId().equals(professorResponsavel.getId())) {
            throw new IllegalArgumentException("Você não é o professor responsável desta oferta.");
        }
    }

    private void validarStatusDocumentacaoEnviada(DocumentacaoAula documentacao) {
        if (documentacao.getInscricao().getStatus() != StatusInscricao.DOCUMENTACAO_ENVIADA) {
            throw new IllegalArgumentException("A documentação não está na fase correta para análise.");
        }
    }
}
