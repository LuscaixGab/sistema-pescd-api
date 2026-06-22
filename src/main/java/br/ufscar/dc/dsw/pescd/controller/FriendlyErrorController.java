package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.config.MessageHelper;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FriendlyErrorController implements ErrorController {

    private static final Logger logger = LoggerFactory.getLogger(FriendlyErrorController.class);

    private final MessageHelper messages;

    public FriendlyErrorController(MessageHelper messages) {
        this.messages = messages;
    }

    @RequestMapping("/error")
    public String tratarErroPadrao(HttpServletRequest request, Model model) {
        int status = obterStatus(request);
        String path = obterPath(request);
        Throwable exception = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        if (status >= 500) {
            logger.error("Erro HTTP {} em {}", status, path, exception);
        } else {
            logger.warn("Erro HTTP {} em {}", status, path, exception);
        }

        preencherModelo(model, status, path);
        return "error/friendly-error";
    }

    @GetMapping("/erro/403")
    public String acessoNegado(HttpServletRequest request, Model model) {
        String path = request.getRequestURI();
        logger.warn("Acesso negado encaminhado para pagina amigavel: {}", path);
        preencherModelo(model, HttpStatus.FORBIDDEN.value(), path);
        return "error/friendly-error";
    }

    private int obterStatus(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status instanceof Integer statusCode) {
            return statusCode;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private String obterPath(HttpServletRequest request) {
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        return path == null ? request.getRequestURI() : path.toString();
    }

    private void preencherModelo(Model model, int status, String path) {
        model.addAttribute("status", status);
        model.addAttribute("path", path);
        if (status == HttpStatus.FORBIDDEN.value()) {
            model.addAttribute("titulo", messages.get("error.forbidden.title"));
            model.addAttribute("mensagem", messages.get("error.forbidden.message"));
        } else if (status == HttpStatus.NOT_FOUND.value()) {
            model.addAttribute("titulo", messages.get("error.notFound.title"));
            model.addAttribute("mensagem", messages.get("error.notFound.message"));
        } else if (status == HttpStatus.CONFLICT.value()) {
            model.addAttribute("titulo", messages.get("error.conflict.title"));
            model.addAttribute("mensagem", messages.get("error.conflict.message"));
        } else {
            model.addAttribute("titulo", messages.get("error.internal.title"));
            model.addAttribute("mensagem", messages.get("error.internal.message"));
        }
    }
}
