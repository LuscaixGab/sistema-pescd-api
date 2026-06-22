package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.config.MessageHelper;
import br.ufscar.dc.dsw.pescd.dto.AnaliseRelatorioResponsavelForm;
import br.ufscar.dc.dsw.pescd.model.RelatorioFinal;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.AnaliseRelatorioResponsavelService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/professor-responsavel/relatorios")
@PreAuthorize("hasRole('PROFESSOR')")
public class ProfessorResponsavelRelatorioController {
    private static final Logger logger = LoggerFactory.getLogger(ProfessorResponsavelRelatorioController.class);

    private final AnaliseRelatorioResponsavelService analiseRelatorioResponsavelService;
    private final MessageHelper messages;

    public ProfessorResponsavelRelatorioController(AnaliseRelatorioResponsavelService analiseRelatorioResponsavelService,
                                                   MessageHelper messages) {
        this.analiseRelatorioResponsavelService = analiseRelatorioResponsavelService;
        this.messages = messages;
    }

    @GetMapping
    public String listarPendentes(@AuthenticationPrincipal UsuarioUserDetails usuarioLogado, Model model) {
        Usuario professorResponsavel = usuarioLogado.getUsuario();
        model.addAttribute("relatorios", analiseRelatorioResponsavelService.listarPendentesProfessor(professorResponsavel));
        return "professor-responsavel/relatorios";
    }

    @GetMapping("/{inscricaoId}/analisar")
    public String exibirAnalise(@PathVariable UUID inscricaoId,
                                @AuthenticationPrincipal UsuarioUserDetails usuarioLogado,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            RelatorioFinal relatorioFinal = analiseRelatorioResponsavelService.buscarParaAnalise(
                    inscricaoId,
                    usuarioLogado.getUsuario());

            model.addAttribute("relatorio", relatorioFinal);
            model.addAttribute("plano", analiseRelatorioResponsavelService.buscarPlanoDoRelatorio(relatorioFinal).orElse(null));
            model.addAttribute("analiseRelatorioForm", new AnaliseRelatorioResponsavelForm());
            return "professor-responsavel/analisar-relatorio";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
            return "redirect:/professor-responsavel/relatorios";
        }
    }

    @PostMapping("/{inscricaoId}/analisar")
    public String finalizarAnalise(@PathVariable UUID inscricaoId,
                                   @AuthenticationPrincipal UsuarioUserDetails usuarioLogado,
                                   @Valid @ModelAttribute("analiseRelatorioForm")
                                   AnaliseRelatorioResponsavelForm analiseRelatorioResponsavelForm,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        RelatorioFinal relatorioFinal;
        try {
            relatorioFinal = analiseRelatorioResponsavelService.buscarParaAnalise(inscricaoId, usuarioLogado.getUsuario());
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("erro", exception.getMessage());
            return "redirect:/professor-responsavel/relatorios";
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("relatorio", relatorioFinal);
            model.addAttribute("plano", analiseRelatorioResponsavelService.buscarPlanoDoRelatorio(relatorioFinal).orElse(null));
            return "professor-responsavel/analisar-relatorio";
        }

        analiseRelatorioResponsavelService.finalizarAnalise(
                inscricaoId,
                analiseRelatorioResponsavelForm,
                usuarioLogado.getUsuario());

        redirectAttributes.addFlashAttribute("sucesso", messages.get("msg.report.analyzed"));
        return "redirect:/professor-responsavel/relatorios";
    }

    @GetMapping("/{inscricaoId}/download")
    public ResponseEntity<Resource> baixarRelatorio(@PathVariable UUID inscricaoId,
                                                    @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        RelatorioFinal relatorioFinal = analiseRelatorioResponsavelService.buscarParaAnalise(
                inscricaoId,
                usuarioLogado.getUsuario());

        try {
            Path caminhoArquivo = Paths.get("uploads/relatorios/")
                    .toAbsolutePath()
                    .resolve(relatorioFinal.getArquivoRelatorio())
                    .normalize();
            Resource resource = new UrlResource(caminhoArquivo.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception exception) {
            logger.error("Erro ao baixar relatorio da inscricao {}", inscricaoId, exception);
            return ResponseEntity.internalServerError().build();
        }
    }
}
