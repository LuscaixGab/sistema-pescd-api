package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.dto.OfertaForm;
import br.ufscar.dc.dsw.pescd.model.*;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.OfertaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
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
    public String listar(@AuthenticationPrincipal UsuarioUserDetails usuarioLogado, Model model) {
        Usuario usuario = usuarioLogado.getUsuario();
        List<Oferta> ofertas;

        // PR.04:
        // Se for professor, busca só as dele. Se for secretário/admin, busca todas.
        if (usuario.getPerfil() == Perfil.PROFESSOR) {
            ofertas = ofertaRepository.findByProfessorResponsavel(usuario);
        } else {
            ofertas = ofertaRepository.findAll();
        }

        Map<UUID, String> statusOfertas = new HashMap<>();

        for (Oferta oferta : ofertas) {
            List<Inscricao> inscricoes = inscricaoRepository.findByOferta(oferta);
            statusOfertas.put(oferta.getId(), calcularStatus(oferta, inscricoes));
        }

        model.addAttribute("ofertas", ofertas);
        model.addAttribute("statusOfertas", statusOfertas);
        // Passa o perfil para o html saber o que esconder/mostrar
        model.addAttribute("perfilUsuario", usuario.getPerfil().name());

        return "ofertas/lista";
    }

    private String calcularStatus(Oferta oferta, List<Inscricao> inscricoes) {
        // 1. Se já possui data de encerramento registrada no banco, está oficialmente Concluída
        if (oferta.getDataEncerramento() != null) {
            return "Concluída";
        }

        LocalDate hoje = LocalDate.now();

        // 2. Se a data atual for anterior ao início
        if (hoje.isBefore(oferta.getDataInicio())) {
            return "Aguardando início";
        }

        // 3. Se a oferta não tem nenhum aluno matriculado, baseia-se apenas no calendário
        if (inscricoes.isEmpty()) {
            return hoje.isAfter(oferta.getDataFim()) ? "Concluída" : "Em andamento";
        }

        // 4. (RN-1): Todos os alunos já foram validados pelo professor?
        boolean todosConcluidosPeloProfessor = true;
        for (Inscricao inscricao : inscricoes) {
            if (inscricao.getStatus() != StatusInscricao.CONCLUIDO_PELO_RESPONSAVEL &&
                    inscricao.getStatus() != StatusInscricao.CONCLUIDO) {
                todosConcluidosPeloProfessor = false;
                break;
            }
        }

        // Se todos concluíram, o status MUDA imediatamente para aguardar o Secretário (mesmo que o semestre não tenha acabado)
        if (todosConcluidosPeloProfessor) {
            return "Aguardando encerramento do secretário";
        }

        // 5. Se nem todos terminaram e o prazo final já passou
        if (hoje.isAfter(oferta.getDataFim())) {
            return "Em atraso";
        }

        // 6. Se ainda está dentro do prazo e os alunos estão trabalhando
        return "Em andamento";
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
    @Autowired
    private br.ufscar.dc.dsw.pescd.repository.ConfiguracaoRepository configuracaoRepository;
    @Autowired
    private br.ufscar.dc.dsw.pescd.service.LogStatusService logStatusService;

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
            redirectAttributes.addFlashAttribute("erroGeral", "Por favor, selecione um arquivo válido.");
            return "redirect:/ofertas/" + id + "/alunos";
        }

        // FIX RN-1: Validar se é realmente um CSV
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            redirectAttributes.addFlashAttribute("erroGeral", "O arquivo deve ser obrigatoriamente do tipo .csv.");
            return "redirect:/ofertas/" + id + "/alunos";
        }

        // FIX RN-2: Validar o limite de 5MB (5 * 1024 * 1024 bytes)
        if (file.getSize() > 5242880) {
            redirectAttributes.addFlashAttribute("erroGeral", "O arquivo não pode ultrapassar o limite de 5MB.");
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

    // S.04: Fluxo de encerramento

    // GET: Exibe a tela de confirmação lendo as instruções do banco
    @GetMapping("/{id}/encerrar")
    public String exibirConfirmacaoEncerramento(@PathVariable("id") UUID id, Model model) {
        Oferta oferta = ofertaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oferta inválida."));

        List<Inscricao> inscricoes = inscricaoRepository.findByOferta(oferta);
        String statusAtual = calcularStatus(oferta, inscricoes);

        if (!"Aguardando encerramento do secretário".equals(statusAtual)) {
            return "redirect:/ofertas?erro=A oferta não está no status adequado para encerramento.";
        }

        // Lê a instrução do banco de dados (Tabela Configuracao)
        String instrucoesDoBanco = configuracaoRepository.findById("INSTRUCOES_ENCERRAMENTO")
                .map(Configuracao::getValor)
                .orElse("Instruções padrão do sistema.");

        model.addAttribute("oferta", oferta);
        model.addAttribute("instrucoes", instrucoesDoBanco);
        return "ofertas/encerrar";
    }

    // POST: Finaliza a oferta e gera os logs
    @Transactional
    @PostMapping("/{id}/encerrar")
    public String processarEncerramento(@PathVariable("id") UUID id,
                                        @AuthenticationPrincipal UsuarioUserDetails usuarioLogado,
                                        RedirectAttributes redirectAttributes) {
        Oferta oferta = ofertaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oferta inválida."));

        List<Inscricao> inscricoes = inscricaoRepository.findByOferta(oferta);
        String statusAtual = calcularStatus(oferta, inscricoes);

        if (!"Aguardando encerramento do secretário".equals(statusAtual)) {
            redirectAttributes.addFlashAttribute("erroGeral", "Operação não permitida no momento.");
            return "redirect:/ofertas";
        }

        // 1. Marca a oferta como encerrada
        oferta.setDataEncerramento(LocalDateTime.now());
        oferta.setUsuarioEncerramento(usuarioLogado.getUsuario());
        ofertaRepository.save(oferta);

        // 2. Modifica todos os alunos para CONCLUÍDO e grava no histórico
        for (Inscricao inscricao : inscricoes) {
            inscricao.setStatus(StatusInscricao.CONCLUIDO);
            inscricaoRepository.save(inscricao);
            logStatusService.registrarLog(inscricao, StatusInscricao.CONCLUIDO, usuarioLogado.getUsuario());
        }

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Oferta encerrada com sucesso!");
        return "redirect:/ofertas";
    }
}

