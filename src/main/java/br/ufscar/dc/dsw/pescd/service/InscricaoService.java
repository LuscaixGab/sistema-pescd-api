package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.Oferta;
import br.ufscar.dc.dsw.pescd.model.Perfil;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao;
import br.ufscar.dc.dsw.pescd.model.Usuario;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.repository.OfertaRepository;
import br.ufscar.dc.dsw.pescd.repository.UsuarioRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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

    private void validarArquivoCsv(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Por favor, selecione um arquivo válido.");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IllegalArgumentException("O arquivo deve ser obrigatoriamente do tipo .csv.");
        }

        if (file.getSize() > 5242880) { // Trava de 5MB
            throw new IllegalArgumentException("O arquivo não pode ultrapassar o limite de 5MB.");
        }
    }

    @Transactional
    public void processarAlunosCsv(UUID ofertaId, MultipartFile file) throws Exception {

        validarArquivoCsv(file);

        Oferta oferta = ofertaRepository.findById(ofertaId)
                .orElseThrow(() -> new IllegalArgumentException("Oferta não encontrada."));

        // Configuração do formato esperado pelo Apache Commons CSV
        CSVFormat csvFormat = CSVFormat.Builder.create()
                .setHeader("RA", "NOME_COMPLETO", "EMAIL")
                .setSkipHeaderRecord(true) // Ignora a primeira linha (cabeçalho)
                .setIgnoreSurroundingSpaces(true) // Remove espaços em branco sobrando ao redor dos dados
                .setTrim(true)
                .build();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(br, csvFormat)) {

            for (CSVRecord csvRecord : csvParser) {
                // Previne erros caso a linha do CSV esteja em branco ou tenha colunas faltando
                if (!csvRecord.isConsistent()) {
                    continue;
                }

                // Acesso seguro e nomeado aos dados da coluna, garantido pelo setHeader
                String ra = csvRecord.get("RA");
                String nomeCompleto = csvRecord.get("NOME_COMPLETO");
                String email = csvRecord.get("EMAIL");

                if (ra.isEmpty() || nomeCompleto.isEmpty() || email.isEmpty()) {
                    continue; // Pula linhas com dados essenciais em branco
                }

                // RN-3: Usando o e-mail, verificar se o aluno existe no BD
                Usuario aluno = usuarioRepository.findByNomeUsuarioOrEmail(ra, email).orElse(null);

                if (aluno == null) {
                    // RN-1 e RN-3: Caso não exista, efetua o cadastro usando o e-mail como nome do usuário e RA como senha
                    aluno = new Usuario();
                    aluno.setNomeCompleto(nomeCompleto);
                    aluno.setEmail(email);
                    aluno.setNomeUsuario(email);
                    aluno.setSenha(passwordEncoder.encode(ra));
                    aluno.setPerfil(Perfil.ALUNO);

                    aluno = usuarioRepository.save(aluno);
                }

                // RN-2: Verifica se este aluno já está matriculado nessa oferta específica (pode estar em múltiplas ofertas)
                final UUID alunoId = aluno.getId();
                boolean jaInscrito = inscricaoRepository.findByOferta(oferta).stream()
                        .anyMatch(inscricao -> inscricao.getAluno().getId().equals(alunoId));

                if (!jaInscrito) {
                    // Apenas adiciona-o à oferta se já não estiver inscrito
                    Inscricao inscricao = new Inscricao(null, aluno, oferta, StatusInscricao.NAO_ENVIADO);
                    inscricaoRepository.save(inscricao);
                }
            }
        }
    }
}