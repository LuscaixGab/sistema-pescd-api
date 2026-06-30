package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.dto.AdministradorDTO;
import br.ufscar.dc.dsw.pescd.exception.RegraNegocioException;
import br.ufscar.dc.dsw.pescd.exception.UsuarioNaoEncontradoException;
import br.ufscar.dc.dsw.pescd.model.Perfil;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.UsuarioRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class AdministradorService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdministradorService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll(Sort.by(Sort.Direction.ASC, "nomeCompleto"));
    }

    @Transactional(readOnly = true)
    public List<AdministradorDTO> listarUsuariosDTO() {
        return listarUsuarios().stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdministradorDTO carregarFormulario(UUID id) {
        return toDTO(buscarUsuario(id));
    }

    @Transactional(readOnly = true)
    public AdministradorDTO buscarUsuarioDTO(UUID id) {
        return toDTO(buscarUsuario(id));
    }

    @Transactional
    public Usuario criarUsuario(AdministradorDTO dto) {
        validarFormulario(dto, null, true);

        Usuario usuario = new Usuario();
        aplicarDados(usuario, dto, null, true);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizarUsuario(UUID id, AdministradorDTO dto) {
        Usuario usuario = buscarUsuario(id);
        validarFormulario(dto, id, false);

        if (usuario.getPerfil() == Perfil.ADMINISTRADOR
                && dto.getPerfil() != Perfil.ADMINISTRADOR
                && usuarioRepository.countByPerfil(Perfil.ADMINISTRADOR) <= 1) {
            throw new RegraNegocioException("Nao e permitido remover o ultimo administrador do sistema.");
        }

        aplicarDados(usuario, dto, usuario.getSenha(), false);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void excluirUsuario(UUID id, UUID usuarioLogadoId) {
        Usuario usuario = buscarUsuario(id);

        if (usuario.getId().equals(usuarioLogadoId)) {
            throw new RegraNegocioException("Voce nao pode excluir o proprio usuario.");
        }

        if (usuario.getPerfil() == Perfil.ADMINISTRADOR
                && usuarioRepository.countByPerfil(Perfil.ADMINISTRADOR) <= 1) {
            throw new RegraNegocioException("Nao e permitido excluir o ultimo administrador do sistema.");
        }

        usuarioRepository.delete(usuario);
    }

    @Transactional(readOnly = true)
    public List<Perfil> listarPerfis() {
        return List.of(Perfil.values());
    }

    private Usuario buscarUsuario(UUID id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuario nao encontrado."));
    }

    private void validarFormulario(AdministradorDTO dto, UUID idAtual, boolean novoCadastro) {
        validarUnicidade(dto, idAtual);

        if (novoCadastro && !StringUtils.hasText(dto.getSenha())) {
            throw new RegraNegocioException("A senha e obrigatoria para novo usuario.");
        }
    }

    private void validarUnicidade(AdministradorDTO dto, UUID idAtual) {
        String emailNormalizado = dto.getEmail().trim();
        String nomeUsuarioNormalizado = dto.getNomeUsuario().trim();

        usuarioRepository.findByEmail(emailNormalizado)
                .filter(usuario -> idAtual == null || !usuario.getId().equals(idAtual))
                .ifPresent(usuario -> {
                    throw new RegraNegocioException("Ja existe um usuario com este e-mail.");
                });

        usuarioRepository.findByNomeUsuario(nomeUsuarioNormalizado)
                .filter(usuario -> idAtual == null || !usuario.getId().equals(idAtual))
                .ifPresent(usuario -> {
                    throw new RegraNegocioException("Ja existe um usuario com este nome de usuario.");
                });
    }

    private void aplicarDados(Usuario usuario, AdministradorDTO dto, String senhaAtual, boolean novoCadastro) {
        usuario.setNomeCompleto(dto.getNomeCompleto().trim());
        usuario.setEmail(dto.getEmail().trim());
        usuario.setNomeUsuario(dto.getNomeUsuario().trim());
        usuario.setPerfil(dto.getPerfil());

        if (StringUtils.hasText(dto.getSenha())) {
            usuario.setSenha(passwordEncoder.encode(dto.getSenha().trim()));
        } else if (novoCadastro) {
            throw new RegraNegocioException("A senha e obrigatoria para novo usuario.");
        } else {
            usuario.setSenha(senhaAtual);
        }
    }

    private AdministradorDTO toDTO(Usuario usuario) {
        AdministradorDTO dto = new AdministradorDTO();
        dto.setId(usuario.getId());
        dto.setNomeCompleto(usuario.getNomeCompleto());
        dto.setEmail(usuario.getEmail());
        dto.setNomeUsuario(usuario.getNomeUsuario());
        dto.setPerfil(usuario.getPerfil());
        dto.setSenha("");
        return dto;
    }
}
