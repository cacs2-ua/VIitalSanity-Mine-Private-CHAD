package vitalsanity.dto.profesional_medico;

import vitalsanity.dto.centro_medico.CentroMedicoData;
import vitalsanity.dto.general_user.UsuarioData;
import vitalsanity.dto.paciente.PacienteData;
import vitalsanity.model.Documento;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SolicitudAutorizacionData {

    private Long id;
    private String nombreProfesionalMedico;
    private String nifNieProfesionalMedico;
    private String especialidadProfesionalMedico;
    private String nifCentroMedico;
    private  String nombreCentroMedico;
    private String nombrePaciente;
    private String nifNiePaciente;
    private String motivo;
    private String descripcion;
    private boolean firmada;
    private boolean cofirmada;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaFirma;
    private LocalDateTime fechaCofirma;
    private boolean denegada;

    private PacienteData paciente;

    private  ProfesionalMedicoData profesionalMedico;

    private UsuarioData centroMedicoUsuarioProfesional;


    Set<Documento> documentos = new HashSet<>();

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreProfesionalMedico() {
        return nombreProfesionalMedico;
    }

    public void setNombreProfesionalMedico(String nombreProfesionalMedico) {
        this.nombreProfesionalMedico = nombreProfesionalMedico;
    }

    public String getNifNieProfesionalMedico() {
        return nifNieProfesionalMedico;
    }

    public void setNifNieProfesionalMedico(String nifNieProfesionalMedico) {
        this.nifNieProfesionalMedico = nifNieProfesionalMedico;
    }

    public String getEspecialidadProfesionalMedico() {
        return especialidadProfesionalMedico;
    }

    public void setEspecialidadProfesionalMedico(String especialidadProfesionalMedico) {
        this.especialidadProfesionalMedico = especialidadProfesionalMedico;
    }

    public String getNifCentroMedico() {
        return nifCentroMedico;
    }

    public void setNifCentroMedico(String nifCentroMedico) {
        this.nifCentroMedico = nifCentroMedico;
    }

    public String getNombreCentroMedico() {
        return nombreCentroMedico;
    }

    public void setNombreCentroMedico(String nombreCentroMedico) {
        this.nombreCentroMedico = nombreCentroMedico;
    }

    public String getNombrePaciente() {
        return nombrePaciente;
    }

    public void setNombrePaciente(String nombrePaciente) {
        this.nombrePaciente = nombrePaciente;
    }

    public String getNifNiePaciente() {
        return nifNiePaciente;
    }

    public void setNifNiePaciente(String nifNiePaciente) {
        this.nifNiePaciente = nifNiePaciente;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isFirmada() {
        return firmada;
    }

    public void setFirmada(boolean firmada) {
        this.firmada = firmada;
    }

    public boolean isCofirmada() {
        return cofirmada;
    }

    public void setCofirmada(boolean cofirmada) {
        this.cofirmada = cofirmada;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaFirma() {
        return fechaFirma;
    }

    public void setFechaFirma(LocalDateTime fechaFirma) {
        this.fechaFirma = fechaFirma;
    }

    public LocalDateTime getFechaCofirma() {
        return fechaCofirma;
    }

    public void setFechaCofirma(LocalDateTime fechaCofirma) {
        this.fechaCofirma = fechaCofirma;
    }

    public boolean isDenegada() {
        return denegada;
    }

    public void setDenegada(boolean denegada) {
        this.denegada = denegada;
    }

    public Set<Documento> getDocumentos() {
        return documentos;
    }

    public void setDocumentos(Set<Documento> documentos) {
        this.documentos = documentos;
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

    public UsuarioData getCentroMedicoUsuarioProfesional() {
        return centroMedicoUsuarioProfesional;
    }

    public void setCentroMedicoUsuarioProfesional(UsuarioData centroMedicoUsuarioProfesional) {
        this.centroMedicoUsuarioProfesional = centroMedicoUsuarioProfesional;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SolicitudAutorizacionData that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
