package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.dto.SupervisaoAlunoResumo;
import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.PlanoTrabalho;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.PlanoTrabalhoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupervisaoProfessorService {

    private final PlanoTrabalhoRepository planoTrabalhoRepository;

    public SupervisaoProfessorService(PlanoTrabalhoRepository planoTrabalhoRepository) {
        this.planoTrabalhoRepository = planoTrabalhoRepository;
    }

    @Transactional(readOnly = true)
    public List<SupervisaoAlunoResumo> listarAlunosSupervisionados(Usuario professorSupervisor) {
        return planoTrabalhoRepository.findByProfessorSupervisor(professorSupervisor)
                .stream()
                .map(this::toResumo)
                .toList();
    }

    private SupervisaoAlunoResumo toResumo(PlanoTrabalho planoTrabalho) {
        Inscricao inscricao = planoTrabalho.getInscricao();
        StatusInscricao status = inscricao.getStatus();

        SupervisaoAlunoResumo resumo = new SupervisaoAlunoResumo();
        resumo.setInscricaoId(inscricao.getId());
        resumo.setPlanoId(planoTrabalho.getId());
        resumo.setNomeOferta(inscricao.getOferta().getNomeOferta());
        resumo.setSemestre(inscricao.getOferta().getSemestre());
        resumo.setNomeAluno(inscricao.getAluno().getNomeCompleto());
        resumo.setNomeDisciplina(planoTrabalho.getNomeDisciplina());
        resumo.setStatus(status);
        resumo.setPodeAvaliarPlano(status == StatusInscricao.PLANO_ENVIADO);
        resumo.setPodeAvaliarRelatorio(status == StatusInscricao.RELATORIO_ENVIADO);

        return resumo;
    }
}
