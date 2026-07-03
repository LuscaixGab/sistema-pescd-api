package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.UsuarioRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/aluno")
public class AlunoRestController {

    private final InscricaoRepository inscricaoRepository;
    private final UsuarioRepository usuarioRepository;

    // A injeção por construtor exigida pelo professor já está aqui
    public AlunoRestController(UsuarioRepository usuarioRepository, 
                               InscricaoRepository inscricaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @GetMapping("/ofertas")
    public ResponseEntity<List<Inscricao>> listarOfertas(Principal principal) {
        
        String identificador = principal.getName();

        Usuario alunoLogado = usuarioRepository.findByNomeUsuarioOrEmail(identificador, identificador)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado: " + identificador));

        List<Inscricao> inscricoes = inscricaoRepository.findByAluno(alunoLogado);

        // Devolve os dados crus (JSON) com o status HTTP 200 (OK)
        return ResponseEntity.ok(inscricoes);
    }
}