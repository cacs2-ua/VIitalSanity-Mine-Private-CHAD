package vitalsanity.dto.profesional_medico;

import vitalsanity.dto.centro_medico.CentroMedicoData;
import vitalsanity.dto.general_user.UsuarioData;
import vitalsanity.dto.paciente.PacienteData;

import java.time.LocalDateTime;
import java.util.Objects;

public class InformeData {

    private Long id;
    private String uuid;
    private String identificadorPublico;
    private String titulo;
    private String descripcion;
    private String observaciones;
    private LocalDateTime fechaCreacion;

    public InformeData() {}

    private PacienteData paciente;

    private ProfesionalMedicoData profesionalMedico;

    private CentroMedicoData centroMedico;

    private UsuarioData centroMedicoUsuario;

    // getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getIdentificadorPublico() {
        return identificadorPublico;
    }

    public void setIdentificadorPublico(String identificadorPublico) {
        this.identificadorPublico = identificadorPublico;
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

    public PacienteData getPaciente() {
        return paciente;
    }

    public void setPaciente(PacienteData paciente) {
        this.paciente = paciente;
    }

    public ProfesionalMedicoData getProfesionalMedico() {
        return profesionalMedico;
    }

    public void setProfesionalMedico(ProfesionalMedicoData profesionalMedico) {
        this.profesionalMedico = profesionalMedico;
    }

    public CentroMedicoData getCentroMedico() {
        return centroMedico;
    }

    public void setCentroMedico(CentroMedicoData centroMedico) {
        this.centroMedico = centroMedico;
    }

    public UsuarioData getCentroMedicoUsuario() {
        return centroMedicoUsuario;
    }

    public void setCentroMedicoUsuario(UsuarioData centroMedicoUsuario) {
        this.centroMedicoUsuario = centroMedicoUsuario;
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InformeData)) return false;
        InformeData informeData = (InformeData) o;
        return Objects.equals(id, informeData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
