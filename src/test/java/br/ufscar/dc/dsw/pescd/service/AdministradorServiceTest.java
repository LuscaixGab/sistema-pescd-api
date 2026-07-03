package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.dto.AdministradorDTO;
import br.ufscar.dc.dsw.pescd.exception.RegraNegocioException;
import br.ufscar.dc.dsw.pescd.exception.UsuarioNaoEncontradoException;
import br.ufscar.dc.dsw.pescd.model.Perfil;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdministradorServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdministradorService administradorService;

    @BeforeEach
    void setUp() {
        administradorService = new AdministradorService(usuarioRepository, passwordEncoder);
    }

    @Test
    void deveCriarUsuarioComSenhaCriptografada() {
        AdministradorDTO dto = novoDto("Maria Silva", "maria@ufscar.br", "maria.silva", "senha123", Perfil.SECRETARIO);
        when(usuarioRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(usuarioRepository.findByNomeUsuario(dto.getNomeUsuario())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("hash");
        when(usuarioRepository.save(org.mockito.ArgumentMatchers.any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario usuario = administradorService.criarUsuario(dto);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        assertEquals("Maria Silva", captor.getValue().getNomeCompleto());
        assertEquals("hash", captor.getValue().getSenha());
        assertEquals(Perfil.SECRETARIO, usuario.getPerfil());
    }

    @Test
    void deveListarUsuariosComoDTO() {
        Usuario usuario = new Usuario(UUID.randomUUID(), "Ana", "ana@ufscar.br", "ana", "hash", Perfil.ALUNO);
        when(usuarioRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(usuario));

        List<AdministradorDTO> usuarios = administradorService.listarUsuariosDTO();

        assertEquals(1, usuarios.size());
        assertEquals("Ana", usuarios.get(0).getNomeCompleto());
        assertEquals(Perfil.ALUNO, usuarios.get(0).getPerfil());
    }

    @Test
    void deveBloquearExclusaoDoProprioUsuario() {
        UUID id = UUID.randomUUID();
        Usuario usuario = new Usuario(id, "Admin", "admin@ufscar.br", "admin", "hash", Perfil.ADMINISTRADOR);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        assertThrows(RegraNegocioException.class, () -> administradorService.excluirUsuario(id, id));
        verify(usuarioRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
        UUID id = UUID.randomUUID();
        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UsuarioNaoEncontradoException.class, () -> administradorService.buscarUsuarioDTO(id));
    }

    private AdministradorDTO novoDto(String nomeCompleto, String email, String nomeUsuario, String senha, Perfil perfil) {
        AdministradorDTO dto = new AdministradorDTO();
        dto.setNomeCompleto(nomeCompleto);
        dto.setEmail(email);
        dto.setNomeUsuario(nomeUsuario);
        dto.setSenha(senha);
        dto.setPerfil(perfil);
        return dto;
    }
}
