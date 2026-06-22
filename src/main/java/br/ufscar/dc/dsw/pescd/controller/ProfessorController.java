package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.PlanoTrabalho;
import br.ufscar.dc.dsw.pescd.model.RelatorioFinal;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.PlanoTrabalhoRepository;
import br.ufscar.dc.dsw.pescd.repository.RelatorioFinalRepository;

import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.file.Path;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;

@Controller
@RequestMapping("/professor")
public class ProfessorController {

    private static final Logger logger = LoggerFactory.getLogger(ProfessorController.class);

    private final InscricaoRepository inscricaoRepository;
    private final RelatorioFinalRepository relatorioFinalRepository;
    private final PlanoTrabalhoRepository planoTrabalhoRepository;
    private final br.ufscar.dc.dsw.pescd.service.LogStatusService logStatusService;

    public ProfessorController(InscricaoRepository inscricaoRepository,
                               RelatorioFinalRepository relatorioFinalRepository,
                               PlanoTrabalhoRepository planoTrabalhoRepository,
                               br.ufscar.dc.dsw.pescd.service.LogStatusService logStatusService) {
        this.inscricaoRepository = inscricaoRepository;
        this.relatorioFinalRepository = relatorioFinalRepository;
        this.planoTrabalhoRepository = planoTrabalhoRepository;
        this.logStatusService = logStatusService;
    }

    @GetMapping("/atuacao")
    public String selectAtuacao(){
        return "/professor/selectAtuacao";
    }

    @GetMapping("/relatorios/pendentes")
    public String listarRelatoriosPendentes() {
        return "redirect:/professor/supervisao";
    }

    @GetMapping("/relatorios/avaliar/{id}")
    public String abrirTelaAvaliacao(@PathVariable("id") UUID id,
                                     @AuthenticationPrincipal UsuarioUserDetails usuarioLogado,
                                     Model model) {
        Inscricao inscricao = buscarInscricaoDeSupervisor(id, usuarioLogado.getUsuario());
        RelatorioFinal relatorio = buscarRelatorio(inscricao);

        model.addAttribute("inscricao", inscricao);
        model.addAttribute("relatorio", relatorio);
        return "professor-supervisor/avaliarRelatorio";
    }

    @PostMapping("/relatorios/avaliar/{id}")
    public String salvarAvaliacao(@PathVariable("id") UUID id,
                                  @RequestParam("parecer") String parecer,
                                  @RequestParam("frequencia") Integer frequencia,
                                  @RequestParam("nota") String nota,
                                  @RequestParam("acao") String acao,
                                  @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        Inscricao inscricao = buscarInscricaoDeSupervisor(id, usuarioLogado.getUsuario());
        RelatorioFinal relatorio = buscarRelatorio(inscricao);

        if (parecer == null || parecer.trim().isEmpty()) {
            throw new IllegalArgumentException("O parecer é obrigatório.");
        }
        if (frequencia == null || frequencia < 0 || frequencia > 100) {
            throw new IllegalArgumentException("A frequência deve estar entre 0 e 100.");
        }
        if (nota == null || !nota.matches("[ABCDE]")) {
            throw new IllegalArgumentException("A sugestão de nota deve ser A, B, C, D ou E.");
        }

        relatorio.setParecerSupervisor(parecer.trim());
        relatorio.setFrequenciaSupervisor(frequencia);
        relatorio.setSugestaoNotaSupervisor(nota);
        relatorioFinalRepository.save(relatorio);

        if ("aprovar".equals(acao)) {
            inscricao.setStatus(StatusInscricao.RELATORIO_APROVADO_PELO_SUPERVISOR);
        } else if ("reprovar".equals(acao)) {
            inscricao.setStatus(StatusInscricao.RELATORIO_REPROVADO);
        } else {
            throw new IllegalArgumentException("Ação de avaliação inválida.");
        }

        inscricaoRepository.save(inscricao);
        logStatusService.registrarLog(inscricao, inscricao.getStatus(), usuarioLogado.getUsuario());

        return "redirect:/professor/supervisao?sucesso";
    }

    @GetMapping("/relatorios/download/{id}")
    public ResponseEntity<Resource> baixarRelatorio(@PathVariable("id") UUID id,
                                                    @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        try {
            Inscricao inscricao = buscarInscricaoDeSupervisor(id, usuarioLogado.getUsuario());
            RelatorioFinal relatorio = buscarRelatorio(inscricao);

            String nomeArquivo = relatorio.getArquivoRelatorio();
            Path caminhoArquivo = Paths.get("uploads/relatorios/").toAbsolutePath().resolve(nomeArquivo).normalize();
            Resource resource = new UrlResource(caminhoArquivo.toUri());

            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_PDF)
                        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Erro ao baixar relatorio da inscricao {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private Inscricao buscarInscricaoDeSupervisor(UUID inscricaoId, Usuario professor) {
        Inscricao inscricao = inscricaoRepository.findById(inscricaoId)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição inválida"));

        PlanoTrabalho plano = planoTrabalhoRepository.findByInscricao(inscricao)
                .orElseThrow(() -> new IllegalArgumentException("Plano de trabalho não encontrado para esta inscrição."));

        if (!plano.getProfessorSupervisor().getId().equals(professor.getId())) {
            throw new IllegalArgumentException("Você não é o professor supervisor desta inscrição.");
        }
        if (inscricao.getStatus() != StatusInscricao.RELATORIO_ENVIADO) {
            throw new IllegalArgumentException("O relatório não está pendente de avaliação pelo supervisor.");
        }

        return inscricao;
    }

    private RelatorioFinal buscarRelatorio(Inscricao inscricao) {
        return relatorioFinalRepository.findByInscricao(inscricao)
                .orElseThrow(() -> new IllegalArgumentException("Relatório não encontrado"));
    }
}
