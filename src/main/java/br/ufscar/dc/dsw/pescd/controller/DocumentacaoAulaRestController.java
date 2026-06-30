package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.config.MessageHelper;
import br.ufscar.dc.dsw.pescd.dto.DocumentacaoAulaDTO;
import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.service.DocumentacaoAulaService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/aluno/documentacao")
public class DocumentacaoAulaRestController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentacaoAulaRestController.class);

    private final DocumentacaoAulaService documentacaoService;
    private final InscricaoRepository inscricaoRepository;
    private final MessageHelper messages;

    // Injeção de dependência pelo construtor (Exigência do professor mantida)
    public DocumentacaoAulaRestController(DocumentacaoAulaService documentacaoService,
                                          InscricaoRepository inscricaoRepository,
                                          MessageHelper messages) {
        this.documentacaoService = documentacaoService;
        this.inscricaoRepository = inscricaoRepository;
        this.messages = messages;
    }

    // POST: Recebe os dados em texto + o PDF
    @PostMapping("/enviar/{idInscricao}")
    public ResponseEntity<?> processarEnvio(@PathVariable UUID idInscricao, 
                                            @Valid @ModelAttribute DocumentacaoAulaDTO dto) {
        
        Inscricao inscricao = inscricaoRepository.findById(idInscricao).orElse(null);

        // 1. TRAVAS DE SEGURANÇA (Trouxemos aquelas validações do antigo GET pra cá)
        if (inscricao == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("erro", messages.get("msg.docs.notFound")));
        }

        java.time.LocalDate hoje = java.time.LocalDate.now();
        java.time.LocalDate inicio = inscricao.getOferta().getDataInicio();
        java.time.LocalDate fim = inscricao.getOferta().getDataFim();

        if (hoje.isBefore(inicio) || hoje.isAfter(fim)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("erro", messages.get("msg.docs.offerClosed")));
        }

        if (inscricao.getStatus() != StatusInscricao.NAO_ENVIADO) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", messages.get("msg.docs.wrongPhase")));
        }

        // 2. TENTA SALVAR O ARQUIVO E OS DADOS
        try {
            documentacaoService.processarEnvio(dto, inscricao);
            
            // Sucesso: Devolve HTTP 201 (Created) e a mensagem em JSON
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("mensagem", messages.get("msg.docs.sent")));

        } catch (IllegalArgumentException e) {
            // Arquivo era maior que 5MB ou barrou no Magic Number (que você vai implementar no Service)
            return ResponseEntity.badRequest().body(Map.of("erro", e.getMessage()));
            
        } catch (Exception e) {
            // Qualquer outro erro genérico
            logger.error("Erro interno ao salvar documentacao da inscricao {}", idInscricao, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", messages.get("msg.upload.internal")));
        }
    }
}