package br.ufscar.dc.dsw.pescd.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("hasRole('PROFESSOR')")
public class ProfessorPainelController {

    @GetMapping("/professor-responsavel/painel")
    public String painelResponsavel() {
        return "professor-responsavel/painel";
    }

    @GetMapping("/professor-supervisor/painel")
    public String painelSupervisor() {
        return "redirect:/professor/supervisao";
    }
}
