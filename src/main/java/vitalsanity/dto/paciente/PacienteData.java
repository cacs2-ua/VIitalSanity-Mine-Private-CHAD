package vitalsanity.dto.paciente;

import vitalsanity.dto.general_user.UsuarioData;

import java.util.Objects;

public class PacienteData {

    private Long id;
    private String genero;
    private String fechaNacimiento;

    private UsuarioData usuario;

    private int edad;

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public UsuarioData getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioData usuario) {
        this.usuario = usuario;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PacienteData)) return false;
        PacienteData pacienteData = (PacienteData) o;
        return Objects.equals(id, pacienteData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}
