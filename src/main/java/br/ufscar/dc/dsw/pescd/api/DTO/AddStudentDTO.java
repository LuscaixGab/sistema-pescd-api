package br.ufscar.dc.dsw.pescd.api.dto;

public class AddStudentDTO {
    private Long studentId;
    // optional: other fields like enrollment date
    public AddStudentDTO() {}
    public AddStudentDTO(Long studentId) { this.studentId = studentId; }
    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
}
