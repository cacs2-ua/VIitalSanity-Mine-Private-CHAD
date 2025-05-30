package vitalsanity.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "informes")
public class Informe implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String uuid;

    @NotNull
    @Column(unique = true)
    private String identificadorPublico;

    @Column(length = 500)
    @NotNull
    private String titulo;

    @Column(columnDefinition = "TEXT")
    @NotNull
    private String descripcion;

    @Column(columnDefinition = "TEXT")
    @NotNull
    private String observaciones;

    @NotNull
    private LocalDateTime fechaCreacion;

    boolean firmado;

    LocalDateTime fechaFirma;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    public Paciente getPaciente() {
        return this.paciente;
    }

    public void setPaciente(Paciente paciente) {
        if (this.paciente == paciente || paciente == null) {
            return;
        }

        if (this.paciente != null) {
            this.paciente.getInformes().remove(this);
        }

        this.paciente = paciente;

        if (!paciente.getInformes().contains(this)) {
            paciente.addInforme(this);
        }
    }

    @NotNull
    @ManyToOne
    @JoinColumn(name = "profesional_medico_id", nullable = false)
    private ProfesionalMedico profesionalMedico;

    public ProfesionalMedico getProfesionalMedico() {
        return this.profesionalMedico;
    }

    public void setProfesionalMedico(ProfesionalMedico profesionalMedico) {
        if (this.profesionalMedico == profesionalMedico || profesionalMedico == null) {
            return;
        }

        if (this.profesionalMedico != null) {
            this.profesionalMedico.getInformes().remove(this);
        }

        // Asigna el nuevo tipo
        this.profesionalMedico = profesionalMedico;

        if (!profesionalMedico.getInformes().contains(this)) {
            profesionalMedico.addInforme(this);
        }
    }

    @OneToMany(mappedBy = "informe", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<Documento> documentos = new HashSet<>();

    public Set<Documento> getDocumentos() {
        return  this.documentos;
    }

    public void addDocumento(Documento documento) {
        if (documentos.contains(documento)) return;
        documentos.add(documento);
        if (documento.getInforme() != this) {
            documento.setInforme(this);
        }
    }

    public Informe() {}


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdentificadorPublico() {
        return identificadorPublico;
    }

    public void setIdentificadorPublico(String identificadorPublico) {
        this.identificadorPublico = identificadorPublico;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public boolean isFirmado() {
        return firmado;
    }

    public void setFirmado(boolean firmado) {
        this.firmado = firmado;
    }

    public LocalDateTime getFechaFirma() {
        return fechaFirma;
    }

    public void setFechaFirma(LocalDateTime fechaFirma) {
        this.fechaFirma = fechaFirma;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Informe informe = (Informe) o;
        return Objects.equals(id, informe.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
