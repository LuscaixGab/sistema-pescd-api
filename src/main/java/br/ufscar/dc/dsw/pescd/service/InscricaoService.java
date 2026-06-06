package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.model.Perfil;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.OfertaRepository;
import br.ufscar.dc.dsw.pescd.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class InscricaoService {

    private final UsuarioRepository usuarioRepository;
    private final OfertaRepository ofertaRepository;
    private final InscricaoRepository inscricaoRepository;
    private final PasswordEncoder passwordEncoder;

    public InscricaoService(UsuarioRepository usuarioRepository,
                            OfertaRepository ofertaRepository,
                            InscricaoRepository inscricaoRepository,
                            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.ofertaRepository = ofertaRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void processarAlunosCsv(UUID ofertaId, MultipartFile file) throws Exception {
        Oferta oferta = ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> new IllegalArgumentException("Oferta não encontrada."));

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String linha;
            boolean primeiraLinha = true;

            while ((linha = br.readLine()) != null) {
                // Ignora o cabeçalho
                if (primeiraLinha) {
                    primeiraLinha = false;
                    continue;
                }

                if (linha.trim().isEmpty()) continue;

                String[] dados = linha.split(",");
                if (dados.length < 3) continue;

                String ra = dados[0].trim();
                String nomeCompleto = dados[1].trim();
                String email = dados[2].trim();

                // FIX RN-3: Verifica se o aluno já existe usando o método do Repository que busca por RA ou E-mail
                Usuario aluno = usuarioRepository.findByNomeUsuarioOrEmail(ra, email).orElse(null);

                if (aluno == null) {
                    // Cadastra novo aluno usando e-mail como nome de usuário e RA como senha
                    aluno = new Usuario();
                    aluno.setNomeCompleto(nomeCompleto);
                    aluno.setEmail(email);
                    aluno.setNomeUsuario(email);
                    aluno.setSenha(passwordEncoder.encode(ra)); // Criptografia de senha
                    aluno.setPerfil(Perfil.ALUNO);
                    aluno = usuarioRepository.save(aluno);
                }

                // Verifica se este aluno já está matriculado nessa oferta específica
                final UUID alunoId = aluno.getId();
                boolean jaInscrito = inscricaoRepository.findByOferta(oferta).stream()
                        .anyMatch(inscricao -> inscricao.getAluno().getId().equals(alunoId));

                if (!jaInscrito) {
                    // Cria a inscrição apenas se não existir
                    Inscricao inscricao = new Inscricao(null, aluno, oferta, StatusInscricao.NAO_ENVIADO);
                    inscricaoRepository.save(inscricao);
                }
            }
        }
    }
}