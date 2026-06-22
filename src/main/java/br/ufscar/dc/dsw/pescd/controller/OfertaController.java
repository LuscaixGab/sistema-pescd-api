package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.dto.OfertaForm;
import br.ufscar.dc.dsw.pescd.model.*;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.OfertaRepository;
import br.ufscar.dc.dsw.pescd.security.UsuarioUserDetails;
import br.ufscar.dc.dsw.pescd.service.InscricaoService;
import br.ufscar.dc.dsw.pescd.service.OfertaService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ofertas")
public class OfertaController {

    // Injeção apenas por construtor (tirei os autowired)
    private final OfertaService ofertaService;
    private final OfertaRepository ofertaRepository;
    private final InscricaoRepository inscricaoRepository;
    private final InscricaoService inscricaoService;
    private final br.ufscar.dc.dsw.pescd.repository.UsuarioRepository usuarioRepository;
    private final br.ufscar.dc.dsw.pescd.repository.PlanoTrabalhoRepository planoTrabalhoRepository;
    private final br.ufscar.dc.dsw.pescd.repository.DocumentacaoAulaRepository documentacaoAulaRepository;
    private final br.ufscar.dc.dsw.pescd.repository.RelatorioFinalRepository relatorioFinalRepository;
    private final br.ufscar.dc.dsw.pescd.repository.LogStatusInscricaoRepository logStatusInscricaoRepository;

    public OfertaController(OfertaService ofertaService,
                            OfertaRepository ofertaRepository,
                            InscricaoRepository inscricaoRepository,
                            InscricaoService inscricaoService,
                            br.ufscar.dc.dsw.pescd.repository.UsuarioRepository usuarioRepository,
                            br.ufscar.dc.dsw.pescd.repository.PlanoTrabalhoRepository planoTrabalhoRepository,
                            br.ufscar.dc.dsw.pescd.repository.DocumentacaoAulaRepository documentacaoAulaRepository,
                            br.ufscar.dc.dsw.pescd.repository.RelatorioFinalRepository relatorioFinalRepository,
                            br.ufscar.dc.dsw.pescd.repository.LogStatusInscricaoRepository logStatusInscricaoRepository) {
        this.ofertaService = ofertaService;
        this.ofertaRepository = ofertaRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.inscricaoService = inscricaoService;
        this.usuarioRepository = usuarioRepository;
        this.planoTrabalhoRepository = planoTrabalhoRepository;
        this.documentacaoAulaRepository = documentacaoAulaRepository;
        this.relatorioFinalRepository = relatorioFinalRepository;
        this.logStatusInscricaoRepository = logStatusInscricaoRepository;
    }

    // Listagem geral (S.03 e PR.04)
    @GetMapping
    public String listar(@AuthenticationPrincipal UsuarioUserDetails usuarioLogado, Model model) {
        Usuario usuario = usuarioLogado.getUsuario();
        List<Oferta> ofertas;

        if (usuario.getPerfil() == Perfil.PROFESSOR) {
            ofertas = ofertaRepository.findByProfessorResponsavel(usuario);
        } else {
            ofertas = ofertaRepository.findAll();
        }

        model.addAttribute("ofertas", ofertas);
        model.addAttribute("statusOfertas", ofertaService.mapearStatusDasOfertas(ofertas));
        model.addAttribute("perfilUsuario", usuario.getPerfil().name());

        return "ofertas/lista";
    }

    // Criação de ofertas
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

    // Gestão e importação de alunos (S.02)
    @GetMapping("/{id}/alunos")
    public String exibirAdicionarAlunos(@PathVariable("id") java.util.UUID id, Model model) {
        Oferta oferta = ofertaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oferta inválida."));

        List<Usuario> todosAlunos = usuarioRepository.findByPerfilOrderByNomeCompletoAsc(Perfil.ALUNO);
        List<Inscricao> inscricoesAtuais = inscricaoRepository.findByOferta(oferta);

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

        if (alunosSelecionados == null) {
            alunosSelecionados = new ArrayList<>();
        }

        List<Inscricao> inscricoesAtuais = inscricaoRepository.findByOferta(oferta);

        for (Inscricao inscricao : inscricoesAtuais) {
            if (!alunosSelecionados.contains(inscricao.getAluno().getId())) {
                inscricaoRepository.delete(inscricao);
            }
        }

        List<UUID> idsAtuais = inscricoesAtuais.stream()
                .map(i -> i.getAluno().getId())
                .collect(Collectors.toList());

        for (UUID alunoId : alunosSelecionados) {
            if (!idsAtuais.contains(alunoId)) {
                Usuario aluno = usuarioRepository.findById(alunoId).orElseThrow();
                Inscricao novaInscricao = new Inscricao(null, aluno, oferta, StatusInscricao.NAO_ENVIADO);
                inscricaoRepository.save(novaInscricao);
            }
        }

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Matrículas atualizadas com sucesso!");
        return "redirect:/ofertas/" + id + "/alunos";
    }

    @PostMapping("/{id}/alunos/upload")
    public String carregarFicheiroAlunos(@PathVariable("id") UUID id,
                                         @RequestParam("file") MultipartFile file,
                                         RedirectAttributes redirectAttributes) {
        try {
            // Se o arquivo for inválido, o Service vai lançar um erro antes mesmo de tentar ler
            inscricaoService.processarAlunosCsv(id, file);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Alunos cadastrados e inscritos com sucesso!");

        } catch (IllegalArgumentException e) {
            // Captura os erros de validação (extensão, tamanho, vazio)
            redirectAttributes.addFlashAttribute("erroGeral", e.getMessage());
        } catch (Exception e) {
            // Captura qualquer outro erro inesperado do sistema
            redirectAttributes.addFlashAttribute("erroGeral", "Erro ao processar o arquivo: " + e.getMessage());
        }

        return "redirect:/ofertas/" + id + "/alunos";
    }

    // Dashboard de acompanhamento (S.03 / PR.04)
    @GetMapping("/{id}/acompanhamento")
    public String exibirAcompanhamentoOferta(@PathVariable("id") UUID id, Model model) {
        Oferta oferta = ofertaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oferta inválida."));

        List<Inscricao> inscricoes = inscricaoRepository.findByOferta(oferta);

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

    @GetMapping("/{ofertaId}/alunos/{inscricaoId}/detalhes")
    public String verDetalhesAluno(@PathVariable UUID ofertaId, @PathVariable UUID inscricaoId, Model model) {
        Inscricao inscricao = inscricaoRepository.findById(inscricaoId)
                .orElseThrow(() -> new IllegalArgumentException("Inscrição não encontrada."));

        br.ufscar.dc.dsw.pescd.model.PlanoTrabalho plano = planoTrabalhoRepository.findByInscricao(inscricao).orElse(null);
        br.ufscar.dc.dsw.pescd.model.DocumentacaoAula documentacao = documentacaoAulaRepository.findByInscricaoId(inscricaoId).orElse(null);
        br.ufscar.dc.dsw.pescd.model.RelatorioFinal relatorio = relatorioFinalRepository.findByInscricao(inscricao).orElse(null);

        List<br.ufscar.dc.dsw.pescd.model.LogStatusInscricao> logs = logStatusInscricaoRepository.findByInscricaoOrderByDataMudancaDesc(inscricao);

        model.addAttribute("oferta", inscricao.getOferta());
        model.addAttribute("inscricao", inscricao);
        model.addAttribute("plano", plano);
        model.addAttribute("documentacao", documentacao);
        model.addAttribute("relatorio", relatorio);
        model.addAttribute("logs", logs);

        return "ofertas/aluno-detalhes";
    }

    // Encerramento de oferta (S.04)
    @GetMapping("/{id}/encerrar")
    public String exibirConfirmacaoEncerramento(@PathVariable("id") UUID id, Model model) {
        Oferta oferta = ofertaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oferta inválida."));

        List<Inscricao> inscricoes = inscricaoRepository.findByOferta(oferta);
        String statusAtual = ofertaService.calcularStatusDinamicamente(oferta, inscricoes);

        if (!"Aguardando encerramento do secretário".equals(statusAtual)) {
            return "redirect:/ofertas?erro=A oferta não está no status adequado para encerramento.";
        }

        model.addAttribute("oferta", oferta);
        model.addAttribute("instrucoes", ofertaService.obterInstrucoesEncerramento());
        return "ofertas/encerrar";
    }

    @PostMapping("/{id}/encerrar")
    public String processarEncerramento(@PathVariable("id") UUID id,
                                        @AuthenticationPrincipal UsuarioUserDetails usuarioLogado,
                                        RedirectAttributes redirectAttributes) {
        Oferta oferta = ofertaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oferta inválida."));

        List<Inscricao> inscricoes = inscricaoRepository.findByOferta(oferta);
        String statusAtual = ofertaService.calcularStatusDinamicamente(oferta, inscricoes);

        if (!"Aguardando encerramento do secretário".equals(statusAtual)) {
            redirectAttributes.addFlashAttribute("erroGeral", "Operação não permitida no momento.");
            return "redirect:/ofertas";
        }

        ofertaService.encerrarOfertaOficialmente(oferta, usuarioLogado.getUsuario(), inscricoes);

        redirectAttributes.addFlashAttribute("mensagemSucesso", "Oferta encerrada com sucesso!");
        return "redirect:/ofertas";
    }
}