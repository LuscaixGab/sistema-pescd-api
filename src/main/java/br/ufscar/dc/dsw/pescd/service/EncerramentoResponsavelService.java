package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.dto.EncerramentoResponsavelForm;
import br.ufscar.dc.dsw.pescd.dto.OfertaEncerramentoResponsavelResumo;
import br.ufscar.dc.dsw.pescd.dto.ResumoAlunoEncerramentoOferta;
import br.ufscar.dc.dsw.pescd.model.*;
import br.ufscar.dc.dsw.pescd.repository.DocumentacaoAulaRepository;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.OfertaRepository;
import br.ufscar.dc.dsw.pescd.repository.RelatorioFinalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class EncerramentoResponsavelService {

    private final OfertaRepository ofertaRepository;
    private final InscricaoRepository inscricaoRepository;
    private final RelatorioFinalRepository relatorioFinalRepository;
    private final DocumentacaoAulaRepository documentacaoAulaRepository;

    public EncerramentoResponsavelService(OfertaRepository ofertaRepository,
                                          InscricaoRepository inscricaoRepository,
                                          RelatorioFinalRepository relatorioFinalRepository,
                                          DocumentacaoAulaRepository documentacaoAulaRepository) {
        this.ofertaRepository = ofertaRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.relatorioFinalRepository = relatorioFinalRepository;
        this.documentacaoAulaRepository = documentacaoAulaRepository;
    }

    // listar ofertas
    @Transactional(readOnly = true)
    public List<Oferta> listarOfertas(Usuario professorResponsavel) {
        return ofertaRepository.findByProfessorResponsavelOrderByDataCriacaoDesc(professorResponsavel);
    }

    @Transactional(readOnly = true)
    public List<OfertaEncerramentoResponsavelResumo> listarResumoOfertas(Usuario professorResponsavel) {
        return listarOfertas(professorResponsavel).stream()
                .map(this::montarResumoOferta)
                .collect(Collectors.toList());
    }

    // validar se a oferta pertence ao professor logado
    @Transactional(readOnly = true)
    public Oferta buscarOfertaParaEncerramento(UUID ofertaId, Usuario professorResponsavel) {
        Oferta oferta = ofertaRepository.findById(ofertaId).orElseThrow(() -> new IllegalArgumentException("Oferta não encontrada"));
        validarProfessorResponsavel(oferta, professorResponsavel);
        validarOfertaEmAndamento(oferta);
        validarTodosAlunosConcluidos(oferta);

        return oferta;
    }
    // validar se todos os alunos estão CONCLUIDO_PELO_RESPONSAVEL
    @Transactional(readOnly = true)
    public void validarTodosAlunosConcluidos(Oferta oferta) {
        List<Inscricao> inscricoes = inscricaoRepository.findByOferta(oferta);
        if(inscricoes.isEmpty()){
            throw new IllegalArgumentException("A oferta não possui alunos inscritos");
        }

        boolean todosConcluidos = inscricoes.stream().allMatch(inscricao ->  inscricao.getStatus()== StatusInscricao.CONCLUIDO_PELO_RESPONSAVEL);

        if(!todosConcluidos){
            throw new IllegalArgumentException("Todos os alunos devem estar concluídos pelo responsável");
        }
    }

    private void validarProfessorResponsavel(Oferta oferta, Usuario professorResponsavel){
        Usuario professor = oferta.getProfessorResponsavel();

        if(!professor.getId().equals(professorResponsavel.getId())){
            throw new IllegalArgumentException("Você não é o professor responsável desta oferta");
        }
    }

    @Transactional
    public void encerrarOferta(UUID ofertaId, EncerramentoResponsavelForm form, Usuario professorResponsavel){
        Oferta oferta = buscarOfertaParaEncerramento(ofertaId, professorResponsavel);

        oferta.setStatusOferta(StatusOferta.AGUARDANDO_ENCERRAMENTO_SECRETARIO);
        oferta.setLicoesAprendidas(form.getLicoesAprendidas().trim());
        oferta.setDataEncerramentoResponsavel(LocalDateTime.now());
        ofertaRepository.save(oferta);
    }

    @Transactional(readOnly = true)
    public List<ResumoAlunoEncerramentoOferta> montarResumoAlunos(Oferta oferta) {
        return inscricaoRepository.findByOferta(oferta).stream()
                .map(this::montarResumoAluno)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<String, Long> contarNotas(List<ResumoAlunoEncerramentoOferta> resumos) {
        Map<String, Long> notas = new LinkedHashMap<>();
        notas.put("A", contarNota(resumos, "A"));
        notas.put("B", contarNota(resumos, "B"));
        notas.put("C", contarNota(resumos, "C"));
        notas.put("D", contarNota(resumos, "D"));
        notas.put("E", contarNota(resumos, "E"));
        return notas;
    }

    @Transactional(readOnly = true)
    public double calcularMediaFrequencia(List<ResumoAlunoEncerramentoOferta> resumos) {
        return resumos.stream()
                .filter(resumo -> resumo.getFrequencia() != null)
                .mapToInt(ResumoAlunoEncerramentoOferta::getFrequencia)
                .average()
                .orElse(0.0);
    }

    @Transactional(readOnly = true)
    public long contarCreditosPorEstagio(List<ResumoAlunoEncerramentoOferta> resumos) {
        return resumos.stream()
                .filter(resumo -> "Estágio".equals(resumo.getTipoCredito()))
                .count();
    }

    @Transactional(readOnly = true)
    public long contarCreditosPorDocumentacao(List<ResumoAlunoEncerramentoOferta> resumos) {
        return resumos.stream()
                .filter(resumo -> "Documentação".equals(resumo.getTipoCredito()))
                .count();
    }

    private ResumoAlunoEncerramentoOferta montarResumoAluno(Inscricao inscricao) {
        Optional<RelatorioFinal> relatorioFinal = relatorioFinalRepository.findByInscricao(inscricao);
        if (relatorioFinal.isPresent() && relatorioFinal.get().getNotaFinal() != null) {
            RelatorioFinal relatorio = relatorioFinal.get();
            return new ResumoAlunoEncerramentoOferta(
                    inscricao.getAluno().getNomeCompleto(),
                    "Estágio",
                    relatorio.getFrequenciaFinal(),
                    relatorio.getNotaFinal());
        }

        Optional<DocumentacaoAula> documentacaoAula = documentacaoAulaRepository.findByInscricaoId(inscricao.getId());
        if (documentacaoAula.isPresent() && documentacaoAula.get().getNota() != null) {
            DocumentacaoAula documentacao = documentacaoAula.get();
            return new ResumoAlunoEncerramentoOferta(
                    inscricao.getAluno().getNomeCompleto(),
                    "Documentação",
                    documentacao.getIndicadorFrequencia(),
                    documentacao.getNota());
        }

        return new ResumoAlunoEncerramentoOferta(
                inscricao.getAluno().getNomeCompleto(),
                "Não identificado",
                null,
                null);
    }

    private long contarNota(List<ResumoAlunoEncerramentoOferta> resumos, String nota) {
        return resumos.stream()
                .filter(resumo -> nota.equals(resumo.getNota()))
                .count();
    }

    private void validarOfertaEmAndamento(Oferta oferta) {
        if (oferta.getStatusOferta() != StatusOferta.EM_ANDAMENTO) {
            throw new IllegalArgumentException("A oferta não está disponível para encerramento pelo responsável.");
        }
    }

    private OfertaEncerramentoResponsavelResumo montarResumoOferta(Oferta oferta) {
        if (oferta.getStatusOferta() != StatusOferta.EM_ANDAMENTO) {
            return new OfertaEncerramentoResponsavelResumo(
                    oferta,
                    false,
                    "A oferta já foi encaminhada para encerramento ou concluída.");
        }

        List<Inscricao> inscricoes = inscricaoRepository.findByOferta(oferta);
        if (inscricoes.isEmpty()) {
            return new OfertaEncerramentoResponsavelResumo(
                    oferta,
                    false,
                    "A oferta ainda não possui alunos inscritos.");
        }

        boolean todosConcluidos = inscricoes.stream()
                .allMatch(inscricao -> inscricao.getStatus() == StatusInscricao.CONCLUIDO_PELO_RESPONSAVEL);

        if (!todosConcluidos) {
            return new OfertaEncerramentoResponsavelResumo(
                    oferta,
                    false,
                    "Ainda há alunos com pendências de relatório, documentação ou análise final.");
        }

        return new OfertaEncerramentoResponsavelResumo(oferta, true, null);
    }

}
