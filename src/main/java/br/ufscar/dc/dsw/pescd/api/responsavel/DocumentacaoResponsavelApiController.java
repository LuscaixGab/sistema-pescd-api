package br.ufscar.dc.dsw.pescd.api.responsavel;

import br.ufscar.dc.dsw.pescd.dto.AnaliseDocumentacaoForm;
import br.ufscar.dc.dsw.pescd.model.DocumentacaoAula;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.AnaliseDocumentacaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/professor-responsavel/documentacoes")
@PreAuthorize("hasRole('PROFESSOR')")
public class DocumentacaoResponsavelApiController {

    private final AnaliseDocumentacaoService analiseDocumentacaoService;

    public DocumentacaoResponsavelApiController(AnaliseDocumentacaoService analiseDocumentacaoService) {
        this.analiseDocumentacaoService = analiseDocumentacaoService;
    }

    @GetMapping
    public ResponseEntity<List<DocumentacaoResponsavelResponseDTO>> listarPendentes(
            @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {

        List<DocumentacaoResponsavelResponseDTO> resposta = analiseDocumentacaoService
                .listarPendentesDoProfessor(usuarioLogado.getUsuario())
                .stream()
                .map(DocumentacaoResponsavelResponseDTO::from)
                .toList();

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{inscricaoId}")
    public ResponseEntity<DocumentacaoResponsavelResponseDTO> buscarParaAnalise(
            @PathVariable UUID inscricaoId,
            @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {

        DocumentacaoAula documentacao = analiseDocumentacaoService.buscarParaAnalise(
                inscricaoId,
                usuarioLogado.getUsuario());

        return ResponseEntity.ok(DocumentacaoResponsavelResponseDTO.from(documentacao));
    }

    @PostMapping("/{inscricaoId}/analise")
    public ResponseEntity<Void> finalizarAnalise(
            @PathVariable UUID inscricaoId,
            @Valid @RequestBody AnaliseDocumentacaoForm form,
            @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {

        analiseDocumentacaoService.finalizarAnalise(
                inscricaoId,
                form,
                usuarioLogado.getUsuario());

        return ResponseEntity.noContent().build();
    }
}
