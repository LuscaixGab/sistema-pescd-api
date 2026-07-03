package br.ufscar.dc.dsw.pescd.api;

import br.ufscar.dc.dsw.pescd.exception.RegraNegocioException;
import br.ufscar.dc.dsw.pescd.exception.PlanoTrabalhoNaoEncontradoException;
import br.ufscar.dc.dsw.pescd.exception.UsuarioNaoEncontradoException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.core.AuthenticationException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice(basePackages = "br.ufscar.dc.dsw.pescd.api")
public class ApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> tratarValidacao(MethodArgumentNotValidException exception,
                                                            HttpServletRequest request) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            erros.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return ResponseEntity.badRequest()
                .body(new ApiErrorResponse(
                        Instant.now().toString(),
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        "Falha de validacao nos dados enviados.",
                        request.getRequestURI(),
                        erros));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> tratarJsonInvalido(HttpMessageNotReadableException exception,
                                                               HttpServletRequest request) {
        return construirResposta(HttpStatus.BAD_REQUEST,
                "JSON invalido ou campo com valor incorreto.",
                request.getRequestURI(),
                Map.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> tratarParametroAusente(MissingServletRequestParameterException exception,
                                                                   HttpServletRequest request) {
        return construirResposta(HttpStatus.BAD_REQUEST,
                "Parametro obrigatorio ausente: " + exception.getParameterName(),
                request.getRequestURI(),
                Map.of());
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ApiErrorResponse> tratarParteAusente(MissingServletRequestPartException exception,
                                                               HttpServletRequest request) {
        return construirResposta(HttpStatus.BAD_REQUEST,
                "Parte obrigatoria ausente: " + exception.getRequestPartName(),
                request.getRequestURI(),
                Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> tratarTipoInvalido(MethodArgumentTypeMismatchException exception,
                                                               HttpServletRequest request) {
        String mensagem = "Parametro invalido na rota.";
        if ("id".equals(exception.getName())) {
            mensagem = "O id informado nao tem formato de UUID valido.";
        }
        return construirResposta(HttpStatus.BAD_REQUEST, mensagem, request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> tratarNaoEncontrado(UsuarioNaoEncontradoException exception,
                                                                HttpServletRequest request) {
        return construirResposta(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(PlanoTrabalhoNaoEncontradoException.class)
    public ResponseEntity<ApiErrorResponse> tratarPlanoNaoEncontrado(PlanoTrabalhoNaoEncontradoException exception,
                                                                     HttpServletRequest request) {
        return construirResposta(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ApiErrorResponse> tratarRegraNegocio(RegraNegocioException exception,
                                                               HttpServletRequest request) {
        return construirResposta(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> tratarArgumentoInvalido(IllegalArgumentException exception,
                                                                    HttpServletRequest request) {
        return construirResposta(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI(), Map.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> tratarAcessoNegado(AccessDeniedException exception,
                                                               HttpServletRequest request) {
        return construirResposta(HttpStatus.FORBIDDEN,
                "Voce nao possui permissao para acessar este recurso.",
                request.getRequestURI(),
                Map.of());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiErrorResponse> tratarFalhaAutenticacao(AuthenticationException exception,
                                                                    HttpServletRequest request) {
        return construirResposta(HttpStatus.UNAUTHORIZED,
                "Usuario ou senha invalidos.",
                request.getRequestURI(),
                Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> tratarViolacaoDeIntegridade(DataIntegrityViolationException exception,
                                                                        HttpServletRequest request) {
        logger.warn("Falha de integridade em {} {}", request.getMethod(), request.getRequestURI(), exception);
        return construirResposta(HttpStatus.CONFLICT,
                "Violacao de integridade ao processar a requisicao.",
                request.getRequestURI(),
                Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> tratarErroInesperado(Exception exception,
                                                                 HttpServletRequest request) {
        logger.error("Erro inesperado em {} {}", request.getMethod(), request.getRequestURI(), exception);
        return construirResposta(HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro inesperado ao processar a requisicao.",
                request.getRequestURI(),
                Map.of());
    }

    private ResponseEntity<ApiErrorResponse> construirResposta(HttpStatus status,
                                                               String mensagem,
                                                               String path,
                                                               Map<String, String> fieldErrors) {
        return ResponseEntity.status(status)
                .body(new ApiErrorResponse(
                        Instant.now().toString(),
                        status.value(),
                        status.getReasonPhrase(),
                        mensagem,
                        path,
                        fieldErrors));
    }
}
