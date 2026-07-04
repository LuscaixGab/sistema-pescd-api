package br.ufscar.dc.dsw.pescd.api;

import br.ufscar.dc.dsw.pescd.dto.OfertaRequestDTO;
import br.ufscar.dc.dsw.pescd.dto.OfertaResponseDTO;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.OfertaService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ofertas")
public class OfertaApiController {

    private final OfertaService ofertaService;

    public OfertaApiController(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SECRETARIO')")
    public ResponseEntity<OfertaResponseDTO> criarOferta(@Valid @RequestBody OfertaRequestDTO ofertaRequest,
                                                         @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        Oferta oferta = ofertaService.criarOferta(ofertaRequest, usuarioLogado.getUsuario());
        OfertaResponseDTO resposta = OfertaResponseDTO.from(oferta);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(oferta.getId())
                .toUri();

        return ResponseEntity.created(location).body(resposta);
    }

    @GetMapping
    @PreAuthorize("hasRole('SECRETARIO')")
    public ResponseEntity<List<OfertaResponseDTO>> listarOfertas() {
        return ResponseEntity.ok(ofertaService.listarOfertas().stream()
                .map(OfertaResponseDTO::from)
                .toList());
    }

    @GetMapping("/professor")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<List<OfertaResponseDTO>> listarOfertasProfessorResponsavel(@AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        return ResponseEntity.ok(ofertaService.listarOfertasParaProfessorResponsavel(usuarioLogado.getUsuario()).stream()
                .map(OfertaResponseDTO::from)
                .toList());
    }

    @PostMapping("/{id}/encerrar-oficialmente")
    @PreAuthorize("hasRole('SECRETARIO')")
    public ResponseEntity<Void> encerrarOfertaOficialmente(@PathVariable UUID id, @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        ofertaService.encerrarOfertaOficialmente(id, usuarioLogado.getUsuario());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/encerrar")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Void> encerrarOferta(@PathVariable UUID id, @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        ofertaService.encerrarOfertaOficialmente(id, usuarioLogado.getUsuario());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/alunos")
    @PreAuthorize("hasRole('SECRETARIO')")
    public ResponseEntity<Void> adicionarAlunos(@PathVariable UUID id,
                                                @RequestBody List<UUID> alunoIds,
                                                @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        ofertaService.adicionarAlunos(id, alunoIds, usuarioLogado.getUsuario());
        return ResponseEntity.ok().build();
    }
}
