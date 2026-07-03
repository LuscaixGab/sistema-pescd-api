package br.ufscar.dc.dsw.pescd.api;

import br.ufscar.dc.dsw.pescd.dto.LoginRequestDTO;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;

    public AuthApiController(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest,
                                                  HttpServletRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getNomeUsuario(),
                        loginRequest.getSenha()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context);

        UsuarioUserDetails usuarioUserDetails = (UsuarioUserDetails) authentication.getPrincipal();
        LoginResponseDTO resposta = LoginResponseDTO.from(
                usuarioUserDetails.getUsuario(),
                obterDestinoPorPerfil(authentication));

        return ResponseEntity.ok(resposta);
    }

    private String obterDestinoPorPerfil(Authentication authentication) {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PROFESSOR"))) {
            return "/professor/atuacao";
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMINISTRADOR"))) {
            return "/painel";
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SECRETARIO"))) {
            return "/painel";
        }
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ALUNO"))) {
            return "/painel";
        }
        return "/painel";
    }
}
