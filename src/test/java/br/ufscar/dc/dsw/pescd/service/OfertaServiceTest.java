package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.dto.OfertaRequestDTO;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.model.Perfil;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.ConfiguracaoRepository;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.OfertaRepository;
import br.ufscar.dc.dsw.pescd.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfertaServiceTest {

    @Mock
    private OfertaRepository ofertaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private InscricaoRepository inscricaoRepository;

    @Mock
    private ConfiguracaoRepository configuracaoRepository;

    @Mock
    private LogStatusService logStatusService;

    private OfertaService ofertaService;

    @BeforeEach
    void setUp() {
        ofertaService = new OfertaService(
                ofertaRepository,
                usuarioRepository,
                inscricaoRepository,
                configuracaoRepository,
                logStatusService
        );
    }

    @Test
    void deveCriarOfertaParaSecretarioComNomePadrao() {
        Usuario secretario = usuario(UUID.randomUUID(), "Secretario Teste", Perfil.SECRETARIO);
        Usuario professor = usuario(UUID.randomUUID(), "Professor Responsavel", Perfil.PROFESSOR);
        OfertaRequestDTO request = novoRequest("", professor.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30));

        when(usuarioRepository.findById(professor.getId())).thenReturn(Optional.of(professor));
        when(ofertaRepository.save(any(Oferta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Oferta oferta = ofertaService.criarOferta(request, secretario);

        ArgumentCaptor<Oferta> captor = ArgumentCaptor.forClass(Oferta.class);
        verify(ofertaRepository).save(captor.capture());
        assertEquals("Oferta 2026/1 - Prof. Professor Responsavel", captor.getValue().getNomeOferta());
        assertEquals("2026/1", captor.getValue().getSemestre());
        assertEquals(Perfil.PROFESSOR, captor.getValue().getProfessorResponsavel().getPerfil());
        assertEquals(oferta.getNomeOferta(), captor.getValue().getNomeOferta());
    }

    @Test
    void deveBloquearCriacaoPorUsuarioSemPerfilSecretario() {
        Usuario aluno = usuario(UUID.randomUUID(), "Aluno Teste", Perfil.ALUNO);
        Usuario professor = usuario(UUID.randomUUID(), "Professor Responsavel", Perfil.PROFESSOR);
        OfertaRequestDTO request = novoRequest("Oferta Teste", professor.getId(),
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(30));

        assertThrows(IllegalArgumentException.class, () -> ofertaService.criarOferta(request, aluno));
    }

    @Test
    void deveBloquearQuandoDataFimNaoEhPosteriorADataInicio() {
        Usuario secretario = usuario(UUID.randomUUID(), "Secretario Teste", Perfil.SECRETARIO);
        Usuario professor = usuario(UUID.randomUUID(), "Professor Responsavel", Perfil.PROFESSOR);
        OfertaRequestDTO request = novoRequest("Oferta Teste", professor.getId(),
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(2));

        assertThrows(IllegalArgumentException.class, () -> ofertaService.criarOferta(request, secretario));
    }

    private OfertaRequestDTO novoRequest(String nomeOferta, UUID professorResponsavelId,
                                         LocalDate dataInicio, LocalDate dataFim) {
        OfertaRequestDTO request = new OfertaRequestDTO();
        request.setNomeOferta(nomeOferta);
        request.setCodigoDisciplina("DC101");
        request.setNomeDisciplina("Desenvolvimento de Software para Web 1");
        request.setCursoDisciplina("Ciencia da Computacao");
        request.setSemestre("2026/1");
        request.setDataInicio(dataInicio);
        request.setDataFim(dataFim);
        request.setProfessorResponsavelId(professorResponsavelId);
        return request;
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
}
