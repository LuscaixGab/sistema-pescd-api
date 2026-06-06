package br.ufscar.dc.dsw.pescd.repository;

import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfertaRepository extends JpaRepository<Oferta, UUID> {
    // Já tem CRUD completo por padrão

    List<Oferta> findByProfessorResponsavel(Usuario professor);

    List<Oferta> findAllByOrderByDataCriacaoDesc();

    List<Oferta> findAllByOrderBySemestreDesc();
}
