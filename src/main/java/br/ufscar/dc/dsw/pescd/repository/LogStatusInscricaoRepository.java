package br.ufscar.dc.dsw.pescd.repository;

import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.LogStatusInscricao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LogStatusInscricaoRepository extends JpaRepository<LogStatusInscricao, UUID> {
    // Busca os logs de uma inscrição específica, ordenados do mais recente para o mais antigo
    List<LogStatusInscricao> findByInscricaoOrderByDataMudancaDesc(Inscricao inscricao);
}