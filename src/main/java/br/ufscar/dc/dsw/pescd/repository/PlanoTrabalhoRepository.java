package br.ufscar.dc.dsw.pescd.repository;

import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.ufscar.dc.dsw.pescd.model.PlanoTrabalho;

import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface PlanoTrabalhoRepository extends JpaRepository<PlanoTrabalho, UUID> {
    List<PlanoTrabalho> findAllByOrderByDataCriacaoDesc();

    Optional<PlanoTrabalho> findByInscricao(Inscricao inscricao);

    List<PlanoTrabalho> findByProfessorSupervisorAndInscricaoStatus(
            Usuario professor,
            StatusInscricao status
    );

    List<PlanoTrabalho> findByProfessorSupervisor(
            Usuario professorSupervisor
    );
}