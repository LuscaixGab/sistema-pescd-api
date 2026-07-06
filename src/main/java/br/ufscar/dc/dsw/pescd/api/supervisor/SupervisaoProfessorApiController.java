package br.ufscar.dc.dsw.pescd.api.supervisor;

import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.SupervisaoProfessorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/professor-supervisor/supervisao")
@PreAuthorize("hasRole('PROFESSOR')")
public class SupervisaoProfessorApiController {

    private final SupervisaoProfessorService supervisaoProfessorService;

    public SupervisaoProfessorApiController(SupervisaoProfessorService supervisaoProfessorService) {
        this.supervisaoProfessorService = supervisaoProfessorService;
    }

    @GetMapping
    public ResponseEntity<List<SupervisaoAlunoResponseDTO>> listarAlunosSupervisionados(
            @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {

        List<SupervisaoAlunoResponseDTO> resposta = supervisaoProfessorService
                .listarAlunosSupervisionados(usuarioLogado.getUsuario())
                .stream()
                .map(SupervisaoAlunoResponseDTO::from)
                .toList();

        return ResponseEntity.ok(resposta);
    }
}
