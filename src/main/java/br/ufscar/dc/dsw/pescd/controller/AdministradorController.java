package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.config.MessageHelper;
import br.ufscar.dc.dsw.pescd.dto.AdministradorDTO;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.AdministradorService;
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
@RequestMapping("/administrador")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class AdministradorController {

    private final AdministradorService administradorService;
    private final MessageHelper messages;

    public AdministradorController(AdministradorService administradorService, MessageHelper messages) {
        this.administradorService = administradorService;
        this.messages = messages;
    }

    @GetMapping
    public String redirecionarParaUsuarios() {
        return "redirect:/administrador/usuarios";
    }

    @GetMapping("/usuarios")
    public String listarUsuarios(@AuthenticationPrincipal UsuarioUserDetails usuarioLogado, Model model) {
        model.addAttribute("usuarios", administradorService.listarUsuarios());
        model.addAttribute("usuarioLogadoId", usuarioLogado.getUsuario().getId());
        return "administrador/usuarios-lista";
    }

    @GetMapping("/usuarios/novo")
    public String exibirFormularioNovo(Model model) {
        prepararFormulario(model, new AdministradorDTO(), "/administrador/usuarios",
                messages.get("admin.form.createTitle"),
                messages.get("admin.form.createButton"));
        return "administrador/usuario-formulario";
    }

    @PostMapping("/usuarios")
    public String criarUsuario(@Valid @ModelAttribute("administradorDTO") AdministradorDTO administradorDTO,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (administradorDTO.getSenha() == null || administradorDTO.getSenha().isBlank()) {
            bindingResult.rejectValue("senha", "validation.password.required");
        }

        if (bindingResult.hasErrors()) {
            prepararFormulario(model, administradorDTO, "/administrador/usuarios",
                    messages.get("admin.form.createTitle"),
                    messages.get("admin.form.createButton"));
            return "administrador/usuario-formulario";
        }

        try {
            Usuario usuario = administradorService.criarUsuario(administradorDTO);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    messages.get("msg.user.created", usuario.getNomeCompleto()));
            return "redirect:/administrador/usuarios";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("erroGeral", exception.getMessage());
            prepararFormulario(model, administradorDTO, "/administrador/usuarios",
                    messages.get("admin.form.createTitle"),
                    messages.get("admin.form.createButton"));
            return "administrador/usuario-formulario";
        }
    }

    @GetMapping("/usuarios/{id}/editar")
    public String exibirFormularioEdicao(@PathVariable UUID id, Model model, RedirectAttributes redirectAttributes) {
        try {
            prepararFormulario(model, administradorService.carregarFormulario(id),
                    "/administrador/usuarios/" + id + "/editar",
                    messages.get("admin.form.editTitle"),
                    messages.get("admin.form.updateButton"));
            return "administrador/usuario-formulario";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("erroGeral", exception.getMessage());
            return "redirect:/administrador/usuarios";
        }
    }

    @PostMapping("/usuarios/{id}/editar")
    public String atualizarUsuario(@PathVariable UUID id,
                                   @Valid @ModelAttribute("administradorDTO") AdministradorDTO administradorDTO,
                                   BindingResult bindingResult,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            prepararFormulario(model, administradorDTO, "/administrador/usuarios/" + id + "/editar",
                    messages.get("admin.form.editTitle"),
                    messages.get("admin.form.updateButton"));
            return "administrador/usuario-formulario";
        }

        try {
            Usuario usuario = administradorService.atualizarUsuario(id, administradorDTO);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    messages.get("msg.user.updated", usuario.getNomeCompleto()));
            return "redirect:/administrador/usuarios";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("erroGeral", exception.getMessage());
            prepararFormulario(model, administradorDTO, "/administrador/usuarios/" + id + "/editar",
                    messages.get("admin.form.editTitle"),
                    messages.get("admin.form.updateButton"));
            return "administrador/usuario-formulario";
        }
    }

    @PostMapping("/usuarios/{id}/excluir")
    public String excluirUsuario(@PathVariable UUID id,
                                 @AuthenticationPrincipal UsuarioUserDetails usuarioLogado,
                                 RedirectAttributes redirectAttributes) {
        try {
            administradorService.excluirUsuario(id, usuarioLogado.getUsuario().getId());
            redirectAttributes.addFlashAttribute("mensagemSucesso", messages.get("msg.user.deleted"));
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("erroGeral", exception.getMessage());
        }
        return "redirect:/administrador/usuarios";
    }

    private void prepararFormulario(Model model, AdministradorDTO administradorDTO, String formAction,
                                     String tituloPagina, String botaoTexto) {
        model.addAttribute("administradorDTO", administradorDTO);
        model.addAttribute("perfis", administradorService.listarPerfis());
        model.addAttribute("formAction", formAction);
        model.addAttribute("tituloPagina", tituloPagina);
        model.addAttribute("botaoTexto", botaoTexto);
    }
}
