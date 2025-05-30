package vitalsanity.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(unique = true)
    private String Uuid;

    @NotNull
    @Column(unique = true)
    private String email;

    @NotNull
    private String nombreCompleto;

    @NotNull
    private String contrasenya;

    @NotNull
    private boolean activado = false;

    @NotNull
    private boolean primerAcceso = true;

    @NotNull
    private String nifNie;

    @NotNull
    private String telefono;

    private String pais;

    private String provincia;

    private String municipio;

    private String codigoPostal;

    @NotNull
    @ManyToOne
    @JoinColumn(name = "tipo_id", nullable = false)
    private TipoUsuario tipo;

    public TipoUsuario getTipo() {
        return tipo;
    }

    public void setTipo(TipoUsuario tipo) {
        // Si el nuevo tipo es el mismo que el actual, no hace nada
        if (this.tipo == tipo || tipo == null) {
            return;
        }

        // Si ya tiene un tipo, lo desvincula de la lista de usuarios de ese tipo
        if (this.tipo != null) {
            this.tipo.getUsuarios().remove(this);
        }

        // Asigna el nuevo tipo
        this.tipo = tipo;

        // Si el tipo no es nulo, lo añade a la lista de usuarios de ese tipo
        if (!tipo.getUsuarios().contains(this)) {
            tipo.addUsuario(this);
        }
    }

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Paciente paciente;

    public Paciente getPaciente() {
        return paciente;
    }

    public void setPaciente(Paciente paciente) {
        if (paciente == null) {
            throw new IllegalArgumentException("El paciente no puede ser null.");
        }
        if (this.paciente != null && this.paciente != paciente) {
            throw new IllegalStateException("El usuario ya tiene un paciente asignado. Desvincule el paciente existente antes de asignar uno nuevo.");
        }
        if (this.paciente == paciente) {
            return; // No hacer nada si ya están vinculados
        }
        this.paciente = paciente;
        if (paciente.getUsuario() != this) {
            paciente.setUsuario(this);
        }
    }

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private ProfesionalMedico profesionalMedico;

    public ProfesionalMedico getProfesionalMedico () {
        return this.profesionalMedico;
    }

    public void setProfesionalMedico (ProfesionalMedico profesionalMedico) {
        if (profesionalMedico == null) {
            throw new IllegalArgumentException("El profesional médico no puede ser null.");
        }
        if (this.profesionalMedico != null && this.profesionalMedico != profesionalMedico) {
            throw new IllegalStateException("El usuario ya tiene un profesional médico asignado. Desvincule el profesional médico existente antes de asignar uno nuevo.");
        }
        if (this.profesionalMedico == profesionalMedico) {
            return; // No hacer nada si ya están vinculados
        }
        this.profesionalMedico = profesionalMedico;
        if (profesionalMedico.getUsuario() != this) {
            profesionalMedico.setUsuario(this);
        }
    }

    @OneToOne(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private CentroMedico centroMedico;

    public CentroMedico getCentroMedico () {
        return this.centroMedico;
    }

    public void setCentroMedico(CentroMedico centroMedico) {
        if (centroMedico == null) {
            throw new IllegalArgumentException("El centro médico no puede ser null.");
        }
        if (this.centroMedico != null && this.centroMedico != centroMedico) {
            throw new IllegalStateException("El usuario ya tiene un centro médico asignado. Desvincule el centro médico existente antes de asignar uno nuevo.");
        }
        if (this.centroMedico == centroMedico) {
            return; // No hacer nada si ya están vinculados
        }
        this.centroMedico = centroMedico;
        if (centroMedico.getUsuario() != this) {
            centroMedico.setUsuario(this);
        }
    }

    public Usuario() {
    }

    //Constructor con todos los atributos
    public Usuario(String Uuid, String email, String nombreCompleto, String contrasenya, boolean activado, String nifNie, String telefono, String pais, String provincia, String municipio, String codigoPostal) {
        this.Uuid = Uuid;
        this.email = email;
        this.nombreCompleto = nombreCompleto;
        this.contrasenya = contrasenya;
        this.activado = activado;
        this.nifNie = nifNie;
        this.telefono = telefono;
        this.pais = pais;
        this.provincia = provincia;
        this.municipio = municipio;
        this.codigoPostal = codigoPostal;
    }

    // getters y setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return this.Uuid;
    }

    public void setIdentificador(String Uuid) {
        this.Uuid = Uuid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getContrasenya() {
        return contrasenya;
    }

    public void setContrasenya(String contrasenya) {
        this.contrasenya = contrasenya;
    }

    public boolean isActivado() {
        return activado;
    }

    public void setActivado(boolean activado) {
        this.activado = activado;
    }

    public boolean getPrimerAcceso() {
        return primerAcceso;
    }

    public void setPrimerAcceso(boolean primerAcceso) {
        this.primerAcceso = primerAcceso;
    }

    public String getNifNie() {
        return nifNie;
    }

    public void setNifNie(String nifNie) {
        this.nifNie = nifNie;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getProvincia() {
        return provincia;
    }

    public void setProvincia(String provincia) {
        this.provincia = provincia;
    }

    public String getMunicipio() {
        return municipio;
    }

    public void setMunicipio(String municipio) {
        this.municipio = municipio;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Usuario usuario = (Usuario) o;
        if (id != null && usuario.id != null)
            return Objects.equals(id, usuario.id);
        return Objects.equals(email, usuario.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }


}
