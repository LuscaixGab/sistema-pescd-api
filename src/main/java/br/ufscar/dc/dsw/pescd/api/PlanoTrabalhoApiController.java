package br.ufscar.dc.dsw.pescd.api;

import br.ufscar.dc.dsw.pescd.dto.PlanoTrabalhoForm;
import br.ufscar.dc.dsw.pescd.model.PlanoTrabalho;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.PlanoTrabalhoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plano-trabalho")
public class PlanoTrabalhoApiController {

    private final PlanoTrabalhoService planoTrabalhoService;

    public PlanoTrabalhoApiController(PlanoTrabalhoService planoTrabalhoService) {
        this.planoTrabalhoService = planoTrabalhoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ALUNO')")
    public ResponseEntity<PlanoTrabalhoResponseDTO> enviarPlanoTrabalho(
            @RequestParam UUID professorSupervisorId,
            @RequestParam UUID inscricaoId,
            @RequestPart("arquivoPlano") MultipartFile arquivoPlano,
            @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {

        PlanoTrabalhoForm form = new PlanoTrabalhoForm();
        form.setProfessorSupervisorId(professorSupervisorId);
        form.setInscricaoId(inscricaoId);
        form.setArquivoPlano(arquivoPlano);

        PlanoTrabalho planoSalvo = planoTrabalhoService.criarPlanoTrabalho(form, usuarioLogado.getUsuario());
        PlanoTrabalhoResponseDTO resposta = PlanoTrabalhoResponseDTO.from(planoSalvo);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(planoSalvo.getId())
                .toUri();

        return ResponseEntity.created(location).body(resposta);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ALUNO')")
    public ResponseEntity<PlanoTrabalhoResponseDTO> buscarPlanoDoAluno(@PathVariable UUID id,
                                                                        @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        PlanoTrabalho planoTrabalho = planoTrabalhoService.buscarPlanoDoAluno(id, usuarioLogado.getUsuario());
        return ResponseEntity.ok(PlanoTrabalhoResponseDTO.from(planoTrabalho));
    }

    @PostMapping("/{id}/avaliar")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Void> avaliarPlano(
            @PathVariable UUID id,
            @RequestParam String parecer,
            @RequestParam String acao,
            @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {

        planoTrabalhoService.avaliarPlano(id, parecer, acao, usuarioLogado.getUsuario());
        return ResponseEntity.ok().build();
    }
}
