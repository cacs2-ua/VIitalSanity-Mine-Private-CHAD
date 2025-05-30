package vitalsanity.dto.admin_user;

public class RegistroCentroMedicoData {
    private String ccc;
    private String nombreCompleto;
    private String nifNie;
    private String movil;
    private String email;
    private String iban;
    private String direccion;
    private String provincia;
    private String municipio;
    private String codigoPostal;


    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setCcc(String ccc) {
        this.ccc = ccc;
    }

    public String getCcc() {
        return ccc;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }
    public String getNifNie() {
        return nifNie;
    }
    public void setNifNie(String nifNie) {
        this.nifNie = nifNie;
    }
    public String getMovil() {
        return movil;
    }
    public void setMovil(String movil) {
        this.movil = movil;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getIban() {
        return iban;
    }
    public void setIban(String iban) {
        this.iban = iban;
    }
    public String getDireccion() {
        return direccion;
    }
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    public String getProvincia() {
        return provincia;
    }
    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }
    public String getMunicipio() {
        return municipio;
    }
    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }
    public String getCodigoPostal() {
        return codigoPostal;
    }
    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }
}
