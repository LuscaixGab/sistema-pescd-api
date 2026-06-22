package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.config.MessageHelper;
import br.ufscar.dc.dsw.pescd.model.PlanoTrabalho;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.PlanoTrabalhoService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/professor/planos")
@PreAuthorize("hasRole('PROFESSOR')")
public class ProfessorSupervisorPlanoController {

    private static final Logger logger = LoggerFactory.getLogger(ProfessorSupervisorPlanoController.class);

    private final PlanoTrabalhoService planoTrabalhoService;
    private final MessageHelper messages;

    public ProfessorSupervisorPlanoController(PlanoTrabalhoService planoTrabalhoService,
                                             MessageHelper messages) {
        this.planoTrabalhoService = planoTrabalhoService;
        this.messages = messages;
    }

    @GetMapping("/pendentes")
    public String listarPendentes() {
        return "redirect:/professor/supervisao";
    }

    @GetMapping("/{id}/avaliar")
    public String exibirFormularioAvaliacao(@PathVariable UUID id, Model model, @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        try {
            model.addAttribute("plano", planoTrabalhoService.buscarPlanoParaAvaliacao(id, usuarioLogado.getUsuario()));
            return "professor-supervisor/avaliarPlano";
        } catch (IllegalArgumentException e) {
            return "redirect:/professor/supervisao";
        }
    }

    @PostMapping("/{id}/avaliar")
    public String processarAvaliacao(@PathVariable UUID id,
                                     @RequestParam("parecer") String parecer,
                                     @RequestParam("acao") String acao,
                                     @AuthenticationPrincipal UsuarioUserDetails usuarioLogado,
                                     RedirectAttributes redirectAttributes) {
        try {
            planoTrabalhoService.avaliarPlano(id, parecer, acao, usuarioLogado.getUsuario());
            redirectAttributes.addFlashAttribute("sucesso", messages.get("msg.plan.evaluated"));
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/professor/supervisao";
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> baixarPlano(@PathVariable UUID id, @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        try {
            PlanoTrabalho plano = planoTrabalhoService.buscarPlanoParaAvaliacao(id, usuarioLogado.getUsuario());
            Path caminhoArquivo = Paths.get(plano.getArquivoPlano()).normalize();
            Resource resource = new UrlResource(caminhoArquivo.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro ao baixar plano {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
