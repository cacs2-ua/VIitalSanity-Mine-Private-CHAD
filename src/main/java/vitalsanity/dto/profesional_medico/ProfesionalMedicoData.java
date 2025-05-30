package vitalsanity.dto.profesional_medico;

import vitalsanity.dto.centro_medico.CentroMedicoData;
import vitalsanity.dto.general_user.UsuarioData;

import java.util.Objects;

public class ProfesionalMedicoData {

    private String id;

    private String naf;

    private String ccc;

    private String iban;

    private String genero;

    private String fechaNacimiento;

    private  String nombreCentroMedico;

    private  EspecialidadMedicaData especialidadMedica;

    private UsuarioData usuario;

    private CentroMedicoData centroMedico;

    private UsuarioData centroMedicoUsuarioProfesional;


    //getters y setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getNombreCentroMedico() {
        return nombreCentroMedico;
    }

    public void setNombreCentroMedico(String nombreCentroMedico) {
        this.nombreCentroMedico = nombreCentroMedico;
    }

    public EspecialidadMedicaData getEspecialidadMedica() {
        return especialidadMedica;
    }

    public void setEspecialidadMedica(EspecialidadMedicaData especialidadMedica) {
        this.especialidadMedica = especialidadMedica;
    }

    public UsuarioData getUsuario() {
        return usuario;
    }

    public void setUsuario(UsuarioData usuario) {
        this.usuario = usuario;
    }

    public CentroMedicoData getCentroMedico() {
        return centroMedico;
    }

    public void setCentroMedico(CentroMedicoData centroMedico) {
        this.centroMedico = centroMedico;
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
        if (!(o instanceof ProfesionalMedicoData)) return false;
        ProfesionalMedicoData profesionalMedicoData = (ProfesionalMedicoData) o;
        return Objects.equals(id, profesionalMedicoData.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


}
