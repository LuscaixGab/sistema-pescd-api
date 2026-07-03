package br.ufscar.dc.dsw.pescd.api.responsavel;

import br.ufscar.dc.dsw.pescd.dto.AnaliseRelatorioResponsavelForm;
import br.ufscar.dc.dsw.pescd.model.RelatorioFinal;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.AnaliseRelatorioResponsavelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/professor-responsavel/relatorios")
@PreAuthorize("hasRole('PROFESSOR')")
public class RelatorioResponsavelApiController {

    private final AnaliseRelatorioResponsavelService analiseRelatorioResponsavelService;

    public RelatorioResponsavelApiController(AnaliseRelatorioResponsavelService analiseRelatorioResponsavelService) {
        this.analiseRelatorioResponsavelService = analiseRelatorioResponsavelService;
    }

    @GetMapping
    public ResponseEntity<List<RelatorioResponsavelResponseDTO>> listarPendentes(
            @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {

        List<RelatorioResponsavelResponseDTO> resposta = analiseRelatorioResponsavelService
                .listarPendentesProfessor(usuarioLogado.getUsuario())
                .stream()
                .map(RelatorioResponsavelResponseDTO::from)
                .toList();

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{inscricaoId}")
    public ResponseEntity<RelatorioResponsavelResponseDTO> buscarParaAnalise(
            @PathVariable UUID inscricaoId,
            @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {

        RelatorioFinal relatorio = analiseRelatorioResponsavelService.buscarParaAnalise(
                inscricaoId,
                usuarioLogado.getUsuario());

        return ResponseEntity.ok(RelatorioResponsavelResponseDTO.from(relatorio));
    }

    @PostMapping("/{inscricaoId}/analise")
    public ResponseEntity<Void> finalizarAnalise(
            @PathVariable UUID inscricaoId,
            @Valid @RequestBody AnaliseRelatorioResponsavelForm form,
            @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {

        analiseRelatorioResponsavelService.finalizarAnalise(
                inscricaoId,
                form,
                usuarioLogado.getUsuario());

        return ResponseEntity.noContent().build();
    }
}
