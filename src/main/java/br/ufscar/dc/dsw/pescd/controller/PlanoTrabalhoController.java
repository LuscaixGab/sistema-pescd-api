package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.config.MessageHelper;
import br.ufscar.dc.dsw.pescd.dto.PlanoTrabalhoForm;
import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.PlanoTrabalhoService;
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
@RequestMapping("/aluno/plano-trabalho")
@PreAuthorize("hasRole('ALUNO')")
public class PlanoTrabalhoController {

    private final PlanoTrabalhoService planoTrabalhoService;
    private final InscricaoRepository inscricaoRepository;
    private final MessageHelper messages;

    public PlanoTrabalhoController(PlanoTrabalhoService planoTrabalhoService,
                                   InscricaoRepository inscricaoRepository,
                                   MessageHelper messages) {
        this.planoTrabalhoService = planoTrabalhoService;
        this.inscricaoRepository = inscricaoRepository;
        this.messages = messages;
    }

    @GetMapping("/adicionar")
    public String redirecionarSemInscricao(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("erroGeral",
                messages.get("msg.plan.missingEnrollment"));
        return "redirect:/aluno/ofertas";
    }

    @GetMapping("/adicionar/{inscricaoId}")
    public String exibirFormulario(@PathVariable UUID inscricaoId,
                                   @AuthenticationPrincipal UsuarioUserDetails usuarioLogado,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            Inscricao inscricao = carregarInscricaoDoAluno(inscricaoId, usuarioLogado);

            PlanoTrabalhoForm planoTrabalhoForm = new PlanoTrabalhoForm();
            planoTrabalhoForm.setInscricaoId(inscricao.getId());

            model.addAttribute("inscricao", inscricao);
            model.addAttribute("planoTrabalhoForm", planoTrabalhoForm);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("erroGeral", exception.getMessage());
            return "redirect:/aluno/ofertas";
        }

        model.addAttribute("professores", planoTrabalhoService.listarProfessores());
        return "aluno/adicionar-plano-trabalho";
    }

    @PostMapping("/adicionar")
    public String adicionarPlanoTrabalho(@Valid @ModelAttribute("planoTrabalhoForm") PlanoTrabalhoForm planoTrabalhoForm,
                                         BindingResult bindingResult,
                                         @AuthenticationPrincipal UsuarioUserDetails usuarioLogado,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        if (planoTrabalhoForm.getArquivoPlano() == null || planoTrabalhoForm.getArquivoPlano().isEmpty()) {
            bindingResult.rejectValue("arquivoPlano", "arquivoPlano.vazio",
                    messages.get("validation.planFile.required"));
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("professores", planoTrabalhoService.listarProfessores());
            if (planoTrabalhoForm.getInscricaoId() != null) {
                inscricaoRepository.findById(planoTrabalhoForm.getInscricaoId())
                        .ifPresent(inscricao -> model.addAttribute("inscricao", inscricao));
            }
            return "aluno/adicionar-plano-trabalho";
        }

        try {
            planoTrabalhoService.criarPlanoTrabalho(planoTrabalhoForm, usuarioLogado.getUsuario());
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    messages.get("msg.plan.sent"));
            return "redirect:/painel";
        } catch (IllegalArgumentException | IllegalStateException exception) {
            model.addAttribute("erroGeral", exception.getMessage());
            model.addAttribute("professores", planoTrabalhoService.listarProfessores());
            if (planoTrabalhoForm.getInscricaoId() != null) {
                inscricaoRepository.findById(planoTrabalhoForm.getInscricaoId())
                        .ifPresent(inscricao -> model.addAttribute("inscricao", inscricao));
            }
            return "aluno/adicionar-plano-trabalho";
        }
    }

    private Inscricao carregarInscricaoDoAluno(UUID inscricaoId, UsuarioUserDetails usuarioLogado) {
        Inscricao inscricao = inscricaoRepository.findById(inscricaoId)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição não encontrada."));

        if (!inscricao.getAluno().getId().equals(usuarioLogado.getUsuario().getId())) {
            throw new IllegalArgumentException("Você não pode acessar esta inscrição.");
        }

        return inscricao;
    }
}
