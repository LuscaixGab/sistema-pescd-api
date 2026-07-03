package br.ufscar.dc.dsw.pescd.api;

import br.ufscar.dc.dsw.pescd.dto.OfertaRequestDTO;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.OfertaService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/ofertas")
@PreAuthorize("hasRole('SECRETARIO')")
public class OfertaApiController {

    private final OfertaService ofertaService;

    public OfertaApiController(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
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
}
