package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.config.MessageHelper;
import br.ufscar.dc.dsw.pescd.dto.EncerramentoResponsavelForm;
import br.ufscar.dc.dsw.pescd.dto.ResumoAlunoEncerramentoOferta;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.EncerramentoResponsavelService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/professor-responsavel/ofertas")
@PreAuthorize("hasRole('PROFESSOR')")
public class ProfessorResponsavelOfertaController {

    private final EncerramentoResponsavelService encerramentoResponsavelService;
    private final MessageHelper messages;

    public ProfessorResponsavelOfertaController(EncerramentoResponsavelService encerramentoResponsavelService,
                                                MessageHelper messages) {
        this.encerramentoResponsavelService = encerramentoResponsavelService;
        this.messages = messages;
    }


    @GetMapping
    public String listarOfertas(@AuthenticationPrincipal UsuarioUserDetails usuarioLogado, Model model) {
        Usuario professorResponsavel = usuarioLogado.getUsuario();
        model.addAttribute("ofertasResumo", encerramentoResponsavelService.listarResumoOfertas(professorResponsavel));
        return "professor-responsavel/ofertas";
    }

    @GetMapping("/{ofertaId}/encerrar")
    public String exibirEncerramentoOferta(@PathVariable UUID ofertaId,
                                           @AuthenticationPrincipal UsuarioUserDetails usuarioLogado,
                                           Model model,
                                           RedirectAttributes redirectAttributes) {
        try{
            Oferta oferta = encerramentoResponsavelService.buscarOfertaParaEncerramento(ofertaId, usuarioLogado.getUsuario());
            prepararModeloEncerramento(model, oferta);
            model.addAttribute("encerramentoResponsavelForm", new EncerramentoResponsavelForm());
            return "professor-responsavel/encerrar-oferta";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
            return "redirect:/professor-responsavel/ofertas";
        }
    }

    @PostMapping("/{ofertaId}/encerrar")
    public String encerrarOferta(@PathVariable UUID ofertaId,
                                 @AuthenticationPrincipal UsuarioUserDetails usuarioLogado,
                                 @Valid @ModelAttribute("encerramentoResponsavelForm")
                                     EncerramentoResponsavelForm encerramentoResponsavelForm,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes
                                 ) {
        Oferta oferta;
        try{
            oferta = encerramentoResponsavelService.buscarOfertaParaEncerramento(ofertaId, usuarioLogado.getUsuario());
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
            return "redirect:/professor-responsavel/ofertas";
        }

        if(bindingResult.hasErrors()) {
            prepararModeloEncerramento(model, oferta);
            return "professor-responsavel/encerrar-oferta";
        }

        encerramentoResponsavelService.encerrarOferta(ofertaId, encerramentoResponsavelForm, usuarioLogado.getUsuario());

        redirectAttributes.addFlashAttribute("sucesso", messages.get("msg.offer.finalized"));
        return "redirect:/professor-responsavel/ofertas";

    }

    private void prepararModeloEncerramento(Model model, Oferta oferta) {
        List<ResumoAlunoEncerramentoOferta> resumoAlunos = encerramentoResponsavelService.montarResumoAlunos(oferta);

        model.addAttribute("oferta", oferta);
        model.addAttribute("resumoAlunos", resumoAlunos);
        model.addAttribute("mediaFrequencia", encerramentoResponsavelService.calcularMediaFrequencia(resumoAlunos));
        model.addAttribute("quantidadeEstagio", encerramentoResponsavelService.contarCreditosPorEstagio(resumoAlunos));
        model.addAttribute("quantidadeDocumentacao", encerramentoResponsavelService.contarCreditosPorDocumentacao(resumoAlunos));
        model.addAttribute("quantidadeNotas", encerramentoResponsavelService.contarNotas(resumoAlunos));
    }
}
