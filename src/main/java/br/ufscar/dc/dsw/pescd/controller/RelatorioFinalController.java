package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.dto.RelatorioFinalDTO;
import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.PlanoTrabalho;
import br.ufscar.dc.dsw.pescd.model.RelatorioFinal;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.PlanoTrabalhoRepository;
import br.ufscar.dc.dsw.pescd.repository.RelatorioFinalRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.nio.file.StandardCopyOption;

@Controller
@RequestMapping("/aluno/relatorio")
public class RelatorioFinalController {

    private final InscricaoRepository inscricaoRepository;
    private final PlanoTrabalhoRepository planoTrabalhoRepository;
    private final RelatorioFinalRepository relatorioFinalRepository;
    private final br.ufscar.dc.dsw.pescd.service.LogStatusService logStatusService;

    public RelatorioFinalController(InscricaoRepository inscricaoRepository, 
                                    PlanoTrabalhoRepository planoTrabalhoRepository, 
                                    RelatorioFinalRepository relatorioFinalRepository,
                                    br.ufscar.dc.dsw.pescd.service.LogStatusService logStatusService) {
        this.inscricaoRepository = inscricaoRepository;
        this.planoTrabalhoRepository = planoTrabalhoRepository;
        this.relatorioFinalRepository = relatorioFinalRepository;
        this.logStatusService = logStatusService;
    }

    @GetMapping("/novo/{id}")
    public String exibirFormularioRelatorio(@PathVariable UUID id, Model model) {
        Inscricao inscricao = inscricaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição inválida."));

        // Só acessa a tela se o plano estiver aprovado
        if (inscricao.getStatus() != StatusInscricao.PLANO_APROVADO) {
            return "redirect:/aluno/ofertas";
        }

        // Buscar o plano de trabalho para exibir como "somente leitura"
        PlanoTrabalho plano = planoTrabalhoRepository.findByInscricao(inscricao).orElse(null);

        model.addAttribute("inscricao", inscricao);
        model.addAttribute("plano", plano);
        model.addAttribute("relatorioDTO", new RelatorioFinalDTO());

        return "/aluno/formularioRelatorio"; 
    }

    @PostMapping("/enviar/{id}")
    public String enviarRelatorio(@PathVariable UUID id, 
                                  @Valid @ModelAttribute("relatorioDTO") RelatorioFinalDTO dto, 
                                  BindingResult result, 
                                  Model model) {
        
        Inscricao inscricao = inscricaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição inválida."));

        // Se houver erro de validação (ex: frequência menor que 0 ou maior que 100)
        if (result.hasErrors()) {
            return recarregarTelaRelatorioComErro(inscricao, model, dto);
        }

        MultipartFile arquivo = dto.getArquivo();

        // Validação de PDF
        if (arquivo.isEmpty() || arquivo.getContentType() == null || !arquivo.getContentType().equals("application/pdf")) {
            model.addAttribute("erro", "O arquivo deve ser um PDF válido.");
            return recarregarTelaRelatorioComErro(inscricao, model, dto);
        }

        // Validação de tamanho (Máx 5MB)
        long tamanhoMaximo = 5 * 1024 * 1024;
        if (arquivo.getSize() > tamanhoMaximo) {
            model.addAttribute("erro", "O arquivo excede o limite de 5MB.");
            return recarregarTelaRelatorioComErro(inscricao, model, dto);
        }

        try {
            // Lógica de salvamento físico do arquivo no servidor
            String nomeArquivo = UUID.randomUUID() + "_" + arquivo.getOriginalFilename();
            // Pega o caminho absoluto da pasta do projeto para não ter erro de rota
            Path caminhoDiretorio = Paths.get("uploads/relatorios/").toAbsolutePath();
            
            if (!Files.exists(caminhoDiretorio)) {
                Files.createDirectories(caminhoDiretorio);
            }
            
            Path caminhoCompleto = caminhoDiretorio.resolve(nomeArquivo);
            
            // O jeito blindado de salvar arquivos no Spring Boot
            Files.copy(arquivo.getInputStream(), caminhoCompleto, StandardCopyOption.REPLACE_EXISTING);

            // Criação da entidade e salvamento no banco de dados
            RelatorioFinal relatorio = new RelatorioFinal(
                null, 
                dto.getFrequenciaAluno(), 
                nomeArquivo, 
                inscricao
            );
            relatorioFinalRepository.save(relatorio);

            // RN-4: Atualização do Status da Inscrição após envio com sucesso
            inscricao.setStatus(StatusInscricao.RELATORIO_ENVIADO);
            inscricaoRepository.save(inscricao);

            logStatusService.registrarLog(inscricao, StatusInscricao.RELATORIO_ENVIADO, inscricao.getAluno());

        } catch (IOException e) {
            e.printStackTrace(); // Isso vai imprimir o erro real no seu terminal para ajudar se falhar de novo
            model.addAttribute("erro", "Erro ao processar o upload do arquivo. Tente novamente.");
            return recarregarTelaRelatorioComErro(inscricao, model, dto);
        }

        return "redirect:/aluno/ofertas";
    }

    private String recarregarTelaRelatorioComErro(Inscricao inscricao, Model model, RelatorioFinalDTO dto) {
        PlanoTrabalho plano = planoTrabalhoRepository.findByInscricao(inscricao).orElse(null);
        model.addAttribute("inscricao", inscricao);
        model.addAttribute("plano", plano);
        model.addAttribute("relatorioDTO", dto);
        return "/aluno/formularioRelatorio";
    }
}