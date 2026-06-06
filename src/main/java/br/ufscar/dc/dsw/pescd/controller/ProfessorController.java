package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.RelatorioFinal;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.RelatorioFinalRepository;
import br.ufscar.dc.dsw.pescd.repository.UsuarioRepository;

import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.file.Path;

@Controller
@RequestMapping("/professor")
public class ProfessorController {

    private final InscricaoRepository inscricaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RelatorioFinalRepository relatorioFinalRepository;

    public ProfessorController(InscricaoRepository inscricaoRepository, 
                            UsuarioRepository usuarioRepository,
                            RelatorioFinalRepository relatorioFinalRepository) {
        this.inscricaoRepository = inscricaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.relatorioFinalRepository = relatorioFinalRepository;
    }

    // Seleção de papel (professor supervisor ou professor responsável)
    @GetMapping("/atuacao")
    public String selectAtuacao(){
        return "/professor/selectAtuacao";
    }

    // Listar relatórios pendentes
    @GetMapping("/relatorios/pendentes")
    public String listarRelatoriosPendentes(Authentication authentication, Model model) {
        // Pega o professor que está logado no momento
        Usuario professorLogado = usuarioRepository.findByNomeUsuario(authentication.getName())
                .orElseThrow(() -> new RuntimeException("Professor não logado ou não encontrado"));

        // Busca todas as inscrições que são das ofertas desse professor e que estão aguardando o relatório
        List<Inscricao> pendentes = inscricaoRepository.findByOfertaProfessorResponsavelAndStatus(
                professorLogado, 
                StatusInscricao.RELATORIO_ENVIADO
        );

        model.addAttribute("inscricoes", pendentes);
        return "professor/listarRelatoriosPendentes"; // Renderiza a tabela HTML
    }

    // Tela do formulário de avaliação
    @GetMapping("/relatorios/avaliar/{id}")
    public String abrirTelaAvaliacao(@PathVariable("id") UUID id, Model model) {
        Inscricao inscricao = inscricaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição inválida"));

        model.addAttribute("inscricao", inscricao);
        
        return "professor/avaliarRelatorio"; // Renderiza o formulário do parecer
    }

    // Receber a submissão de aprovação ou reprovação
    @PostMapping("/relatorios/avaliar/{id}")
    public String salvarAvaliacao(@PathVariable("id") UUID id, 
                                  @RequestParam("parecer") String parecer, 
                                  @RequestParam("acao") String acao) {
        
        Inscricao inscricao = inscricaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição inválida"));
        
        // Define o novo status baseado no botão que o professor clicou (Aprovar ou Reprovar)
        if ("aprovar".equals(acao)) {
            inscricao.setStatus(StatusInscricao.RELATORIO_APROVADO_PELO_SUPERVISOR);
        } else if ("reprovar".equals(acao)) {
            inscricao.setStatus(StatusInscricao.RELATORIO_REPROVADO);
        }
        
        inscricaoRepository.save(inscricao);

        // Volta para a lista de relatórios pendentes com sucesso
        return "redirect:/professor/relatorios/pendentes?sucesso";
    }

    @GetMapping("/relatorios/download/{id}")
    public ResponseEntity<Resource> baixarRelatorio(@PathVariable("id") UUID id) {
        try {
            Inscricao inscricao = inscricaoRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Inscrição inválida"));

            // Busca o relatório no banco atrelado a essa inscrição
            RelatorioFinal relatorio = relatorioFinalRepository.findByInscricao(inscricao)
                    .orElseThrow(() -> new IllegalArgumentException("Relatório não encontrado"));

            String nomeArquivo = relatorio.getArquivoRelatorio(); 

            // Aponta exatamente para a pasta onde o Aluno salvou
            Path caminhoArquivo = Paths.get("uploads/relatorios/").toAbsolutePath().resolve(nomeArquivo).normalize();
            Resource resource = new UrlResource(caminhoArquivo.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
