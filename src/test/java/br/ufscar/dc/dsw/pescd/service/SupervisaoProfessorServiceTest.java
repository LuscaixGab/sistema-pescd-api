package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.dto.SupervisaoAlunoResumo;
import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.model.Perfil;
import br.ufscar.dc.dsw.pescd.model.PlanoTrabalho;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.PlanoTrabalhoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupervisaoProfessorServiceTest {

    @Mock
    private PlanoTrabalhoRepository planoTrabalhoRepository;

    private SupervisaoProfessorService supervisaoProfessorService;

    @BeforeEach
    void setUp() {
        supervisaoProfessorService = new SupervisaoProfessorService(planoTrabalhoRepository);
    }

    @Test
    void deveIgnorarPlanosSemInscricaoNaSupervisao() {
        Usuario professor = usuario(UUID.randomUUID(), "Professor", Perfil.PROFESSOR);
        PlanoTrabalho planoInvalido = mock(PlanoTrabalho.class);
        when(planoInvalido.getInscricao()).thenReturn(null);

        Usuario aluno = usuario(UUID.randomUUID(), "Aluno", Perfil.ALUNO);
        Oferta oferta = oferta(professor);
        Inscricao inscricao = new Inscricao(UUID.randomUUID(), aluno, oferta, StatusInscricao.PLANO_ENVIADO);
        PlanoTrabalho planoValido = mock(PlanoTrabalho.class);
        when(planoValido.getId()).thenReturn(UUID.randomUUID());
        when(planoValido.getInscricao()).thenReturn(inscricao);
        when(planoValido.getNomeDisciplina()).thenReturn("Web 1");

        when(planoTrabalhoRepository.findByProfessorSupervisor(professor))
                .thenReturn(List.of(planoInvalido, planoValido));

        List<SupervisaoAlunoResumo> resultados = supervisaoProfessorService.listarAlunosSupervisionados(professor);

        assertEquals(1, resultados.size());
        assertEquals("Aluno", resultados.get(0).getNomeAluno());
        assertEquals("Web 1", resultados.get(0).getNomeDisciplina());
    }

    private Usuario usuario(UUID id, String nomeCompleto, Perfil perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNomeCompleto(nomeCompleto);
        usuario.setEmail(nomeCompleto.toLowerCase() + "@ufscar.br");
        usuario.setNomeUsuario(nomeCompleto.toLowerCase());
        usuario.setSenha("hash");
        usuario.setPerfil(perfil);
        return usuario;
    }

    private Oferta oferta(Usuario professor) {
        Oferta oferta = new Oferta();
        oferta.setId(UUID.randomUUID());
        oferta.setNomeOferta("Desenvolvimento Web");
        oferta.setSemestre("2026/1");
        oferta.setDataInicio(LocalDate.now());
        oferta.setDataFim(LocalDate.now().plusDays(30));
        oferta.setProfessorResponsavel(professor);
        oferta.setUsuarioCriador(professor);
        return oferta;
    }
}
