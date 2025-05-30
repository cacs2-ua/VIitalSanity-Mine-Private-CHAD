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
@Table(name = "centros_medicos")
public class CentroMedico implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String ccc;

    @NotNull
    private String iban;

    @NotNull
    private String direccion;

    @NotNull
    @OneToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;


    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("El usuario no puede ser null.");
        }
        if (this.usuario != null && this.usuario != usuario) {
            throw new IllegalStateException("El centro médico ya está asignado a un usuario. Desvincule el usuario existente antes de asignar uno nuevo.");
        }
        if (this.usuario == usuario) {
            return;
        }
        this.usuario = usuario;
        if (usuario.getCentroMedico() != this) {
            usuario.setCentroMedico(this);
        }
    }

    @OneToMany(mappedBy = "centroMedico", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<ProfesionalMedico> profesionalesMedicos = new HashSet<>();

    public Set<ProfesionalMedico> getProfesionalesMedicos() {
        return profesionalesMedicos;
    }

    public void addProfesionalMedico(ProfesionalMedico profesionalMedico) {
        if (profesionalesMedicos.contains(profesionalMedico)) return;
        profesionalMedico.setCcc(this.ccc);
        profesionalesMedicos.add(profesionalMedico);
        if (profesionalMedico.getCentroMedico() != this) {
            profesionalMedico.setCentroMedico(this);
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCcc() {
        return ccc;
    }

    public void setCcc(String ccc) {
        this.ccc = ccc;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CentroMedico centroMedico = (CentroMedico) o;
        return Objects.equals(id, centroMedico.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
