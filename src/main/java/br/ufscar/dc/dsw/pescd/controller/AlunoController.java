package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.OfertaRepository;
import br.ufscar.dc.dsw.pescd.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.model.Perfil;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.dto.AlunoCadastroDTO;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.validation.BindingResult;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.validation.Valid;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;

import java.security.Principal;
import java.util.NoSuchElementException;
import java.util.UUID;

@Controller
@RequestMapping("/aluno")
public class AlunoController {

    private final InscricaoRepository inscricaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final OfertaRepository ofertaRepository;

    public AlunoController(UsuarioRepository usuarioRepository, 
                           PasswordEncoder passwordEncoder,
                           OfertaRepository ofertaRepository,
                           InscricaoRepository inscricaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.ofertaRepository = ofertaRepository;
        this.inscricaoRepository = inscricaoRepository;
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
    
    // Rota GET: Entrega o DTO vazio para a tela desenhar o formulário
    @GetMapping("/novo")
    public String exibirFormularioNovoAluno(
        @RequestParam(required = false) UUID ofertaId, // Recebe o ID da URL de forma opcional
        Model model) {
        
        model.addAttribute("alunoDTO", new AlunoCadastroDTO());
        model.addAttribute("ofertaId", ofertaId);

        return "aluno/adicionarAluno";
    }

    // Rota POST: Recebe o DTO preenchido quando o secretário clica em "Salvar"
    @PostMapping("/novo")
    public String salvarNovoAluno(
            @Valid @ModelAttribute("alunoDTO") AlunoCadastroDTO alunoDTO,
            BindingResult result,
            @RequestParam(required = false) UUID ofertaId,
            Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("ofertaId", ofertaId); 
            return "aluno/adicionarAluno";
        }

        if (usuarioRepository.findByEmail(alunoDTO.getEmail()).isPresent() || 
            usuarioRepository.findByNomeUsuario(alunoDTO.getNomeUsuario()).isPresent()) {
            model.addAttribute("erro", "Já existe um usuário cadastrado com este E-mail ou RA.");
            model.addAttribute("ofertaId", ofertaId);
            return "aluno/adicionarAluno";
        }

        Usuario novoAluno = new Usuario();
        novoAluno.setNomeCompleto(alunoDTO.getNomeCompleto());
        novoAluno.setEmail(alunoDTO.getEmail());
        novoAluno.setNomeUsuario(alunoDTO.getNomeUsuario());
        novoAluno.setSenha(passwordEncoder.encode(alunoDTO.getSenha()));
        novoAluno.setPerfil(Perfil.ALUNO);

        usuarioRepository.save(novoAluno);

        // 2. Faz a Matrícula se o ID da oferta existir
        if (ofertaId != null) {
            
            // Acha a oferta pelo ID
            Oferta oferta = ofertaRepository.findById(ofertaId)
                    .orElseThrow(() -> new IllegalArgumentException("Oferta não encontrada."));

            // Cria a inscrição amarrando o aluno novo na oferta direto pelo construtor
            Inscricao novaInscricao = new Inscricao(null, novoAluno, oferta, StatusInscricao.NAO_ENVIADO);

            // Salva no banco de dados!
            inscricaoRepository.save(novaInscricao);

            return "redirect:/ofertas/" + ofertaId + "/alunos"; 
        }

        return "redirect:/painel"; 
    }
}