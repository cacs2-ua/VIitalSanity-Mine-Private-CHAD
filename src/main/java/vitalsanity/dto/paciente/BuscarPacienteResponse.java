package vitalsanity.dto.paciente;

public class BuscarPacienteResponse {
    Long id;
    private String nombreCompleto;
    private String nifNie;
    private String genero;
    private int edad;

    public  Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public String getNombreCompleto() {
        return nombreCompleto;
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

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }
}
