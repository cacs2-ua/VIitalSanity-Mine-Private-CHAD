package vitalsanity.dto.profesional_medico;

import java.util.Objects;

public class EspecialidadMedicaData {

    private Long id;
    String nombre;

    // getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EspecialidadMedicaData)) return false;
        EspecialidadMedicaData especialidadMedicaData = (EspecialidadMedicaData) o;
        return Objects.equals(id, especialidadMedicaData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }




}
