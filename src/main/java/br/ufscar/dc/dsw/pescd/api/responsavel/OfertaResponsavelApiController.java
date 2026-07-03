package br.ufscar.dc.dsw.pescd.api.responsavel;

import br.ufscar.dc.dsw.pescd.dto.EncerramentoResponsavelForm;
import br.ufscar.dc.dsw.pescd.dto.ResumoAlunoEncerramentoOferta;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.EncerramentoResponsavelService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/professor-responsavel/ofertas")
@PreAuthorize("hasRole('PROFESSOR')")
public class OfertaResponsavelApiController {

    private final EncerramentoResponsavelService encerramentoResponsavelService;

    public OfertaResponsavelApiController(EncerramentoResponsavelService encerramentoResponsavelService) {
        this.encerramentoResponsavelService = encerramentoResponsavelService;
    }

    @GetMapping
    public ResponseEntity<List<OfertaEncerramentoResponsavelResponseDTO>> listarOfertas(
            @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {

        List<OfertaEncerramentoResponsavelResponseDTO> resposta = encerramentoResponsavelService
                .listarResumoOfertas(usuarioLogado.getUsuario())
                .stream()
                .map(OfertaEncerramentoResponsavelResponseDTO::from)
                .toList();

        return ResponseEntity.ok(resposta);
    }

    @GetMapping("/{ofertaId}")
    public ResponseEntity<OfertaEncerramentoResponsavelDetalheResponseDTO> buscarParaEncerramento(
            @PathVariable UUID ofertaId,
            @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {

        Oferta oferta = encerramentoResponsavelService.buscarOfertaParaEncerramento(
                ofertaId,
                usuarioLogado.getUsuario());

        return ResponseEntity.ok(montarDetalhe(oferta));
    }

    @PostMapping("/{ofertaId}/encerramento")
    public ResponseEntity<Void> encerrarOferta(
            @PathVariable UUID ofertaId,
            @Valid @RequestBody EncerramentoResponsavelForm form,
            @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {

        encerramentoResponsavelService.encerrarOferta(
                ofertaId,
                form,
                usuarioLogado.getUsuario());

        return ResponseEntity.noContent().build();
    }

    private OfertaEncerramentoResponsavelDetalheResponseDTO montarDetalhe(Oferta oferta) {
        List<ResumoAlunoEncerramentoOferta> resumoAlunos = encerramentoResponsavelService.montarResumoAlunos(oferta);
        Map<String, Long> quantidadeNotas = encerramentoResponsavelService.contarNotas(resumoAlunos);

        return OfertaEncerramentoResponsavelDetalheResponseDTO.from(
                oferta,
                resumoAlunos,
                encerramentoResponsavelService.calcularMediaFrequencia(resumoAlunos),
                encerramentoResponsavelService.contarCreditosPorEstagio(resumoAlunos),
                encerramentoResponsavelService.contarCreditosPorDocumentacao(resumoAlunos),
                quantidadeNotas);
    }
}
