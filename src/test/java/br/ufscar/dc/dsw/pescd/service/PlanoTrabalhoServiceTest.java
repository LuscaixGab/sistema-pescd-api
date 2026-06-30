package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.dto.PlanoTrabalhoForm;
import br.ufscar.dc.dsw.pescd.exception.PlanoTrabalhoNaoEncontradoException;
import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.model.Perfil;
import br.ufscar.dc.dsw.pescd.model.PlanoTrabalho;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.PlanoTrabalhoRepository;
import br.ufscar.dc.dsw.pescd.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlanoTrabalhoServiceTest {

    @Mock
    private PlanoTrabalhoRepository planoTrabalhoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private InscricaoRepository inscricaoRepository;

    @Mock
    private LogStatusService logStatusService;

    private PlanoTrabalhoService planoTrabalhoService;

    @BeforeEach
    void setUp() {
        planoTrabalhoService = new PlanoTrabalhoService(
                planoTrabalhoRepository,
                usuarioRepository,
                inscricaoRepository,
                logStatusService
        );
    }

    @Test
    void deveCriarPlanoTrabalhoEAtualizarStatusDaInscricao() throws Exception {
        Usuario aluno = usuario(gerarUuid(), "Aluno Teste", Perfil.ALUNO);
        Usuario professor = usuario(gerarUuid(), "Professor Teste", Perfil.PROFESSOR);
        Oferta oferta = new Oferta();
        oferta.setId(gerarUuid());
        oferta.setNomeOferta("Web 1");
        oferta.setSemestre("2026/1");
        oferta.setDataInicio(LocalDate.now());
        oferta.setDataFim(LocalDate.now().plusDays(30));
        oferta.setProfessorResponsavel(professor);
        oferta.setUsuarioCriador(professor);

        Inscricao inscricao = new Inscricao(gerarUuid(), aluno, oferta, StatusInscricao.NAO_ENVIADO);
        UUID inscricaoId = inscricao.getId();
        UUID professorId = professor.getId();

        MockMultipartFile arquivoPlano = new MockMultipartFile(
                "arquivoPlano",
                "plano.pdf",
                "application/pdf",
                "conteudo".getBytes()
        );

        PlanoTrabalhoForm form = novoForm(inscricaoId, professorId, arquivoPlano);

        when(inscricaoRepository.findById(inscricaoId)).thenReturn(Optional.of(inscricao));
        when(usuarioRepository.findById(professorId)).thenReturn(Optional.of(professor));
        when(planoTrabalhoRepository.findByInscricao(inscricao)).thenReturn(Optional.empty());
        when(planoTrabalhoRepository.save(any(PlanoTrabalho.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PlanoTrabalho plano = planoTrabalhoService.criarPlanoTrabalho(form, aluno);
        Path arquivoSalvo = Path.of(plano.getArquivoPlano());

        try {
            assertEquals("ENG104", plano.getCodigoDisciplina());
            assertEquals(StatusInscricao.PLANO_ENVIADO, inscricao.getStatus());
            verify(logStatusService).registrarLog(inscricao, StatusInscricao.PLANO_ENVIADO, aluno);
        } finally {
            Files.deleteIfExists(arquivoSalvo);
        }
    }

    @Test
    void deveNegarAcessoAoPlanoDeOutroAluno() {
        Usuario aluno = usuario(gerarUuid(), "Aluno Teste", Perfil.ALUNO);
        Usuario outroAluno = usuario(gerarUuid(), "Outro Aluno", Perfil.ALUNO);
        Usuario professor = usuario(gerarUuid(), "Professor Teste", Perfil.PROFESSOR);
        Oferta oferta = new Oferta();
        oferta.setId(gerarUuid());
        oferta.setNomeOferta("Web 1");
        oferta.setSemestre("2026/1");
        oferta.setDataInicio(LocalDate.now());
        oferta.setDataFim(LocalDate.now().plusDays(30));
        oferta.setProfessorResponsavel(professor);
        oferta.setUsuarioCriador(professor);

        Inscricao inscricao = new Inscricao(gerarUuid(), aluno, oferta, StatusInscricao.PLANO_ENVIADO);
        PlanoTrabalho plano = new PlanoTrabalho(gerarUuid(), "ENG104", "Web 1", "CC", "uploads/plano.pdf", professor, inscricao);

        when(planoTrabalhoRepository.findById(plano.getId())).thenReturn(Optional.of(plano));

        assertThrows(AccessDeniedException.class,
                () -> planoTrabalhoService.buscarPlanoDoAluno(plano.getId(), outroAluno));
    }

    @Test
    void deveLancarExcecaoQuandoPlanoNaoExiste() {
        UUID id = gerarUuid();
        Usuario aluno = usuario(gerarUuid(), "Aluno Teste", Perfil.ALUNO);
        when(planoTrabalhoRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PlanoTrabalhoNaoEncontradoException.class,
                () -> planoTrabalhoService.buscarPlanoDoAluno(id, aluno));
    }

    private PlanoTrabalhoForm novoForm(UUID inscricaoId, UUID professorId, MockMultipartFile arquivoPlano) {
        PlanoTrabalhoForm form = new PlanoTrabalhoForm();
        form.setCodigoDisciplina("ENG104");
        form.setNomeDisciplina("Desenvolvimento Web");
        form.setCursoDisciplina("Ciencia da Computacao");
        form.setProfessorSupervisorId(professorId);
        form.setInscricaoId(inscricaoId);
        form.setArquivoPlano(arquivoPlano);
        return form;
    }

    private Usuario usuario(UUID id, String nomeCompleto, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNomeCompleto(nomeCompleto);
        usuario.setEmail(nomeCompleto.toLowerCase().replace(" ", ".") + "@ufscar.br");
        usuario.setNomeUsuario(nomeCompleto.toLowerCase().replace(" ", "."));
        usuario.setSenha("hash");
        usuario.setPerfil(perfil);
        return usuario;
    }

    private UUID gerarUuid() {
        return UUID.randomUUID();
    }
}
