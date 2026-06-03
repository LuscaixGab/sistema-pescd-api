package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.dto.AnaliseDocumentacaoForm;
import br.ufscar.dc.dsw.pescd.model.DocumentacaoAula;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.AnaliseDocumentacaoService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/professor-responsavel/documentacoes")
@PreAuthorize("hasRole('PROFESSOR')")
public class ProfessorResponsavelDocumentacaoController {

    private final AnaliseDocumentacaoService analiseDocumentacaoService;

    public ProfessorResponsavelDocumentacaoController(AnaliseDocumentacaoService analiseDocumentacaoService) {
        this.analiseDocumentacaoService = analiseDocumentacaoService;
    }

    @GetMapping
    public String listarPendentes(@AuthenticationPrincipal UsuarioUserDetails usuarioLogado, Model model) {
        Usuario professorResponsavel = usuarioLogado.getUsuario();
        model.addAttribute("documentacoes",
                analiseDocumentacaoService.listarPendentesDoProfessor(professorResponsavel));
        return "professor-responsavel/documentacoes";
    }

    @GetMapping("/{inscricaoId}/analisar")
    public String exibirAnalise(@PathVariable UUID inscricaoId,
                                @AuthenticationPrincipal UsuarioUserDetails usuarioLogado,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            DocumentacaoAula documentacao = analiseDocumentacaoService.buscarParaAnalise(
                    inscricaoId,
                    usuarioLogado.getUsuario());

            model.addAttribute("documentacao", documentacao);
            model.addAttribute("analiseDocumentacaoForm", new AnaliseDocumentacaoForm());
            return "professor-responsavel/analisar-documentacao";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
            return "redirect:/professor-responsavel/documentacoes";
        }
    }

    @PostMapping("/{inscricaoId}/analisar")
    public String finalizarAnalise(@PathVariable UUID inscricaoId,
                                   @AuthenticationPrincipal UsuarioUserDetails usuarioLogado,
                                   @Valid @ModelAttribute("analiseDocumentacaoForm")
                                           AnaliseDocumentacaoForm analiseDocumentacaoForm,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        DocumentacaoAula documentacao;
        try {
            documentacao = analiseDocumentacaoService.buscarParaAnalise(inscricaoId, usuarioLogado.getUsuario());
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
            return "redirect:/professor-responsavel/documentacoes";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("documentacao", documentacao);
            return "professor-responsavel/analisar-documentacao";
        }

        analiseDocumentacaoService.finalizarAnalise(
                inscricaoId,
                analiseDocumentacaoForm,
                usuarioLogado.getUsuario());

        redirectAttributes.addFlashAttribute("sucesso", "Documentação analisada com sucesso.");
        return "redirect:/professor-responsavel/documentacoes";
    }
}
