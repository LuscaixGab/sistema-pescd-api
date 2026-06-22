package br.ufscar.dc.dsw.pescd.util;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public final class UploadUtils {

    // Regra de negócio usada pelos uploads da aplicação.
    public static final long MAX_UPLOAD_BYTES = 5L * 1024 * 1024;
    public static final String MAX_UPLOAD_LABEL = "5MB";

    private UploadUtils() {
    }

    public static void validarPdfObrigatorio(MultipartFile arquivo, String nomeCampo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Selecione um arquivo PDF para " + nomeCampo + ".");
        }

        if (arquivo.getSize() > MAX_UPLOAD_BYTES) {
            throw new IllegalArgumentException("O arquivo não pode ultrapassar o limite de " + MAX_UPLOAD_LABEL + ".");
        }

        String nomeOriginal = arquivo.getOriginalFilename();
        String contentType = arquivo.getContentType();
        // Valida extensão e Content-Type para evitar aceitar arquivos renomeados indevidamente.
        boolean extensaoPdf = StringUtils.hasText(nomeOriginal) && nomeOriginal.toLowerCase().endsWith(".pdf");
        boolean tipoPdf = "application/pdf".equals(contentType);

        if (!extensaoPdf || !tipoPdf) {
            throw new IllegalArgumentException("O arquivo deve ser obrigatoriamente um PDF.");
        }
    }
}
