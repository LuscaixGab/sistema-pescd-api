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

    public PlanoTrabalhoService(PlanoTrabalhoRepository planoTrabalhoRepository,
                                UsuarioRepository usuarioRepository,
                                InscricaoRepository inscricaoRepository) {
        this.planoTrabalhoRepository = planoTrabalhoRepository;
        this.usuarioRepository = usuarioRepository;
        this.inscricaoRepository = inscricaoRepository;
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

        return planoSalvo;
    }

    private String salvarArquivoPlano(MultipartFile arquivoPlano) {
        if (arquivoPlano == null || arquivoPlano.isEmpty()) {
            throw new IllegalArgumentException("O arquivo do plano é obrigatório.");
        }

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
}
