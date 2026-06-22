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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.UUID;

@Controller
@RequestMapping("/aluno/documentacao")
public class DocumentacaoAulaController {

    private static final Logger logger = LoggerFactory.getLogger(DocumentacaoAulaController.class);

    private final DocumentacaoAulaService documentacaoService;
    private final InscricaoRepository inscricaoRepository;
    private final MessageHelper messages;

    // Injeção de dependência pelo construtor
    public DocumentacaoAulaController(DocumentacaoAulaService documentacaoService,
                                      InscricaoRepository inscricaoRepository,
                                      MessageHelper messages) {
        this.documentacaoService = documentacaoService;
        this.inscricaoRepository = inscricaoRepository;
        this.messages = messages;
    }

    // GET: Exibe o formulário
    // O aluno vai acessar algo como: /aluno/documentacao/nova/123e4567-e89b-12d3...
    @GetMapping("/nova/{idInscricao}")
    public String exibirFormulario(@PathVariable UUID idInscricao, Model model, RedirectAttributes redirectAttributes) {
        
        // Estar inscrito em uma oferta
        Inscricao inscricao = inscricaoRepository.findById(idInscricao).orElse(null);
        if (inscricao == null) {
            redirectAttributes.addFlashAttribute("erro", messages.get("msg.docs.notFound"));
            return "redirect:/aluno/ofertas"; // TODO: Ajuste para a rota correta da tela inicial do aluno
        }

        // Oferta "em andamento" (checando pelo período das datas)
        java.time.LocalDate hoje = java.time.LocalDate.now();
        java.time.LocalDate inicio = inscricao.getOferta().getDataInicio();
        java.time.LocalDate fim = inscricao.getOferta().getDataFim();

        if (hoje.isBefore(inicio) || hoje.isAfter(fim)) {
            redirectAttributes.addFlashAttribute("erro", messages.get("msg.docs.offerClosed"));
            return "redirect:/aluno/ofertas";
        }

        // Status do aluno deve ser "não enviado" (ou selecionado para enviar)
        if (inscricao.getStatus() != StatusInscricao.NAO_ENVIADO) {
            redirectAttributes.addFlashAttribute("erro", messages.get("msg.docs.wrongPhase"));
            return "redirect:/aluno/ofertas";
        }

        // Manda o DTO vazio pra tela para o aluno preencher
        model.addAttribute("inscricao", inscricao);
        model.addAttribute("documentacaoDTO", new DocumentacaoAulaDTO());
        return "aluno/formularioDocumentacao";
    }

    // POST: Recebe os dados e o PDF da tela
    @PostMapping("/enviar/{idInscricao}")
    public String processarEnvio(@PathVariable UUID idInscricao, 
                                 @Valid @ModelAttribute("documentacaoDTO") DocumentacaoAulaDTO dto,
                                 BindingResult result, 
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        
        Inscricao inscricao = inscricaoRepository.findById(idInscricao).orElseThrow();

        // Se o Spring achar algum erro nas validações do DTO (@NotBlank, @NotNull)
        if (result.hasErrors()) {
            model.addAttribute("inscricao", inscricao);
            return "aluno/formularioDocumentacao"; // Volta pra tela com os erros
        }

        try {
            // Chama o Service
            documentacaoService.processarEnvio(dto, inscricao);
            redirectAttributes.addFlashAttribute("sucesso", messages.get("msg.docs.sent"));
            return "redirect:/aluno/ofertas"; // Redireciona de volta pra lista

        } catch (IllegalArgumentException e) {
            // Arquivo era maior que 5MB ou não era PDF
            result.rejectValue("arquivo", "error.arquivo", e.getMessage());
            model.addAttribute("inscricao", inscricao);
            return "aluno/formularioDocumentacao";
        } catch (Exception e) {
            // Qualquer outro erro genérico ao salvar o arquivo
            logger.error("Erro interno ao salvar documentacao da inscricao {}", idInscricao, e);
            redirectAttributes.addFlashAttribute("erro", messages.get("msg.upload.internal"));
            return "redirect:/aluno/ofertas";
        }
    }
}
