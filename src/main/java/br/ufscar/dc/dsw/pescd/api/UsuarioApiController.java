package br.ufscar.dc.dsw.pescd.api;

import br.ufscar.dc.dsw.pescd.dto.AdministradorDTO;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.AdministradorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioApiController {

    private final AdministradorService administradorService;

    public UsuarioApiController(AdministradorService administradorService) {
        this.administradorService = administradorService;
    }

    @GetMapping
    public ResponseEntity<List<AdministradorDTO>> listarUsuarios() {
        return ResponseEntity.ok(administradorService.listarUsuariosDTO());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdministradorDTO> buscarUsuario(@PathVariable UUID id) {
        return ResponseEntity.ok(administradorService.buscarUsuarioDTO(id));
    }

    @PostMapping
    public ResponseEntity<AdministradorDTO> criarUsuario(@Valid @RequestBody AdministradorDTO administradorDTO) {
        Usuario usuario = administradorService.criarUsuario(administradorDTO);
        AdministradorDTO resposta = administradorService.buscarUsuarioDTO(usuario.getId());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(usuario.getId())
                .toUri();

        return ResponseEntity.created(location).body(resposta);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdministradorDTO> atualizarUsuario(@PathVariable UUID id,
                                                            @Valid @RequestBody AdministradorDTO administradorDTO) {
        administradorService.atualizarUsuario(id, administradorDTO);
        return ResponseEntity.ok(administradorService.buscarUsuarioDTO(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluirUsuario(@PathVariable UUID id,
                                               @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        administradorService.excluirUsuario(id, usuarioLogado.getUsuario().getId());
        return ResponseEntity.noContent().build();
    }
}
