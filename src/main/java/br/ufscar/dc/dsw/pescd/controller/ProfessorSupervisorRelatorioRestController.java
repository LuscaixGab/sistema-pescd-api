package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.PlanoTrabalho;
import br.ufscar.dc.dsw.pescd.model.RelatorioFinal;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.PlanoTrabalhoRepository;
import br.ufscar.dc.dsw.pescd.repository.RelatorioFinalRepository;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.LogStatusService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/professor-supervisor/relatorios")
@PreAuthorize("hasRole('PROFESSOR')")
public class ProfessorSupervisorRelatorioRestController {

    private static final Logger logger = LoggerFactory.getLogger(ProfessorSupervisorRelatorioRestController.class);

    private final InscricaoRepository inscricaoRepository;
    private final RelatorioFinalRepository relatorioFinalRepository;
    private final PlanoTrabalhoRepository planoTrabalhoRepository;
    private final LogStatusService logStatusService;

    public ProfessorSupervisorRelatorioRestController(InscricaoRepository inscricaoRepository,
                                                      RelatorioFinalRepository relatorioFinalRepository,
                                                      PlanoTrabalhoRepository planoTrabalhoRepository,
                                                      LogStatusService logStatusService) {
        this.inscricaoRepository = inscricaoRepository;
        this.relatorioFinalRepository = relatorioFinalRepository;
        this.planoTrabalhoRepository = planoTrabalhoRepository;
        this.logStatusService = logStatusService;
    }

    // 1. CARREGAR DADOS PARA ANÁLISE (O Front-end vai usar isso para montar a tela)
    @GetMapping("/{id}")
    public ResponseEntity<?> obterDetalhesAvaliacao(@PathVariable("id") UUID id,
                                                    @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        try {
            Inscricao inscricao = buscarInscricaoDeSupervisor(id, usuarioLogado.getUsuario());
            RelatorioFinal relatorio = buscarRelatorio(inscricao);
            
            return ResponseEntity.ok(Map.of("inscricao", inscricao, "relatorio", relatorio));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", e.getMessage()));
        }
    }

    // 2. SALVAR AVALIAÇÃO (Recebe os dados do front-end em formato JSON)
    @PostMapping("/{id}/avaliar")
    public ResponseEntity<?> salvarAvaliacao(@PathVariable("id") UUID id,
                                             @RequestBody AvaliacaoSupervisorDTO dto,
                                             @AuthenticationPrincipal UsuarioUserDetails usuarioLogado) {
        try {
            Inscricao inscricao = buscarInscricaoDeSupervisor(id, usuarioLogado.getUsuario());
            RelatorioFinal relatorio = buscarRelatorio(inscricao);

            if (dto.parecer() == null || dto.parecer().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "O parecer é obrigatório."));
            }
            if (dto.frequencia() == null || dto.frequencia() < 0 || dto.frequencia() > 100) {
                return ResponseEntity.badRequest().body(Map.of("erro", "A frequência deve estar entre 0 e 100."));
            }
            if (dto.nota() == null || !dto.nota().matches("[ABCDE]")) {
                return ResponseEntity.badRequest().body(Map.of("erro", "A sugestão de nota deve ser A, B, C, D ou E."));
            }

            relatorio.setParecerSupervisor(dto.parecer().trim());
            relatorio.setFrequenciaSupervisor(dto.frequencia());
            relatorio.setSugestaoNotaSupervisor(dto.nota());
            relatorioFinalRepository.save(relatorio);

            if ("aprovar".equalsIgnoreCase(dto.acao())) {
                inscricao.setStatus(StatusInscricao.RELATORIO_APROVADO_PELO_SUPERVISOR);
            } else if ("reprovar".equalsIgnoreCase(dto.acao())) {
                inscricao.setStatus(StatusInscricao.RELATORIO_REPROVADO);
            } else {
                return ResponseEntity.badRequest().body(Map.of("erro", "Ação de avaliação inválida. Use 'aprovar' ou 'reprovar'."));
            }

            inscricaoRepository.save(inscricao);
            logStatusService.registrarLog(inscricao, inscricao.getStatus(), usuarioLogado.getUsuario());

            return ResponseEntity.ok(Map.of("mensagem", "Avaliação do relatório salva com sucesso!"));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", e.getMessage()));
        }
    }

    // 3. DOWNLOAD DO RELATÓRIO PDF
    @GetMapping("/{id}/download")
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
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            logger.error("Erro ao baixar relatorio da inscricao {}", id, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- MÉTODOS PRIVADOS DE VALIDAÇÃO ---
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

    // --- DTO INTERNO PARA RECEBER O JSON NO POST ---
    public record AvaliacaoSupervisorDTO(String parecer, Integer frequencia, String nota, String acao) {}
}