package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.dto.AnaliseRelatorioResponsavelForm;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnaliseRelatorioResponsavelService {

    private final RelatorioFinalRepository relatorioFinalRepository;
    private final InscricaoRepository inscricaoRepository;
    private final PlanoTrabalhoRepository planoTrabalhoRepository;

    public AnaliseRelatorioResponsavelService(RelatorioFinalRepository relatorioFinalRepository,
                                              InscricaoRepository inscricaoRepository,
                                              PlanoTrabalhoRepository planoTrabalhoRepository) {
        this.relatorioFinalRepository = relatorioFinalRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.planoTrabalhoRepository = planoTrabalhoRepository;
    }

    @Transactional(readOnly = true)
    public List<RelatorioFinal> listarPendentesProfessor(Usuario professorResponsavel){
        return relatorioFinalRepository.findByInscricaoOfertaProfessorResponsavelAndInscricaoStatus(
                professorResponsavel,
                StatusInscricao.RELATORIO_APROVADO_PELO_SUPERVISOR);
    }

    @Transactional(readOnly = true)
    public RelatorioFinal buscarParaAnalise(UUID inscricaoId, Usuario professorResponsavel){
        RelatorioFinal relatorioFinal = relatorioFinalRepository.findByInscricaoId(inscricaoId).orElseThrow(() -> new IllegalArgumentException("Relatório não encontrado"));
        validarProfessorResponsavel(relatorioFinal, professorResponsavel);
        validarStatus(relatorioFinal);
        return relatorioFinal;
    }

    @Transactional(readOnly = true)
    public Optional<PlanoTrabalho> buscarPlanoDoRelatorio(RelatorioFinal relatorioFinal) {
        return planoTrabalhoRepository.findByInscricao(relatorioFinal.getInscricao());
    }

    @Transactional
    public void finalizarAnalise(UUID inscricaoId, AnaliseRelatorioResponsavelForm form, Usuario professorResponsavel){
        RelatorioFinal relatorioFinal = buscarParaAnalise(inscricaoId, professorResponsavel);
        Inscricao inscricao = relatorioFinal.getInscricao();

        relatorioFinal.setParecerResponsavel(form.getParecer());
        relatorioFinal.setFrequenciaFinal(form.getIndicadorFrequencia());
        relatorioFinal.setNotaFinal(form.getNota());
        relatorioFinal.setDataAnaliseResponsavel(LocalDateTime.now());
        relatorioFinalRepository.save(relatorioFinal);

        inscricao.setStatus(StatusInscricao.CONCLUIDO_PELO_RESPONSAVEL);
        inscricaoRepository.save(inscricao);
    }

    private void validarProfessorResponsavel(RelatorioFinal relatorioFinal, Usuario professorResponsavel){
        Usuario professor = relatorioFinal.getInscricao().getOferta().getProfessorResponsavel();

        if(!professor.getId().equals(professorResponsavel.getId())){
            throw new IllegalArgumentException("Você não é o professor responsável desta oferta");
        }
    }

    private void validarStatus(RelatorioFinal relatorioFinal){
        if(relatorioFinal.getInscricao().getStatus()!= StatusInscricao.RELATORIO_APROVADO_PELO_SUPERVISOR){
            throw new IllegalArgumentException("O relatório não está na fase de análise");
        }
    }
}
