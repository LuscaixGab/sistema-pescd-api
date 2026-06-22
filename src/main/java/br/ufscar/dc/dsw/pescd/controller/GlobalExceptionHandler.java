package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.config.MessageHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageHelper messages;

    public GlobalExceptionHandler(MessageHelper messages) {
        this.messages = messages;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String tratarViolacaoDeIntegridade(DataIntegrityViolationException exception,
                                              HttpServletRequest request,
                                              Model model) {
        logger.warn("Falha de integridade ao processar {} {}", request.getMethod(), request.getRequestURI(), exception);
        preencherModelo(model,
                HttpStatus.CONFLICT.value(),
                messages.get("error.conflict.title"),
                messages.get("error.conflict.message"),
                request.getRequestURI());
        return "error/friendly-error";
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String tratarAcessoNegado(AccessDeniedException exception,
                                     HttpServletRequest request,
                                     Model model) {
        logger.warn("Acesso negado em {} {}", request.getMethod(), request.getRequestURI(), exception);
        preencherModelo(model,
                HttpStatus.FORBIDDEN.value(),
                messages.get("error.forbidden.title"),
                messages.get("error.forbidden.message"),
                request.getRequestURI());
        return "error/friendly-error";
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String tratarNaoEncontrado(NoHandlerFoundException exception,
                                      HttpServletRequest request,
                                      Model model) {
        logger.warn("Recurso nao encontrado: {} {}", request.getMethod(), request.getRequestURI(), exception);
        preencherModelo(model,
                HttpStatus.NOT_FOUND.value(),
                messages.get("error.notFound.title"),
                messages.get("error.notFound.message"),
                request.getRequestURI());
        return "error/friendly-error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String tratarErroInesperado(Exception exception,
                                      HttpServletRequest request,
                                      Model model) {
        logger.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), exception);
        preencherModelo(model,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                messages.get("error.internal.title"),
                messages.get("error.internal.message"),
                request.getRequestURI());
        return "error/friendly-error";
    }

    private void preencherModelo(Model model, int status, String titulo, String mensagem, String path) {
        model.addAttribute("status", status);
        model.addAttribute("titulo", titulo);
        model.addAttribute("mensagem", mensagem);
        model.addAttribute("path", path);
    }
}
