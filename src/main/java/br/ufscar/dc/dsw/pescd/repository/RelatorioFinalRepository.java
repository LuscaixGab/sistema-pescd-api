package br.ufscar.dc.dsw.pescd.repository;

import br.ufscar.dc.dsw.pescd.model.RelatorioFinal;
import br.ufscar.dc.dsw.pescd.model.Inscricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RelatorioFinalRepository extends JpaRepository<RelatorioFinal, UUID> {
    Optional<RelatorioFinal> findByInscricao(Inscricao inscricao);
}