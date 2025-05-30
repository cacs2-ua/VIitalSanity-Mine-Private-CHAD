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
@Table(
        name = "profesionales_medicos",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"naf", "ccc"})
        }
)
public class ProfesionalMedico implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String naf;

    @NotNull
    private String ccc;

    @NotNull
    private String iban;

    @NotNull
    private String genero;

    @NotNull
    private String fechaNacimiento;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "especialidad_medica_id", nullable = false)
    private EspecialidadMedica especialidadMedica;

    public EspecialidadMedica getEspecialidadMedica() {
        return this.especialidadMedica;
    }

    public void setEspecialidadMedica(EspecialidadMedica especialidadMedica) {
        if (this.especialidadMedica == especialidadMedica || especialidadMedica == null) {
            return;
        }

        if (this.especialidadMedica != null) {
            this.especialidadMedica.getProfesionalesMedicos().remove(this);
        }

        this.especialidadMedica = especialidadMedica;

        if (!especialidadMedica.getProfesionalesMedicos().contains(this)) {
            especialidadMedica.addProfesionalMedico(this);
        }
    }

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
            throw new IllegalStateException("El profesional médico ya está asignado a un usuario. Desvincule el usuario existente antes de asignar uno nuevo.");
        }
        if (this.usuario == usuario) {
            return; // No hacer nada si ya están vinculados
        }
        this.usuario = usuario;
        if (usuario.getProfesionalMedico() != this) {
            usuario.setProfesionalMedico(this);
        }
    }

    @NotNull
    @ManyToOne
    @JoinColumn(name = "centro_medico_id", nullable = false)
    private CentroMedico centroMedico;

    public CentroMedico getCentroMedico() {
        return centroMedico;
    }

    public void setCentroMedico(CentroMedico centroMedico) {
        if (this.centroMedico == centroMedico || centroMedico == null) {
            return;
        }

        if (this.centroMedico != null) {
            this.centroMedico.getProfesionalesMedicos().remove(this);
        }

        this.centroMedico = centroMedico;
        this.ccc = centroMedico.getCcc();

        if (!centroMedico.getProfesionalesMedicos().contains(this)) {
            centroMedico.addProfesionalMedico(this);
        }
    }

    @ManyToMany(mappedBy = "profesionalesMedicosAutorizados")
    Set<Paciente> pacientesQueHanAutorizado = new HashSet<>();

    public Set<Paciente> getPacientesQueHanAutorizado() {
        return this.pacientesQueHanAutorizado;
    }

    public void addPacienteQueHaAutorizado(Paciente paciente) {
        this.getPacientesQueHanAutorizado().add(paciente);
        paciente.getProfesionalesMedicosAutorizados().add(this);

        this.getPacientesQueHanDesautorizado().remove(paciente);
        paciente.getProfesionalesMedicosDesautorizados().remove(this);
    }

    public  void  quitarPacienteQueHaAutorizado (Paciente paciente) {
        this.getPacientesQueHanAutorizado().remove(paciente);
        paciente.getProfesionalesMedicosAutorizados().remove(this);

        this.getPacientesQueHanDesautorizado().add(paciente);
        paciente.getProfesionalesMedicosDesautorizados().add(this);
    }



    @ManyToMany(mappedBy = "profesionalesMedicosDesautorizados")
    Set<Paciente> pacientesQueHanDesautorizado = new HashSet<>();

    public Set<Paciente> getPacientesQueHanDesautorizado() {
        return this.pacientesQueHanDesautorizado;
    }

    public void addPacienteQueHaDesautorizado(Paciente paciente) {
        this.getPacientesQueHanDesautorizado().add(paciente);
        paciente.getProfesionalesMedicosDesautorizados().add(this);

        this.getPacientesQueHanAutorizado().remove(paciente);
        paciente.getProfesionalesMedicosAutorizados().remove(this);
    }

    public  void  quitarPacienteQueHaDesautorizado (Paciente paciente) {
        this.getPacientesQueHanDesautorizado().remove(paciente);
        paciente.getProfesionalesMedicosDesautorizados().remove(this);

        this.getPacientesQueHanAutorizado().add(paciente);
        paciente.getProfesionalesMedicosAutorizados().add(this);
    }

    @OneToMany(mappedBy = "profesionalMedico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<SolicitudAutorizacion> solicitudesAutorizacion = new HashSet<>();

    public Set<SolicitudAutorizacion> getSolicitudesAutorizacion() {
        return  this.solicitudesAutorizacion;
    }

    public void addSolicitudAutorizacion(SolicitudAutorizacion solicitudAutorizacion) {
        if (solicitudesAutorizacion.contains(solicitudAutorizacion)) return;
        solicitudesAutorizacion.add(solicitudAutorizacion);
        if (solicitudAutorizacion.getProfesionalMedico() != this) {
            solicitudAutorizacion.setProfesionalMedico(this);
        }
    }



    @OneToMany(mappedBy = "profesionalMedico", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<Informe> informes = new HashSet<>();

    public Set<Informe> getInformes() {
        return  this.informes;
    }

    public void addInforme(Informe informe) {
        if (informes.contains(informe)) return;
        informes.add(informe);
        if (informe.getProfesionalMedico() != this) {
            informe.setProfesionalMedico(this);
        }
    }

    // getter y setter

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNaf() {
        return naf;
    }

    public void setNaf(String naf) {
        this.naf = naf;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfesionalMedico profesionalMedicoo = (ProfesionalMedico) o;
        return Objects.equals(id, profesionalMedicoo.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
