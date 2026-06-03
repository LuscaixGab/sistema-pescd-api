package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.OfertaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class VisitanteController {

    private final OfertaRepository ofertaRepository;
    private final InscricaoRepository inscricaoRepository;

    public VisitanteController(OfertaRepository ofertaRepository, InscricaoRepository inscricaoRepository) {
        this.ofertaRepository = ofertaRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @GetMapping("/ofertas-publicas")
    public String listarOfertasVisitante(Model model) {
        // Traz a lista já ordenada do banco (Semestre decrescente)
        List<Oferta> ofertas = ofertaRepository.findAllByOrderBySemestreDesc();
        
        // Conta os alunos matriculados para cada oferta
        Map<UUID, Long> contagemAlunos = new HashMap<>();
        for (Oferta oferta : ofertas) {
            contagemAlunos.put(oferta.getId(), inscricaoRepository.countByOferta(oferta));
        }

        model.addAttribute("ofertas", ofertas);
        model.addAttribute("contagemAlunos", contagemAlunos);
        
        return "visitante/ofertasPublicas"; 
    }
}