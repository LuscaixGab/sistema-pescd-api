package br.ufscar.dc.dsw.pescd.config;

import br.ufscar.dc.dsw.pescd.model.Perfil;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.PlanoTrabalho;
import br.ufscar.dc.dsw.pescd.repository.UsuarioRepository;
import br.ufscar.dc.dsw.pescd.repository.OfertaRepository;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.PlanoTrabalhoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final OfertaRepository ofertaRepository;
    private final InscricaoRepository inscricaoRepository;
    private final PlanoTrabalhoRepository planoTrabalhoRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(UsuarioRepository usuarioRepository, 
                          OfertaRepository ofertaRepository, 
                          InscricaoRepository inscricaoRepository,
                          PlanoTrabalhoRepository planoTrabalhoRepository,
                          PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.ofertaRepository = ofertaRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.planoTrabalhoRepository = planoTrabalhoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Cria os usuários base
        criarUsuarioSeNaoExistir("Administrador do Sistema", "admin@pescd.local", "admin", "admin123", Perfil.ADMINISTRADOR);
        criarUsuarioSeNaoExistir("Secretário do Sistema", "secretario@pescd.local", "secretario", "secretario123", Perfil.SECRETARIO);
        criarUsuarioSeNaoExistir("Professor Responsável", "professor@pescd.local", "professor", "professor123", Perfil.PROFESSOR);
        criarUsuarioSeNaoExistir("Aluno do Sistema", "aluno@pescd.local", "aluno", "aluno123", Perfil.ALUNO);

        // Recupera as instâncias necessárias para os relacionamentos
        Usuario professor = usuarioRepository.findByNomeUsuario("professor").orElseThrow();
        Usuario secretario = usuarioRepository.findByNomeUsuario("secretario").orElseThrow();
        Usuario aluno = usuarioRepository.findByNomeUsuario("aluno").orElseThrow();

        // Cria as Ofertas
        if (ofertaRepository.count() == 0) {
            Oferta web1 = new Oferta();
            web1.setNomeOferta("Desenvolvimento de Software para Web 1");
            web1.setSemestre("2026/1");
            web1.setDataInicio(LocalDate.of(2026, 3, 1));
            web1.setDataFim(LocalDate.of(2026, 7, 15));
            web1.setProfessorResponsavel(professor);
            web1.setUsuarioCriador(secretario);
            ofertaRepository.save(web1);

            Oferta embarcados = new Oferta();
            embarcados.setNomeOferta("Sistemas Embarcados");
            embarcados.setSemestre("2026/1");
            embarcados.setDataInicio(LocalDate.of(2026, 3, 1));
            embarcados.setDataFim(LocalDate.of(2026, 7, 15));
            embarcados.setProfessorResponsavel(professor);
            embarcados.setUsuarioCriador(secretario);
            ofertaRepository.save(embarcados);

            // NOVA OFERTA: Cenário da AL.04
            Oferta controle = new Oferta();
            controle.setNomeOferta("Sistemas de Controle 1");
            controle.setSemestre("2026/1");
            controle.setDataInicio(LocalDate.of(2026, 3, 1));
            controle.setDataFim(LocalDate.of(2026, 7, 15));
            controle.setProfessorResponsavel(professor);
            controle.setUsuarioCriador(secretario);
            ofertaRepository.save(controle);

            // Cria as Inscrições
            if (inscricaoRepository.count() == 0) {
                Inscricao inscricaoWeb1 = new Inscricao(null, aluno, web1, StatusInscricao.NAO_ENVIADO);
                inscricaoRepository.save(inscricaoWeb1);

                Inscricao inscricaoEmbarcados = new Inscricao(null, aluno, embarcados, StatusInscricao.NAO_ENVIADO);
                inscricaoRepository.save(inscricaoEmbarcados);

                // Inscrição avançada com Plano já aprovado
                Inscricao inscricaoControle = new Inscricao(null, aluno, controle, StatusInscricao.PLANO_APROVADO);
                inscricaoRepository.save(inscricaoControle);
                
                // Salva o Plano de Trabalho mockado
                PlanoTrabalho planoControle = new PlanoTrabalho(
                        null,
                        "ENG104",
                        "Sistemas de Controle 1",
                        "Engenharia de Computação",
                        "plano_controle_aluno.pdf",
                        professor,
                        inscricaoControle
                );
                
                planoControle.setParecer("Plano aprovado. O aluno demonstrou bom domínio das ferramentas de simulação (Scilab/Xcos) propostas para as aulas práticas de resposta ao degrau.");
                planoTrabalhoRepository.save(planoControle);
            }
        }
    }

    private void criarUsuarioSeNaoExistir(String nomeCompleto, String email, String nomeUsuario, String senha, Perfil perfil) {
        boolean usuarioExiste = usuarioRepository.findByNomeUsuario(nomeUsuario).isPresent()
                || usuarioRepository.findByEmail(email).isPresent();

        if (usuarioExiste) {
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(nomeCompleto);
        usuario.setEmail(email);
        usuario.setNomeUsuario(nomeUsuario);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setPerfil(perfil);
        usuarioRepository.save(usuario);
    }
}