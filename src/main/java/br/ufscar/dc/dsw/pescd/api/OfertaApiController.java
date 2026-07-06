package br.ufscar.dc.dsw.pescd.api;

import br.ufscar.dc.dsw.pescd.dto.OfertaRequestDTO;
import br.ufscar.dc.dsw.pescd.dto.OfertaResponseDTO;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.InscricaoService;
import br.ufscar.dc.dsw.pescd.service.OfertaService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ofertas")
public class OfertaApiController {

    private final OfertaService ofertaService;
    private final InscricaoService inscricaoService;

    public OfertaApiController(OfertaService ofertaService, InscricaoService inscricaoService) {
        this.ofertaService = ofertaService;
        this.inscricaoService = inscricaoService;
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

    @PostMapping("/{id}/encerrar-oficialmente")
    @PreAuthorize("hasAnyRole('SECRETARIO', 'PROFESSOR')")
    public ResponseEntity<Void> encerrarOfertaOficialmente(@PathVariable UUID id,
                                                           @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        ofertaService.encerrarOfertaOficialmente(id, usuarioLogado.getUsuario());
        return ResponseEntity.noContent().build();
    }

    // Adiciona alunos passando o ID deles no Postman
    @PostMapping("/{id}/alunos")
    @PreAuthorize("hasRole('SECRETARIO')")
    public ResponseEntity<Void> adicionarAlunos(@PathVariable UUID id,
                                                @RequestBody List<UUID> alunoIds,
                                                @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        ofertaService.adicionarAlunos(id, alunoIds, usuarioLogado.getUsuario());
        return ResponseEntity.ok().build();
    }

    // Adiciona alunos por upload de csv
    @PostMapping(value = "/{id}/alunos/csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SECRETARIO')")
    public ResponseEntity<Void> adicionarAlunosPorCsv(
            @PathVariable UUID id,
            @RequestParam("arquivo") MultipartFile arquivo) throws Exception {

        inscricaoService.processarAlunosCsv(id, arquivo);

        return ResponseEntity.ok().build();
    }
}