package br.ufscar.dc.dsw.pescd.service;

import br.ufscar.dc.dsw.pescd.dto.DocumentacaoAulaDTO;
import br.ufscar.dc.dsw.pescd.model.DocumentacaoAula;
import br.ufscar.dc.dsw.pescd.model.Inscricao;
import br.ufscar.dc.dsw.pescd.model.StatusInscricao; // Confirme se o nome do seu Enum é esse
import br.ufscar.dc.dsw.pescd.repository.DocumentacaoAulaRepository;
import br.ufscar.dc.dsw.pescd.repository.InscricaoRepository;
import br.ufscar.dc.dsw.pescd.util.UploadUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class DocumentacaoAulaService {

    private final DocumentacaoAulaRepository docRepository;
    private final InscricaoRepository inscricaoRepository;
    private final LogStatusService logStatusService;

    public DocumentacaoAulaService(DocumentacaoAulaRepository docRepository, InscricaoRepository inscricaoRepository, LogStatusService logStatusService) {
        this.docRepository = docRepository;
        this.inscricaoRepository = inscricaoRepository;
        this.logStatusService = logStatusService;
    }

    // Diretório onde documentos ficarão salvos
    private final String UPLOAD_DIR = "uploads/documentacoes/";
    
    public void processarEnvio(DocumentacaoAulaDTO dto, Inscricao inscricao) throws IOException {
        MultipartFile arquivo = dto.getArquivo();

        UploadUtils.validarPdfObrigatorio(arquivo, "a documentação");

        // Salvar o arquivo fisicamente na pasta
        Path caminhoPasta = Paths.get(UPLOAD_DIR);
        if (!Files.exists(caminhoPasta)) {
            Files.createDirectories(caminhoPasta);
        }

        // Gera um código aleatório antes do nome do arquivo pra evitar que um aluno sobrescreva o arquivo do outro
        String nomeArquivo = UUID.randomUUID().toString() + "_" + arquivo.getOriginalFilename();
        Path caminhoCompleto = caminhoPasta.resolve(nomeArquivo);
        Files.copy(arquivo.getInputStream(), caminhoCompleto);

        // Preenche model
        DocumentacaoAula doc = new DocumentacaoAula(
            null, // O ID é gerado automaticamente pelo banco/UUID
            dto.getNomeInstituicao(),
            dto.getNomeDisciplina(),
            dto.getCursoDisciplina(),
            dto.getCargaHoraria(),
            caminhoCompleto.toString(),
            inscricao
        );

        docRepository.save(doc);

        // Mudar o status do aluno na oferta
        inscricao.setStatus(StatusInscricao.DOCUMENTACAO_ENVIADA); 
        inscricaoRepository.save(inscricao);
        logStatusService.registrarLog(inscricao, StatusInscricao.DOCUMENTACAO_ENVIADA, inscricao.getAluno());
    }
}
