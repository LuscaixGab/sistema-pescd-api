package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.dto.OfertaForm;
import br.ufscar.dc.dsw.pescd.dto.OfertaRequestDTO;
import br.ufscar.dc.dsw.pescd.model.*;
import br.ufscar.dc.dsw.pescd.repository.ConfiguracaoRepository;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.OfertaRepository;
import br.ufscar.dc.dsw.pescd.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class OfertaService {

    // Antes a maioria da lógica estava no Controller,
    // agora refatorei para que os métodos como calcularStatusDinamicamente()
    // estejam nesse Service.

    // Injeção apenas por construtor (tirei os autowired)
    private final OfertaRepository ofertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscricaoRepository inscricaoRepository; // Para a S.03/S.04
    private final ConfiguracaoRepository configuracaoRepository; // Para a S.04
    private final LogStatusService logStatusService; // Para a S.04

    public OfertaService(OfertaRepository ofertaRepository,
                         UsuarioRepository usuarioRepository,
                         InscricaoRepository inscricaoRepository,
                         ConfiguracaoRepository configuracaoRepository,
                         LogStatusService logStatusService) {
        this.ofertaRepository = ofertaRepository;
        this.usuarioRepository = usuarioRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.configuracaoRepository = configuracaoRepository;
        this.logStatusService = logStatusService;
    }

    public List<Oferta> listarOfertas() {
        return ofertaRepository.findAllByOrderByDataCriacaoDesc();
    }

    public List<Usuario> listarProfessores() {
        return usuarioRepository.findAllByPerfil(Perfil.PROFESSOR);
    }

    public Usuario buscarProfessor(UUID professorResponsavelId) {
        return usuarioRepository.findById(professorResponsavelId)
                .filter(usuario -> usuario.getPerfil() == Perfil.PROFESSOR)
                .orElseThrow(() -> new IllegalArgumentException("Selecione um professor responsável válido."));
    }

    @Transactional
    public Oferta criarOferta(OfertaForm ofertaForm, Usuario usuarioCriador) {
        return criarOfertaInterna(
                ofertaForm.getNomeOferta(),
                ofertaForm.getCodigoDisciplina(),
                ofertaForm.getNomeDisciplina(),
                ofertaForm.getCursoDisciplina(),
                ofertaForm.getSemestre(),
                ofertaForm.getDataInicio(),
                ofertaForm.getDataFim(),
                ofertaForm.getProfessorResponsavelId(),
                usuarioCriador);
    }

    @Transactional
    public Oferta criarOferta(OfertaRequestDTO ofertaRequest, Usuario usuarioCriador) {
        return criarOfertaInterna(
                ofertaRequest.getNomeOferta(),
                ofertaRequest.getCodigoDisciplina(),
                ofertaRequest.getNomeDisciplina(),
                ofertaRequest.getCursoDisciplina(),
                ofertaRequest.getSemestre(),
                ofertaRequest.getDataInicio(),
                ofertaRequest.getDataFim(),
                ofertaRequest.getProfessorResponsavelId(),
                usuarioCriador);
    }

    private Oferta criarOfertaInterna(String nomeOferta,
                                      String codigoDisciplina,
                                      String nomeDisciplina,
                                      String cursoDisciplina,
                                      String semestre,
                                      LocalDate dataInicio,
                                      LocalDate dataFim,
                                      UUID professorResponsavelId,
                                      Usuario usuarioCriador) {
        if (usuarioCriador == null || usuarioCriador.getPerfil() != Perfil.SECRETARIO) {
            throw new IllegalArgumentException("Somente um secretário pode criar uma oferta.");
        }

        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("As datas de início e fim são obrigatórias.");
        }
        if (!dataFim.isAfter(dataInicio)) {
            throw new IllegalArgumentException("A data de fim deve ser depois da data de início.");
        }

        Usuario professorResponsavel = buscarProfessor(professorResponsavelId);

        Oferta oferta = new Oferta();
        oferta.setNomeOferta(StringUtils.hasText(nomeOferta)
                ? nomeOferta.trim()
                : nomeDisciplina.trim());
        oferta.setCodigoDisciplina(codigoDisciplina.trim());
        oferta.setNomeDisciplina(nomeDisciplina.trim());
        oferta.setCursoDisciplina(cursoDisciplina.trim());
        oferta.setSemestre(semestre.trim());
        oferta.setDataInicio(dataInicio);
        oferta.setDataFim(dataFim);
        oferta.setProfessorResponsavel(professorResponsavel);
        oferta.setUsuarioCriador(usuarioCriador);

        return ofertaRepository.save(oferta);
    }

    // Métodos de acompanhamento e encerramento (S.03 e S.04)
    public Map<UUID, String> mapearStatusDasOfertas(List<Oferta> ofertas) {
        Map<UUID, String> statusOfertas = new HashMap<>();
        for (Oferta oferta : ofertas) {
            List<Inscricao> inscricoes = inscricaoRepository.findByOferta(oferta);
            statusOfertas.put(oferta.getId(), calcularStatusDinamicamente(oferta, inscricoes));
        }
        return statusOfertas;
    }

    public String calcularStatusDinamicamente(Oferta oferta, List<Inscricao> inscricoes) {
        if (oferta.getDataEncerramento() != null || oferta.getStatusOferta() == StatusOferta.CONCLUIDA) return "Concluída";
        if (oferta.getStatusOferta() == StatusOferta.AGUARDANDO_ENCERRAMENTO_SECRETARIO) {
            return "Aguardando encerramento do secretário";
        }

        LocalDate hoje = LocalDate.now();
        if (hoje.isBefore(oferta.getDataInicio())) return "Aguardando início";
        if (inscricoes.isEmpty()) return hoje.isAfter(oferta.getDataFim()) ? "Concluída" : "Em andamento";

        boolean todosConcluidos = true;
        for (Inscricao inscricao : inscricoes) {
            if (inscricao.getStatus() != StatusInscricao.CONCLUIDO_PELO_RESPONSAVEL &&
                    inscricao.getStatus() != StatusInscricao.CONCLUIDO) {
                todosConcluidos = false;
                break;
            }
        }

        if (todosConcluidos) return "Aguardando encerramento do responsável";
        if (hoje.isAfter(oferta.getDataFim())) return "Em atraso";
        return "Em andamento";
    }

    public String obterInstrucoesEncerramento() {
        return configuracaoRepository.findById("INSTRUCOES_ENCERRAMENTO")
                .map(Configuracao::getValor)
                .orElse("Instruções padrão do sistema.");
    }

    @Transactional
    public void encerrarOfertaOficialmente(Oferta oferta, Usuario usuarioLogado, List<Inscricao> inscricoes) {
        oferta.setStatusOferta(StatusOferta.CONCLUIDA);
        oferta.setDataEncerramento(LocalDateTime.now());
        oferta.setUsuarioEncerramento(usuarioLogado);
        ofertaRepository.save(oferta);

        for (Inscricao inscricao : inscricoes) {
            inscricao.setStatus(StatusInscricao.CONCLUIDO);
            inscricaoRepository.save(inscricao);
            logStatusService.registrarLog(inscricao, StatusInscricao.CONCLUIDO, usuarioLogado);
        }
    }
}
