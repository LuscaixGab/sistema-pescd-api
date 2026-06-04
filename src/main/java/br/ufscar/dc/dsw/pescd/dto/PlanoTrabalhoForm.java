package br.ufscar.dc.dsw.pescd.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public class PlanoTrabalhoForm {

    @NotBlank(message = "O código da disciplina é obrigatório.")
    private String codigoDisciplina;

    @NotBlank(message = "O nome da disciplina é obrigatório.")
    private String nomeDisciplina;

    @NotBlank(message = "O curso da disciplina é obrigatório.")
    private String cursoDisciplina;

    @NotNull(message = "O arquivo do plano é obrigatório.")
    private MultipartFile arquivoPlano;

    @NotNull(message = "O professor supervisor é obrigatório.")
    private UUID professorSupervisorId;

    @NotNull(message = "A inscrição é obrigatória.")
    private UUID inscricaoId;

    public String getCodigoDisciplina() {
        return codigoDisciplina;
    }

    public void setCodigoDisciplina(String codigoDisciplina) {
        this.codigoDisciplina = codigoDisciplina;
    }

    public String getNomeDisciplina() {
        return nomeDisciplina;
    }

    public void setNomeDisciplina(String nomeDisciplina) {
        this.nomeDisciplina = nomeDisciplina;
    }

    public String getCursoDisciplina() {
        return cursoDisciplina;
    }

    public void setCursoDisciplina(String cursoDisciplina) {
        this.cursoDisciplina = cursoDisciplina;
    }

    public MultipartFile getArquivoPlano() {
        return arquivoPlano;
    }

    public void setArquivoPlano(MultipartFile arquivoPlano) {
        this.arquivoPlano = arquivoPlano;
    }

    public UUID getProfessorSupervisorId() {
        return professorSupervisorId;
    }

    public void setProfessorSupervisorId(UUID professorSupervisorId) {
        this.professorSupervisorId = professorSupervisorId;
    }

    public UUID getInscricaoId() {
        return inscricaoId;
    }

    public void setInscricaoId(UUID inscricaoId) {
        this.inscricaoId = inscricaoId;
    }
}
