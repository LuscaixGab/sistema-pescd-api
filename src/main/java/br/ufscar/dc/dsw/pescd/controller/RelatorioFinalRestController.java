package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.config.MessageHelper;
import br.ufscar.dc.dsw.pescd.dto.RelatorioFinalDTO;
import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.RelatorioFinal;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.RelatorioFinalRepository;
import br.ufscar.dc.dsw.pescd.service.LogStatusService;
import br.ufscar.dc.dsw.pescd.util.UploadUtils;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/aluno/relatorio")
public class RelatorioFinalRestController {

    private static final Logger logger = LoggerFactory.getLogger(RelatorioFinalRestController.class);

    private final InscricaoRepository inscricaoRepository;
    private final RelatorioFinalRepository relatorioFinalRepository;
    private final LogStatusService logStatusService;
    private final MessageHelper messages;

    // Repare que o PlanoTrabalhoRepository sumiu daqui, pois a API não precisa mais renderizar telas
    public RelatorioFinalRestController(InscricaoRepository inscricaoRepository,
                                        RelatorioFinalRepository relatorioFinalRepository,
                                        LogStatusService logStatusService,
                                        MessageHelper messages) {
        this.inscricaoRepository = inscricaoRepository;
        this.relatorioFinalRepository = relatorioFinalRepository;
        this.logStatusService = logStatusService;
        this.messages = messages;
    }

    @PostMapping("/enviar/{id}")
    public ResponseEntity<?> enviarRelatorio(@PathVariable UUID id,
                                             @Valid @ModelAttribute RelatorioFinalDTO dto) {

        Inscricao inscricao = inscricaoRepository.findById(id).orElse(null);

        // 1. TRAVAS DE SEGURANÇA (Substituindo o antigo GetMapping)
        if (inscricao == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", "Inscrição inválida."));
        }

        if (inscricao.getStatus() != StatusInscricao.PLANO_APROVADO) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", "A inscrição não está na fase de envio de relatório."));
        }

        MultipartFile arquivo = dto.getArquivo();

        // 2. VALIDAÇÃO DO ARQUIVO
        try {
            UploadUtils.validarPdfObrigatorio(arquivo, "o relatório");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
        }

        // 3. SALVAMENTO FÍSICO E NO BANCO DE DADOS
        try {
            String nomeArquivo = UUID.randomUUID() + "_" + arquivo.getOriginalFilename();
            Path caminhoDiretorio = Paths.get("uploads/relatorios/").toAbsolutePath();

            if (!Files.exists(caminhoDiretorio)) {
                Files.createDirectories(caminhoDiretorio);
            }

            Path caminhoCompleto = caminhoDiretorio.resolve(nomeArquivo);
            Files.copy(arquivo.getInputStream(), caminhoCompleto, StandardCopyOption.REPLACE_EXISTING);

            RelatorioFinal relatorio = new RelatorioFinal(
                    null,
                    dto.getFrequenciaAluno(),
                    nomeArquivo,
                    inscricao
            );
            relatorioFinalRepository.save(relatorio);

            inscricao.setStatus(StatusInscricao.RELATORIO_ENVIADO);
            inscricaoRepository.save(inscricao);

            logStatusService.registrarLog(inscricao, StatusInscricao.RELATORIO_ENVIADO, inscricao.getAluno());

            // Sucesso: Retorna 201 Created
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensagem", "Relatório final enviado com sucesso!"));

        } catch (IOException e) {
            logger.error("Erro ao processar upload do relatorio da inscricao {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", messages.get("msg.report.upload")));
        }
    }
}