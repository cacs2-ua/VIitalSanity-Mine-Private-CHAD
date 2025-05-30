package vitalsanity.dto.profesional_medico;

import vitalsanity.dto.paciente.PacienteData;

import java.util.Objects;

public class SubirInformeResponse {

    private Long idInforme;
    private String pdfBase64;

    public SubirInformeResponse(Long idInforme, String pdfBase64) {
        this.idInforme = idInforme;
        this.pdfBase64 = pdfBase64;
    }

    public Long getIdInforme() {
        return idInforme;
    }

    public void setIdInforme(Long idInforme) {
        this.idInforme = idInforme;
    }

    public String getPdfBase64() {
        return pdfBase64;
    }

    public void setPdfBase64(String pdfBase64) {
        this.pdfBase64 = pdfBase64;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubirInformeResponse)) return false;
        SubirInformeResponse subirInformeResponse = (SubirInformeResponse) o;
        return Objects.equals(idInforme, subirInformeResponse.idInforme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idInforme);
    }


}
