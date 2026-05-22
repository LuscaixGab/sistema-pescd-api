package br.ufscar.dc.dsw.pescd.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import br.ufscar.dc.dsw.pescd.model.Inscricao;

@Repository
public interface InscricaoRepository extends JpaRepository<Inscricao, Long> {
}