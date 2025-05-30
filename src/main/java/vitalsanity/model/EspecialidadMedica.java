package vitalsanity.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "especialidades_medicas")
public class EspecialidadMedica implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    String nombre;

    @OneToMany(mappedBy = "especialidadMedica", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<ProfesionalMedico> profesionalMedicos = new HashSet<>();

    public Set<ProfesionalMedico> getProfesionalesMedicos() {
        return  this.profesionalMedicos;
    }

    public void addProfesionalMedico(ProfesionalMedico profesionalMedico) {
        if (profesionalMedicos.contains(profesionalMedico)) return;
        profesionalMedicos.add(profesionalMedico);
        if (profesionalMedico.getEspecialidadMedica() != this) {
            profesionalMedico.setEspecialidadMedica(this);
        }
    }

    // Constructores

    public EspecialidadMedica() {
    }

    public EspecialidadMedica(String nombre) {
        this.nombre = nombre;
    }

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

}
