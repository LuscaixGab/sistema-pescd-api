package br.ufscar.dc.dsw.pescd.controller;

import br.ufscar.dc.dsw.pescd.dto.OfertaForm;
import br.ufscar.dc.dsw.pescd.model.Oferta;
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

@Controller
@RequestMapping("/ofertas")
@PreAuthorize("hasRole('SECRETARIO')")
public class OfertaController {

    private final OfertaService ofertaService;

    public OfertaController(OfertaService ofertaService) {
        this.ofertaService = ofertaService;
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

    // S.02 - GET: Exibir tela de adicionar alunos
    @GetMapping("/{id}/alunos")
    public String exibirAdicionarAlunos(@PathVariable("id") java.util.UUID id, Model model) {
        Oferta oferta = ofertaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Oferta inválida."));
        model.addAttribute("oferta", oferta);
        return "ofertas/adicionar-alunos";
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
}

