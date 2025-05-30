package vitalsanity.dto.profesional_medico;

import vitalsanity.dto.general_user.UsuarioData;

import java.time.LocalDateTime;
import java.util.Objects;

public class DocumentoData {

    private Long id;
    private String nombre;
    private String s3_key;
    private String tipo_archivo;
    private Long tamanyo;
    private LocalDateTime fechaSubida;
    private String urlPrefirmada;
    private String uuid;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getFechaSubida() {
        return fechaSubida;
    }

    public void setFechaSubida(LocalDateTime fechaSubida) {
        this.fechaSubida = fechaSubida;
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

    public Long getTamanyo() {
        return tamanyo;
    }

    public void setTamanyo(Long tamanyo) {
        this.tamanyo = tamanyo;
    }

    public String getTipo_archivo() {
        return tipo_archivo;
    }

    public void setTipo_archivo(String tipo_archivo) {
        this.tipo_archivo = tipo_archivo;
    }

    public String getUrlPrefirmada() {
        return urlPrefirmada;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setUrlPrefirmada(String urlPrefirmada) {
        this.urlPrefirmada = urlPrefirmada;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentoData that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
