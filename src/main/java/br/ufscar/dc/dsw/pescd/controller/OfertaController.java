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
        model.addAttribute("ofertas", ofertaService.listarOfertas());
        return "ofertas/lista";
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

    @Autowired // Adicionei essa anotação se precisar injetar o serviço
    private br.ufscar.dc.dsw.pescd.service.InscricaoService inscricaoService;

    @Autowired
    private br.ufscar.dc.dsw.pescd.repository.OfertaRepository ofertaRepository;

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
}

