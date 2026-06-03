package br.ufscar.dc.dsw.pescd.repository;

import br.ufscar.dc.dsw.pescd.model.Inscricao;
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
}