package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/aluno")
public class AlunoController {

    private final InscricaoRepository inscricaoRepository;
    private final UsuarioRepository usuarioRepository;

    public AlunoController(InscricaoRepository inscricaoRepository, UsuarioRepository usuarioRepository) {
        this.inscricaoRepository = inscricaoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/ofertas")
    public String listarOfertas(Model model, Principal principal) {
        
        // Contém o que foi digitado no login (pode ser "aluno" ou "aluno@pescd.local")
        String identificador = principal.getName();

        // Busca por nome_usuario OR email usando a mesma string
        Usuario alunoLogado = usuarioRepository.findByNomeUsuarioOrEmail(identificador, identificador)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado: " + identificador));

        // Busca as inscrições ligadas àquele usuário e guarda no model
        model.addAttribute("inscricoes", inscricaoRepository.findByAluno(alunoLogado));

        return "aluno/ofertas";
    }
}