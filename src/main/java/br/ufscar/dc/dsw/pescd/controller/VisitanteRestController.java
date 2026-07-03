package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.OfertaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class VisitanteRestController {

    private final OfertaRepository ofertaRepository;
    private final InscricaoRepository inscricaoRepository;

    public VisitanteRestController(OfertaRepository ofertaRepository, InscricaoRepository inscricaoRepository) {
        this.ofertaRepository = ofertaRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @GetMapping("/api/ofertas-publicas")
    public ResponseEntity<List<Map<String, Object>>> listarOfertasVisitante() {
        
        List<Oferta> ofertas = ofertaRepository.findAllByOrderBySemestreDesc();
        
        List<Map<String, Object>> resposta = ofertas.stream().map(oferta -> {
            long totalAlunos = inscricaoRepository.countByOferta(oferta);
            
            return Map.<String, Object>of(
                "id", oferta.getId(),
                "nomeOferta", oferta.getNomeOferta(),
                "semestre", oferta.getSemestre(),
                "dataInicio", oferta.getDataInicio(),
                "dataFim", oferta.getDataFim(),
                "totalAlunosMatriculados", totalAlunos
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(resposta);
    }
}