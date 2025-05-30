package vitalsanity.dto.paciente;

public class AutorizacionFirmadaResponse {
    private Long idSolicitud;
    private String pdfBase64;


    public AutorizacionFirmadaResponse(Long idSolicitud, String pdfBase64) {
        this.idSolicitud = idSolicitud;
        this.pdfBase64 = pdfBase64;
    }

    public Long getIdSolicitud() {
        return idSolicitud;
    }

    public void setIdSolicitud(Long idSolicitud) {
        this.idSolicitud = idSolicitud;
    }

    public String getPdfBase64() {
        return pdfBase64;
    }

    public void setPdfBase64(String pdfBase64) {
        this.pdfBase64 = pdfBase64;
    }

}

