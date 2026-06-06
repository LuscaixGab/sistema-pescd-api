package br.ufscar.dc.dsw.pescd.dto;

public class ResumoAlunoEncerramentoOferta {
    private String nomeAluno;
    private String tipoCredito; // estágio ou documentação
    private Integer frequencia;
    private String nota;

    public ResumoAlunoEncerramentoOferta(String nomeAluno, String tipoCredito, Integer frequencia, String nota) {
        this.nomeAluno = nomeAluno;
        this.tipoCredito = tipoCredito;
        this.frequencia = frequencia;
        this.nota = nota;
    }

    public String getNomeAluno() {
        return nomeAluno;
    }

    public void setNomeAluno(String nomeAluno) {
        this.nomeAluno = nomeAluno;
    }

    public String getTipoCredito() {
        return tipoCredito;
    }

    public void setTipoCredito(String tipoCredito) {
        this.tipoCredito = tipoCredito;
    }

    public Integer getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(Integer frequencia) {
        this.frequencia = frequencia;
    }

    public String getNota() {
        return nota;
    }

    public void setNota(String nota) {
        this.nota = nota;
    }
}
