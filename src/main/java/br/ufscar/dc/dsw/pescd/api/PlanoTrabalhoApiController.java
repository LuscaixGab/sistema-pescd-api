package br.ufscar.dc.dsw.pescd.api;

import br.ufscar.dc.dsw.pescd.dto.PlanoTrabalhoForm;
import br.ufscar.dc.dsw.pescd.model.PlanoTrabalho;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.PlanoTrabalhoService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/plano-trabalho")
@PreAuthorize("hasRole('ALUNO')")
public class PlanoTrabalhoApiController {

    private final PlanoTrabalhoService planoTrabalhoService;

    public PlanoTrabalhoApiController(PlanoTrabalhoService planoTrabalhoService) {
        this.planoTrabalhoService = planoTrabalhoService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
    public ResponseEntity<PlanoTrabalhoResponseDTO> buscarPlanoTrabalho(@PathVariable UUID id,
                                                                        @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        PlanoTrabalho planoTrabalho = planoTrabalhoService.buscarPlanoDoAluno(id, usuarioLogado.getUsuario());
        return ResponseEntity.ok(PlanoTrabalhoResponseDTO.from(planoTrabalho));
    }
}
