package br.ufscar.dc.dsw.pescd.config;

import br.ufscar.dc.dsw.pescd.model.*;
import br.ufscar.dc.dsw.pescd.repository.*;
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
    private final ConfiguracaoRepository configuracaoRepository;

    public DatabaseSeeder(UsuarioRepository usuarioRepository, 
                          OfertaRepository ofertaRepository, 
                          InscricaoRepository inscricaoRepository,
                          PlanoTrabalhoRepository planoTrabalhoRepository,
                          PasswordEncoder passwordEncoder,
                          ConfiguracaoRepository configuracaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.ofertaRepository = ofertaRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.planoTrabalhoRepository = planoTrabalhoRepository;
        this.passwordEncoder = passwordEncoder;
        this.configuracaoRepository = configuracaoRepository;
    }

    @Override
    public void run(String... args) {
        // Cria os usuários base do sistema
        criarUsuarioSeNaoExistir("Administrador do Sistema", "admin@pescd.local", "admin", "admin123", Perfil.ADMINISTRADOR);
        criarUsuarioSeNaoExistir("Secretário do Sistema", "secretario@pescd.local", "secretario", "secretario123", Perfil.SECRETARIO);
        criarUsuarioSeNaoExistir("Professor Responsável", "professor@pescd.local", "professor", "professor123", Perfil.PROFESSOR);
        criarUsuarioSeNaoExistir("Aluno do Sistema", "aluno@pescd.local", "aluno", "aluno123", Perfil.ALUNO);

        // Gera volume de alunos para testar a tela de gerenciamento
        criarUsuarioSeNaoExistir("Ana Beatriz Lima", "ana@pescd.local", "ana.lima", "aluno123", Perfil.ALUNO);
        criarUsuarioSeNaoExistir("Carlos Eduardo Silva", "carlos@pescd.local", "carlos.silva", "aluno123", Perfil.ALUNO);
        criarUsuarioSeNaoExistir("Diego Alves", "diego@pescd.local", "diego.alves", "aluno123", Perfil.ALUNO);
        criarUsuarioSeNaoExistir("Fernanda Costa", "fernanda@pescd.local", "fernanda.costa", "aluno123", Perfil.ALUNO);
        criarUsuarioSeNaoExistir("Gabriel Santos", "gabriel@pescd.local", "gabriel.santos", "aluno123", Perfil.ALUNO);
        criarUsuarioSeNaoExistir("Julia Oliveira", "julia@pescd.local", "julia.oliveira", "aluno123", Perfil.ALUNO);
        criarUsuarioSeNaoExistir("Lucas Pereira", "lucas@pescd.local", "lucas.pereira", "aluno123", Perfil.ALUNO);
        criarUsuarioSeNaoExistir("Mariana Rodrigues", "mariana@pescd.local", "mariana.rodrigues", "aluno123", Perfil.ALUNO);
        criarUsuarioSeNaoExistir("Pedro Henrique Souza", "pedro@pescd.local", "pedro.souza", "aluno123", Perfil.ALUNO);
        criarUsuarioSeNaoExistir("Rafael Ferreira", "rafael@pescd.local", "rafael.ferreira", "aluno123", Perfil.ALUNO);

        // Recupera as instâncias necessárias para os relacionamentos
        Usuario professor = usuarioRepository.findByNomeUsuario("professor").orElseThrow();
        Usuario secretario = usuarioRepository.findByNomeUsuario("secretario").orElseThrow();
        Usuario alunoBase = usuarioRepository.findByNomeUsuario("aluno").orElseThrow();
        
        // Pega alguns dos alunos novos para testes de matrícula
        Usuario ana = usuarioRepository.findByNomeUsuario("ana.lima").orElseThrow();
        Usuario carlos = usuarioRepository.findByNomeUsuario("carlos.silva").orElseThrow();

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
                
                // Inscrições na Web 1
                inscricaoRepository.save(new Inscricao(null, alunoBase, web1, StatusInscricao.NAO_ENVIADO));

                // Inscrições em Embarcados
                inscricaoRepository.save(new Inscricao(null, alunoBase, embarcados, StatusInscricao.NAO_ENVIADO));

                // Inscrições em Controle 1 (Forçando o status final do professor para testar a S.04)
                Inscricao inscricaoControle = new Inscricao(null, alunoBase, controle, StatusInscricao.CONCLUIDO_PELO_RESPONSAVEL);
                inscricaoRepository.save(inscricaoControle);

                inscricaoRepository.save(new Inscricao(null, ana, controle, StatusInscricao.CONCLUIDO_PELO_RESPONSAVEL));
                inscricaoRepository.save(new Inscricao(null, carlos, controle, StatusInscricao.CONCLUIDO_PELO_RESPONSAVEL));
                
                // Salva o Plano de Trabalho mockado para o aluno base
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

            // Cria as intruções de encerramento
            if (configuracaoRepository.findById("INSTRUCOES_ENCERRAMENTO").isEmpty()) {
                String textoInstrucoes = "Instruções para encerramento da oferta:\n" +
                        "1. Certifique-se de realizar os lançamentos de notas no sistema acadêmico principal (SIGA).\n" +
                        "2. Verifique se o indicador de frequência final e as notas estão consolidados.\n" +
                        "3. Esta operação atribuirá os créditos de extensão aos alunos matriculados.";

                configuracaoRepository.save(new Configuracao("INSTRUCOES_ENCERRAMENTO", textoInstrucoes));
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