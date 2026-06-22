package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.SupervisaoProfessorService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/professor/supervisao")
@PreAuthorize("hasRole('PROFESSOR')")
public class ProfessorSupervisorAcompanhamentoController {
    private final SupervisaoProfessorService supervisaoProfessorService;

    public ProfessorSupervisorAcompanhamentoController(SupervisaoProfessorService supervisaoProfessorService) {
        this.supervisaoProfessorService = supervisaoProfessorService;
    }

    @GetMapping
    public String listarSupervisao(@AuthenticationPrincipal UsuarioUserDetails usuarioLogado, Model model) {
        model.addAttribute("alunos", supervisaoProfessorService.listarAlunosSupervisionados(usuarioLogado.getUsuario()));
        return "professor-supervisor/supervisao";
    }
}
