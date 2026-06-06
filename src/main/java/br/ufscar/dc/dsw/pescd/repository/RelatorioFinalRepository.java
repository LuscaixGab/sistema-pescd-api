package br.ufscar.dc.dsw.pescd.repository;

import br.ufscar.dc.dsw.pescd.model.RelatorioFinal;
import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RelatorioFinalRepository extends JpaRepository<RelatorioFinal, UUID> {
    Optional<RelatorioFinal> findByInscricao(Inscricao inscricao);
    List<RelatorioFinal> findByInscricaoOfertaProfessorResponsavelAndInscricaoStatus(Usuario professorResponsavel, StatusInscricao status);
    Optional<RelatorioFinal> findByInscricaoId(UUID inscricaoId);
}