package br.ufscar.dc.dsw.pescd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;

import java.util.List;
import java.util.UUID;
import br.ufscar.dc.dsw.pescd.model.Usuario;

@Repository
public interface InscricaoRepository extends JpaRepository<Inscricao, UUID> {
    // Já tem CRUD completo por padrão
    
    // Busca inscrições de um determinado aluno
    List<Inscricao> findByAluno(Usuario aluno);

    List<Inscricao> findByOfertaProfessorResponsavelAndStatus(Usuario professor, StatusInscricao status);

    long countByOferta(Oferta oferta);
}