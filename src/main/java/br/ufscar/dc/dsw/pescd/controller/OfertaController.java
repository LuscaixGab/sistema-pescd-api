package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.dto.OfertaForm;
import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.model.Perfil;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.OfertaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import br.ufscar.dc.dsw.pescd.repository.UsuarioRepository;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;

@Controller
@RequestMapping("/ofertas")
@PreAuthorize("hasRole('SECRETARIO')")
public class OfertaController {

    private final OfertaService ofertaService;
    private final UsuarioRepository usuarioRepository;
    private final InscricaoRepository inscricaoRepository;

    public OfertaController(OfertaService ofertaService, UsuarioRepository usuarioRepository, InscricaoRepository inscricaoRepository) {
        this.ofertaService = ofertaService;
        this.usuarioRepository = usuarioRepository;
        this.inscricaoRepository = inscricaoRepository;
    }

    @GetMapping
    public String listar(Model model) {
        // 1. Busca todas as ofertas
        List<Oferta> ofertas = ofertaService.listarOfertas();

        // 2. Cria o mapa para guardar o status de cada oferta
        Map<UUID, String> statusOfertas = new HashMap<>();

        // 3. Calcula o status dinamicamente cruzando as datas e as inscrições
        for (Oferta oferta : ofertas) {
            List<Inscricao> inscricoes = inscricaoRepository.findByOferta(oferta);
            statusOfertas.put(oferta.getId(), calcularStatus(oferta, inscricoes));
        }

        // 4. Envia tanto a lista de ofertas quanto o mapa de status para o Thymeleaf
        model.addAttribute("ofertas", ofertas);
        model.addAttribute("statusOfertas", statusOfertas);

        return "ofertas/lista";
    }

    private String calcularStatus(Oferta oferta, List<Inscricao> inscricoes) {
        LocalDate hoje = LocalDate.now();

        // 1. Caso a oferta ainda não tenha chegado à data de início
        if (hoje.isBefore(oferta.getDataInicio())) {
            return "Aguardando início";
        }

        // 2. Se a data atual estiver dentro do intervalo planejado da oferta
        if ((hoje.isAfter(oferta.getDataInicio()) || hoje.isEqual(oferta.getDataInicio())) &&
                (hoje.isBefore(oferta.getDataFim()) || hoje.isEqual(oferta.getDataFim()))) {
            return "Em andamento";
        }

        // 3. Se o prazo final (dataFim) já expirou, o status depende do progresso dos alunos:
        if (inscricoes.isEmpty()) {
            return "Concluída";
        }

        boolean todosConcluidos = true;
        boolean temAtrasoGrave = false;

        for (Inscricao inscricao : inscricoes) {
            StatusInscricao status = inscricao.getStatus();

            if (status != StatusInscricao.CONCLUIDO && status != StatusInscricao.CONCLUIDO_PELO_RESPONSAVEL) {
                todosConcluidos = false;

                if (status == StatusInscricao.NAO_ENVIADO ||
                        status == StatusInscricao.PLANO_ENVIADO ||
                        status == StatusInscricao.PLANO_REPROVADO) {
                    temAtrasoGrave = true;
                }
            }
        }

        if (todosConcluidos) {
            return "Concluída";
        }

        if (temAtrasoGrave) {
            return "Em atraso";
        }

        return "Aguardando encerramento...";
    }

    @GetMapping("/nova")
    public String novaOferta(Model model) {
        model.addAttribute("ofertaForm", new OfertaForm());
        model.addAttribute("professores", ofertaService.listarProfessores());
        return "ofertas/formulario";
    }

    @PostMapping
    public String criarOferta(
            @Valid @ModelAttribute("ofertaForm") OfertaForm ofertaForm,
            BindingResult bindingResult,
            @AuthenticationPrincipal UsuarioUserDetails usuarioLogado,
            Model model,
            RedirectAttributes redirectAttributes) {

        validarDatas(ofertaForm, bindingResult);

        if (bindingResult.hasErrors()) {
            model.addAttribute("professores", ofertaService.listarProfessores());
            return "ofertas/formulario";
        }

        try {
            Oferta oferta = ofertaService.criarOferta(ofertaForm, usuarioLogado.getUsuario());
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Oferta \"" + oferta.getNomeOferta() + "\" criada com sucesso.");
            return "redirect:/ofertas";
        } catch (IllegalArgumentException exception) {
            model.addAttribute("erroGeral", exception.getMessage());
            model.addAttribute("professores", ofertaService.listarProfessores());
            return "ofertas/formulario";
        }
    }

    private void validarDatas(OfertaForm ofertaForm, BindingResult bindingResult) {
        if (ofertaForm.getDataInicio() != null && ofertaForm.getDataFim() != null
                && !ofertaForm.getDataFim().isAfter(ofertaForm.getDataInicio())) {
            bindingResult.rejectValue("dataFim", "dataFim.invalida",
                    "A data de fim deve ser depois da data de início.");
        }
    }

    @Autowired // Adicionei essa anotação se precisar injetar
    private br.ufscar.dc.dsw.pescd.service.InscricaoService inscricaoService;
    @Autowired
    private br.ufscar.dc.dsw.pescd.repository.OfertaRepository ofertaRepository;
    @Autowired
    private br.ufscar.dc.dsw.pescd.repository.PlanoTrabalhoRepository planoTrabalhoRepository;
    @Autowired
    private br.ufscar.dc.dsw.pescd.repository.DocumentacaoAulaRepository documentacaoAulaRepository;
    @Autowired
    private br.ufscar.dc.dsw.pescd.repository.RelatorioFinalRepository relatorioFinalRepository;
    @Autowired
    private br.ufscar.dc.dsw.pescd.repository.LogStatusInscricaoRepository logStatusInscricaoRepository;

    // Exibir tela de adicionar alunos
    @GetMapping("/{id}/alunos")
    public String exibirAdicionarAlunos(@PathVariable("id") java.util.UUID id, Model model) {
        Oferta oferta = ofertaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oferta inválida."));
        model.addAttribute("oferta", oferta);

        // Pega TODOS os alunos em ordem alfabética
        List<Usuario> todosAlunos = usuarioRepository.findByPerfilOrderByNomeCompletoAsc(Perfil.ALUNO);
        
        // Pega quem já tá inscrito
        List<Inscricao> inscricoesAtuais = inscricaoRepository.findByOferta(oferta);
        
        // Extrai só os IDs pra tela saber quem marcar com o "checked"
        List<UUID> alunosMatriculadosIds = inscricoesAtuais.stream()
                .map(inscricao -> inscricao.getAluno().getId())
                .collect(Collectors.toList());

        model.addAttribute("oferta", oferta);
        model.addAttribute("todosAlunos", todosAlunos);
        model.addAttribute("alunosMatriculadosIds", alunosMatriculadosIds);

        return "ofertas/adicionar-alunos";
    }

    @PostMapping("/{id}/alunos/sincronizar")
    public String sincronizarAlunosLista(@PathVariable UUID id, 
                                        @RequestParam(required = false) List<UUID> alunosSelecionados,
                                        RedirectAttributes redirectAttributes) {
        
        Oferta oferta = ofertaRepository.findById(id).orElseThrow();
        
        // Se a lista vier nula (secretário desmarcou absolutamente todo mundo)
        if (alunosSelecionados == null) {
            alunosSelecionados = new ArrayList<>();
        }

        // Pega como o banco está agora
        List<Inscricao> inscricoesAtuais = inscricaoRepository.findByOferta(oferta);
        
        // PASSO A: O que tá no banco e NÃO tá na tela -> O secretário desmarcou, então REMOVE
        for (Inscricao inscricao : inscricoesAtuais) {
            if (!alunosSelecionados.contains(inscricao.getAluno().getId())) {
                inscricaoRepository.delete(inscricao);
            }
        }

        // Cria uma lista rápida só com os IDs atuais pra facilitar o Passo B
        List<UUID> idsAtuais = inscricoesAtuais.stream()
                .map(i -> i.getAluno().getId())
                .collect(Collectors.toList());

        // PASSO B: O que tá na tela e NÃO tá no banco -> O secretário marcou alguém novo, então ADICIONA
        for (UUID alunoId : alunosSelecionados) {
            if (!idsAtuais.contains(alunoId)) {
                Usuario aluno = usuarioRepository.findById(alunoId).orElseThrow();
                
                // Instancia com o construtor igual fizemos antes
                Inscricao novaInscricao = new Inscricao(null, aluno, oferta, StatusInscricao.NAO_ENVIADO);
                inscricaoRepository.save(novaInscricao);
            }
        }

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Matrículas atualizadas com sucesso!");
        return "redirect:/ofertas/" + id + "/alunos"; 
    }

    // S.02 - POST: Processar o upload do CSV
    @PostMapping("/{id}/alunos/upload")
    public String carregarFicheiroAlunos(@PathVariable("id") java.util.UUID id,
                                         @org.springframework.web.bind.annotation.RequestParam("file") org.springframework.web.multipart.MultipartFile file,
                                         RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("erroGeral", "Por favor, selecione um arquivo CSV válido.");
            return "redirect:/ofertas/" + id + "/alunos";
        }

        try {
            inscricaoService.processarAlunosCsv(id, file);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Alunos cadastrados e inscritos com sucesso!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("erroGeral", "Erro ao processar o arquivo: " + e.getMessage());
        }

        return "redirect:/ofertas/" + id + "/alunos";
    }

    // S.03 - GET: Acompanhar o andamento da oferta
    @GetMapping("/{id}/acompanhamento")
    public String exibirAcompanhamentoOferta(@PathVariable("id") UUID id, Model model) {
        Oferta oferta = ofertaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oferta inválida."));

        // Busca todas as matrículas dessa oferta
        List<Inscricao> inscricoes = inscricaoRepository.findByOferta(oferta);

        // Calcula métricas simples para a visualização do secretário
        long totalAlunos = inscricoes.size();
        long concluidos = inscricoes.stream()
                .filter(i -> i.getStatus() == StatusInscricao.CONCLUIDO || i.getStatus() == StatusInscricao.CONCLUIDO_PELO_RESPONSAVEL)
                .count();
        long pendentesIniciais = inscricoes.stream()
                .filter(i -> i.getStatus() == StatusInscricao.NAO_ENVIADO)
                .count();

        model.addAttribute("oferta", oferta);
        model.addAttribute("inscricoes", inscricoes);
        model.addAttribute("totalAlunos", totalAlunos);
        model.addAttribute("concluidos", concluidos);
        model.addAttribute("emAndamento", totalAlunos - concluidos - pendentesIniciais);
        model.addAttribute("pendentesIniciais", pendentesIniciais);

        return "ofertas/acompanhamento";
    }

    // S.03 - GET: Detalhes do Aluno
    @GetMapping("/{ofertaId}/alunos/{inscricaoId}/detalhes")
    public String verDetalhesAluno(@PathVariable UUID ofertaId, @PathVariable UUID inscricaoId, Model model) {
        Inscricao inscricao = inscricaoRepository.findById(inscricaoId)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição não encontrada."));

        // Busca todos os documentos que o aluno possa ter enviado
        br.ufscar.dc.dsw.pescd.model.PlanoTrabalho plano = planoTrabalhoRepository.findByInscricao(inscricao).orElse(null);
        br.ufscar.dc.dsw.pescd.model.DocumentacaoAula documentacao = documentacaoAulaRepository.findByInscricaoId(inscricaoId).orElse(null);
        br.ufscar.dc.dsw.pescd.model.RelatorioFinal relatorio = relatorioFinalRepository.findByInscricao(inscricao).orElse(null);

        // Busca o histórico de logs
        List<br.ufscar.dc.dsw.pescd.model.LogStatusInscricao> logs = logStatusInscricaoRepository.findByInscricaoOrderByDataMudancaDesc(inscricao);

        model.addAttribute("oferta", inscricao.getOferta());
        model.addAttribute("inscricao", inscricao);
        model.addAttribute("plano", plano);
        model.addAttribute("documentacao", documentacao);
        model.addAttribute("relatorio", relatorio);
        model.addAttribute("logs", logs);

        return "ofertas/aluno-detalhes";
    }
}

