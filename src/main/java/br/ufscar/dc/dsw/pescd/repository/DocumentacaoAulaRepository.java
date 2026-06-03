package br.ufscar.dc.dsw.pescd.repository;

import br.ufscar.dc.dsw.pescd.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentacaoAulaRepository extends JpaRepository<DocumentacaoAula, UUID> {
    // Já tem CRUD completo por padrão
    List<DocumentacaoAula> findByInscricaoOfertaProfessorResponsavelAndInscricaoStatus(Usuario professorResponsavel, StatusInscricao status);
    Optional<DocumentacaoAula> findByInscricaoId(UUID inscricaoId);
}