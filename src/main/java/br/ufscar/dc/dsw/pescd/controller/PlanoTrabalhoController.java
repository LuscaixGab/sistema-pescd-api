package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.dto.PlanoTrabalhoForm;
import br.ufscar.dc.dsw.pescd.service.PlanoTrabalhoService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/aluno/plano-trabalho")
@PreAuthorize("hasRole('ALUNO')")
public class PlanoTrabalhoController {

    private final PlanoTrabalhoService planoTrabalhoService;

    public PlanoTrabalhoController(PlanoTrabalhoService planoTrabalhoService) {
        this.planoTrabalhoService = planoTrabalhoService;
    }

    @GetMapping("/adicionar")
    public String exibirFormulario(Model model) {
        model.addAttribute("planoTrabalhoForm", new PlanoTrabalhoForm());
        model.addAttribute("professores", planoTrabalhoService.listarProfessores());
        return "aluno/adicionar-plano-trabalho";
    }

    @PostMapping("/adicionar")
    public String adicionarPlanoTrabalho(@Valid @ModelAttribute("planoTrabalhoForm") PlanoTrabalhoForm planoTrabalhoForm,
                                         BindingResult bindingResult,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        if (planoTrabalhoForm.getArquivoPlano() == null || planoTrabalhoForm.getArquivoPlano().isEmpty()) {
            bindingResult.rejectValue("arquivoPlano", "arquivoPlano.vazio",
                    "Selecione um arquivo PDF do plano.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("professores", planoTrabalhoService.listarProfessores());
            return "aluno/adicionar-plano-trabalho";
        }

        try {
            planoTrabalhoService.criarPlanoTrabalho(planoTrabalhoForm);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Plano de trabalho enviado com sucesso.");
            return "redirect:/painel";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            model.addAttribute("erroGeral", exception.getMessage());
            model.addAttribute("professores", planoTrabalhoService.listarProfessores());
            return "aluno/adicionar-plano-trabalho";
        }
    }
}
