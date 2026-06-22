package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.config.MessageHelper;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Controller
public class LoginController {

    private final MessageHelper messages;

    public LoginController(MessageHelper messages) {
        this.messages = messages;
    }

    @GetMapping("/")
    public String redirecionarRaiz() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String exibirLogin(
            @RequestParam(value = "erro", required = false) String erro,
            @RequestParam(value = "logout", required = false) String logout,
            @AuthenticationPrincipal UsuarioUserDetails usuarioLogado,
            Model model) {
        if (usuarioLogado != null) {
            // Se já estiver logado, não importa o perfil, manda direto pro painel
            return "redirect:/painel";
        }

        if (erro != null) {
            model.addAttribute("mensagemErro", messages.get("login.invalid"));
        }

        if (logout != null) {
            model.addAttribute("mensagemLogout", messages.get("login.logout"));
        }

        return "telaLogin";
    }
}
