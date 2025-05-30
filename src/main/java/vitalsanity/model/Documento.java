package vitalsanity.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import vitalsanity.service.utils.EmailService;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "documentos")
public class Documento implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String nombre;

    @Column(unique = true)
    private String uuid;

    @NotNull
    private String s3_key;

    @NotNull
    private String tipo_archivo;

    @NotNull
    private Long tamanyo;

    @NotNull
    private LocalDateTime fechaSubida;

    @ManyToOne
    @JoinColumn(name = "informe_id")
    private Informe informe;

    public Informe getInforme() {
        return this.informe;
    }

    public void setInforme(Informe informe) {
        if (this.informe == informe) {
            return;
        }

        // Si ya tenía un informe asignado, se remueve la relación
        if (this.informe != null) {
            this.informe.getDocumentos().remove(this);
        }

        // Se asigna el nuevo informe (puede ser null para desvincular)
        this.informe = informe;

        // Si el nuevo informe no es null, se asegura la sincronización bidireccional
        if (informe != null && !informe.getDocumentos().contains(this)) {
            informe.addDocumento(this);
        }
    }


    @ManyToOne
    @JoinColumn(name = "solicitud_autorizacion_id")
    private SolicitudAutorizacion solicitudAutorizacion;

    public SolicitudAutorizacion getSolicitudAutorizacion() {
        return this.solicitudAutorizacion;
    }

    public void setSolicitudAutorizacion(SolicitudAutorizacion solicitudAutorizacion) {
        if (this.solicitudAutorizacion == solicitudAutorizacion) {
            return;
        }

        if (this.solicitudAutorizacion != null) {
            this.solicitudAutorizacion.getDocumentos().remove(this);
        }

        this.solicitudAutorizacion = solicitudAutorizacion;

        if (solicitudAutorizacion != null && !solicitudAutorizacion.getDocumentos().contains(this)) {
            solicitudAutorizacion.addDocumento(this);
        }
    }



    // constructores

    public Documento() {}

    public Documento(String nombre, String s3_key, String tipo_archivo, Long tamanyo, LocalDateTime fechaSubida) {
        this.nombre = nombre;
        this.s3_key = s3_key;
        this.tipo_archivo = tipo_archivo;
        this.tamanyo = tamanyo;
        this.fechaSubida = fechaSubida;
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

    public String getS3_key() {
        return s3_key;
    }

    public void setS3_key(String s3_key) {
        this.s3_key = s3_key;
    }

    public String getTipo_archivo() {
        return tipo_archivo;
    }

    public void setTipo_archivo(String tipo_archivo) {
        this.tipo_archivo = tipo_archivo;
    }

    public Long getTamanyo() {
        return tamanyo;
    }

    public void setTamanyo(Long tamanyo) {
        this.tamanyo = tamanyo;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Documento documento = (Documento) o;
        return Objects.equals(id, documento.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
