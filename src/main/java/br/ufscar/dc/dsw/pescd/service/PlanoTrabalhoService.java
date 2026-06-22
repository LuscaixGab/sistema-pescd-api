package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.dto.PlanoTrabalhoForm;
import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.Perfil;
import br.ufscar.dc.dsw.pescd.model.PlanoTrabalho;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.PlanoTrabalhoRepository;
import br.ufscar.dc.dsw.pescd.repository.UsuarioRepository;
import br.ufscar.dc.dsw.pescd.util.UploadUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class PlanoTrabalhoService {

    private final PlanoTrabalhoRepository planoTrabalhoRepository;
    private final UsuarioRepository usuarioRepository;
    private final InscricaoRepository inscricaoRepository;
    private final LogStatusService logStatusService;

    public PlanoTrabalhoService(PlanoTrabalhoRepository planoTrabalhoRepository,
                                UsuarioRepository usuarioRepository,
                                InscricaoRepository inscricaoRepository,
                                LogStatusService logStatusService) {
        this.planoTrabalhoRepository = planoTrabalhoRepository;
        this.usuarioRepository = usuarioRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.logStatusService = logStatusService;
    }

    @Transactional(readOnly = true)
    public List<PlanoTrabalho> listarPlanosTrabalho() {
        return planoTrabalhoRepository.findAllByOrderByDataCriacaoDesc();
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarProfessores() {
        return usuarioRepository.findAllByPerfil(Perfil.PROFESSOR);
    }

    @Transactional(readOnly = true)
    public Usuario buscarProfessor(UUID professorSupervisorId) {
        return usuarioRepository.findById(professorSupervisorId)
                .filter(usuario -> usuario.getPerfil() == Perfil.PROFESSOR)
                .orElseThrow(() -> new IllegalArgumentException("Professor supervisor inválido."));
    }

    @Transactional
    public PlanoTrabalho criarPlanoTrabalho(PlanoTrabalhoForm planoTrabalhoForm, Usuario alunoLogado) {
        if (planoTrabalhoForm.getInscricaoId() == null) {
            throw new IllegalArgumentException("A inscrição do plano é obrigatória.");
        }

        Inscricao inscricao = inscricaoRepository.findById(planoTrabalhoForm.getInscricaoId())
                .orElseThrow(() -> new IllegalArgumentException("Inscrição não encontrada."));

        if (!inscricao.getAluno().getId().equals(alunoLogado.getId())) {
            throw new IllegalArgumentException("Você não pode enviar plano para esta inscrição.");
        }

        if (inscricao.getStatus() != StatusInscricao.NAO_ENVIADO) {
            throw new IllegalArgumentException("Esta inscrição não está apta para envio de plano.");
        }

        Usuario professorSupervisor = buscarProfessor(planoTrabalhoForm.getProfessorSupervisorId());
        String arquivoPlanoSalvo = salvarArquivoPlano(planoTrabalhoForm.getArquivoPlano());

        PlanoTrabalho planoTrabalho = planoTrabalhoRepository.findByInscricao(inscricao)
                .orElseGet(() -> new PlanoTrabalho(null, "", "", "", "", null, inscricao));

        planoTrabalho.setCodigoDisciplina(planoTrabalhoForm.getCodigoDisciplina().trim());
        planoTrabalho.setNomeDisciplina(planoTrabalhoForm.getNomeDisciplina().trim());
        planoTrabalho.setCursoDisciplina(planoTrabalhoForm.getCursoDisciplina().trim());
        planoTrabalho.setArquivoPlano(arquivoPlanoSalvo);
        planoTrabalho.setProfessorSupervisor(professorSupervisor);
        planoTrabalho.setInscricao(inscricao);

        PlanoTrabalho planoSalvo = planoTrabalhoRepository.save(planoTrabalho);

        inscricao.setStatus(StatusInscricao.PLANO_ENVIADO);
        inscricaoRepository.save(inscricao);

        // Dispara o log registrando que o próprio aluno foi o responsável pela mudança
        logStatusService.registrarLog(inscricao, StatusInscricao.PLANO_ENVIADO, alunoLogado);

        return planoSalvo;
    }

    private String salvarArquivoPlano(MultipartFile arquivoPlano) {
        UploadUtils.validarPdfObrigatorio(arquivoPlano, "o plano");

        String nomeOriginal = StringUtils.cleanPath(arquivoPlano.getOriginalFilename() == null
                ? ""
                : arquivoPlano.getOriginalFilename());
        if (!StringUtils.hasText(nomeOriginal)) {
            throw new IllegalArgumentException("O nome do arquivo do plano é inválido.");
        }

        String nomeArquivo = UUID.randomUUID() + "_" + nomeOriginal;
        Path diretorioUpload = Paths.get("uploads", "planos-trabalho");

        try {
            Files.createDirectories(diretorioUpload);
            Path destino = diretorioUpload.resolve(nomeArquivo);

            try (InputStream inputStream = arquivoPlano.getInputStream()) {
                Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
            }

            return destino.toString().replace("\\", "/");
        } catch (IOException exception) {
            throw new IllegalStateException("Não foi possível salvar o arquivo do plano.", exception);
        }
    }

    public List<PlanoTrabalho> listarPlanosPendentes(Usuario professor) {
        return planoTrabalhoRepository.findByProfessorSupervisorAndInscricaoStatus(professor, StatusInscricao.PLANO_ENVIADO);
    }

    public PlanoTrabalho buscarPlanoParaAvaliacao(UUID planoId, Usuario professor) {
        PlanoTrabalho plano = planoTrabalhoRepository.findById(planoId)
                .orElseThrow(() -> new IllegalArgumentException("Plano não encontrado."));

        if (!plano.getProfessorSupervisor().getId().equals(professor.getId())) {
            throw new IllegalArgumentException("Você não tem permissão para acessar este plano.");
        }
        return plano;
    }

    @Transactional
    public void avaliarPlano(UUID planoId, String parecer, String acao, Usuario professor) {
        if (parecer == null || parecer.trim().isEmpty()) {
            throw new IllegalArgumentException("O parecer é obrigatório. Por favor, insira um comentário.");
        }

        PlanoTrabalho plano = buscarPlanoParaAvaliacao(planoId, professor);

        if (plano.getInscricao().getStatus() != StatusInscricao.PLANO_ENVIADO) {
            throw new IllegalArgumentException("Este plano não está pendente de avaliação.");
        }

        plano.setParecer(parecer.trim());
        planoTrabalhoRepository.save(plano);

        Inscricao inscricao = plano.getInscricao();
        if ("aprovar".equalsIgnoreCase(acao)) {
            inscricao.setStatus(StatusInscricao.PLANO_APROVADO);
        } else if ("reprovar".equalsIgnoreCase(acao)) {
            inscricao.setStatus(StatusInscricao.PLANO_REPROVADO);
        } else {
            throw new IllegalArgumentException("Ação de avaliação inválida.");
        }

        inscricaoRepository.save(inscricao);
        logStatusService.registrarLog(inscricao, inscricao.getStatus(), professor);
    }
}
