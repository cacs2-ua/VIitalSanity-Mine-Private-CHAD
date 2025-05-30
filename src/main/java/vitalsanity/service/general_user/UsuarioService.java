package vitalsanity.service.general_user;

import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vitalsanity.dto.admin_user.RegistroCentroMedicoData;
import vitalsanity.dto.general_user.ActualizarContrasenyaData;
import vitalsanity.dto.general_user.UsuarioData;
import vitalsanity.dto.guest_user.RegistroData;
import vitalsanity.dto.paciente.ResidenciaData;
import vitalsanity.model.*;
import vitalsanity.repository.*;
import vitalsanity.service.utils.EmailService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    Logger logger = LoggerFactory.getLogger(UsuarioService.class);
    @Autowired
    private PacienteRepository pacienteRepository;

    public enum LoginStatus {LOGIN_OK, USER_NOT_FOUND, USER_DISABLED, ERROR_PASSWORD}

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProfesionalMedicoRepository profesionalMedicoRepository;

    @Autowired
    private EspecialidadMedicaRepository especialidadMedicaRepository;

    // Repositorio para tipos de usuario
    @Autowired
    private TipoUsuarioRepository tipoUsuarioRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private EmailService emailService;

    // Método auxiliar para convertir bytes a hexadecimal
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Método para generar el hash SHA3-256 de una contraseña
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA3-256");
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA3-256 no está disponible", e);
        }
    }

    @Transactional
    public LoginStatus login(String email, String password) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        if (!usuario.isPresent()) {
            return LoginStatus.USER_NOT_FOUND;
        }

        String hashedPassword = hashPassword(password);

        if (!hashedPassword.equals(usuario.get().getContrasenya())) {
            return LoginStatus.ERROR_PASSWORD;
        } else if (!usuario.get().isActivado()) {
            return LoginStatus.USER_DISABLED;
        } else {
            return LoginStatus.LOGIN_OK;
        }
    }

    @Transactional(readOnly = true)
    public UsuarioData findByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
        if (usuario == null) return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    @Transactional(readOnly = true)
    public UsuarioData findById(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
        if (usuario == null) return null;
        else {
            return modelMapper.map(usuario, UsuarioData.class);
        }
    }

    @Transactional
    public UsuarioData registrarPaciente(RegistroData registroData) {
        // Verificar que la contrasenya tenga al menos 8 caracteres
        if (registroData.getContrasenya() == null || registroData.getContrasenya().length() < 8) {
            throw new IllegalArgumentException("La contrasenya debe tener al menos 8 caracteres");
        }

        // Verificar que las contrasenyas sean iguales
        if (!registroData.getContrasenya().equals(registroData.getConfirmarContrasenya())) {
            throw new IllegalArgumentException("Las contrasenyas no coinciden");
        }

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setIdentificador(java.util.UUID.randomUUID().toString());
        usuario.setEmail(registroData.getEmail());
        usuario.setNombreCompleto(registroData.getNombreCompleto());
        usuario.setContrasenya(hashPassword(registroData.getContrasenya()));
        usuario.setActivado(true);
        usuario.setNifNie(registroData.getNifNie());
        usuario.setTelefono(registroData.getMovil());
        usuario.setPais(registroData.getPais());
        // Provincia, municipio y codigoPostal se ignoran

        // Asignar tipo de usuario paciente (se asume que existe un TipoUsuario con tipo "paciente")
        TipoUsuario tipoPaciente = tipoUsuarioRepository.findByTipo("paciente")
                .orElseThrow(() -> new IllegalStateException("Tipo de usuario 'paciente' no encontrado"));
        usuario.setTipo(tipoPaciente);

        // Crear entidad Paciente
        Paciente paciente = new Paciente();
        paciente.setGenero(registroData.getGenero());
        paciente.setFechaNacimiento(registroData.getFechaNacimiento());
        // Establecer relacion bidireccional
        usuario.setPaciente(paciente);
        paciente.setUsuario(usuario);

        // Guardar usuario (se guardara el paciente por cascada)
        Usuario savedUsuario = usuarioRepository.save(usuario);

        // Mapear a UsuarioData y retornar
        UsuarioData usuarioData = modelMapper.map(savedUsuario, UsuarioData.class);
        return usuarioData;
    }

    // Metodo privado para generar contrasenya segura
    private String generateSecurePassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$%&*!";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    @Transactional
    public UsuarioData registrarCentroMedico(RegistroCentroMedicoData data) {
        // Generar contrasenya segura
        String generatedPassword = generateSecurePassword(12);

        // Crear nuevo usuario
        Usuario usuario = new Usuario();
        usuario.setIdentificador(java.util.UUID.randomUUID().toString());
        usuario.setEmail(data.getEmail());
        usuario.setNombreCompleto(data.getNombreCompleto());
        usuario.setContrasenya(hashPassword(generatedPassword));
        usuario.setActivado(true);
        usuario.setPrimerAcceso(true);
        usuario.setNifNie(data.getNifNie());
        usuario.setTelefono(data.getMovil());
        usuario.setPais("Espana");
        usuario.setProvincia(data.getProvincia());
        usuario.setMunicipio(data.getMunicipio());
        usuario.setCodigoPostal(data.getCodigoPostal());

        // Asignar tipo de usuario centro-medico
        TipoUsuario tipoCentroMedico = tipoUsuarioRepository.findById(2L)
                .orElseThrow(() -> new IllegalStateException("Tipo de usuario correspondiente a los Centros Médicos no encontrado"));
        usuario.setTipo(tipoCentroMedico);

        // Crear entidad CentroMedico
        CentroMedico centroMedico = new CentroMedico();
        centroMedico.setCcc(data.getCcc());
        centroMedico.setIban(data.getIban());
        centroMedico.setDireccion(data.getDireccion());
        // Establecer relacion bidireccional
        usuario.setCentroMedico(centroMedico);
        centroMedico.setUsuario(usuario);

        // Guardar usuario (centroMedico se guarda por cascada)
        Usuario savedUsuario = usuarioRepository.save(usuario);

        new Thread(() -> {
            emailService.send(data.getEmail(), "Registro Centro Medico",
                    "Se ha registrado su centro medico. Su contrasenya de acceso es: " + generatedPassword +
                            ". Cuando inicie sesion por primera vez, debera cambiarla por una nueva.");
        }).start();

        // Mapear a UsuarioData y retornar
        UsuarioData usuarioData = modelMapper.map(savedUsuario, UsuarioData.class);
        return usuarioData;
    }

    @Transactional
    public UsuarioData actualizarContrasenya(Long id, ActualizarContrasenyaData data) {
        if (data.getContrasenya() == null || data.getContrasenya().length() < 8) {
            throw new IllegalArgumentException("La contrasenya debe tener al menos 8 caracteres");
        }
        if (!data.getContrasenya().equals(data.getConfirmarContrasenya())) {
            throw new IllegalArgumentException("Las contrasenyas no coinciden");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        usuario.setContrasenya(hashPassword(data.getContrasenya()));
        usuario.setPrimerAcceso(false);
        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return modelMapper.map(updatedUsuario, UsuarioData.class);
    }

    @Transactional
    public UsuarioData actualizarDatosResidencia(Long id, ResidenciaData data) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        usuario.setProvincia(data.getProvincia());
        usuario.setMunicipio(data.getMunicipio());
        usuario.setCodigoPostal(data.getCodigoPostal());
        usuario.setPrimerAcceso(false);
        Usuario updatedUsuario = usuarioRepository.save(usuario);
        return modelMapper.map(updatedUsuario, UsuarioData.class);
    }

    @Transactional
    public List<UsuarioData> registrarProfesionalesMedicos(MultipartFile csvFile, CentroMedico centroMedico) throws Exception {
        if (csvFile.isEmpty()) {
            throw new IllegalArgumentException("El fichero CSV no puede estar vacio");
        }
        List<UsuarioData> registrados = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Se asume que el fichero no tiene cabecera y tiene 14 columnas
                String[] columnas = line.split(",");
                if (columnas.length < 14) {
                    throw new IllegalArgumentException("Formato de fichero CSV incorrecto");
                }
                String email            = columnas[13].trim();
                String naf              = columnas[4].trim();
                String ccc              = columnas[5].trim();

                if (usuarioRepository.existsByEmail(email) || profesionalMedicoRepository.existsByNafAndCcc(naf, centroMedico.getCcc())) {
                    continue;
                }

                String nombreCompleto   = columnas[0].trim();
                String especialidadMedica = columnas[1].trim();
                String fechaNacimiento  = columnas[2].trim();
                String genero           = columnas[3].trim();
                String nifNie           = columnas[6].trim();
                String movil            = columnas[7].trim();
                String iban             = columnas[8].trim();
                String pais             = columnas[9].trim();
                String provincia        = columnas[10].trim();
                String municipio        = columnas[11].trim();
                String codigoPostal     = columnas[12].trim();

                // Generar contrasena segura
                String contrasenaGenerada = generateSecurePassword(12);

                // Asignar tipo de usuario profesional medico (se asume que existe un TipoUsuario con id 3)
                TipoUsuario tipoProfesional = tipoUsuarioRepository.findById(3L)
                        .orElseThrow(() -> new IllegalStateException("Tipo de usuario 'profesional_medico' no encontrado"));

                // Establecer relacion bidireccional
                EspecialidadMedica especialidadMedicaParaAsignar = especialidadMedicaRepository.findByNombre(especialidadMedica)
                        .orElse(null);

                // Crear nuevo usuario
                Usuario usuario = new Usuario();
                usuario.setIdentificador(java.util.UUID.randomUUID().toString());
                usuario.setEmail(email);
                usuario.setNombreCompleto(nombreCompleto);
                usuario.setContrasenya(hashPassword(contrasenaGenerada));
                usuario.setActivado(true);
                usuario.setPrimerAcceso(true);
                usuario.setNifNie(nifNie);
                usuario.setTelefono(movil);
                usuario.setPais(pais);
                usuario.setProvincia(provincia);
                usuario.setMunicipio(municipio);
                usuario.setCodigoPostal(codigoPostal);

                usuario.setTipo(tipoProfesional);

                // Crear entidad ProfesionalMedico y asignar campos nuevos
                ProfesionalMedico profesionalMedico = new ProfesionalMedico();
                profesionalMedico.setNaf(naf);
                profesionalMedico.setCcc(ccc);
                profesionalMedico.setIban(iban);
                profesionalMedico.setGenero(genero);
                profesionalMedico.setFechaNacimiento(fechaNacimiento);
                // Asignar el centro medico recibido
                profesionalMedico.setCentroMedico(centroMedico);

                usuario.setProfesionalMedico(profesionalMedico);
                profesionalMedico.setUsuario(usuario);

                if (especialidadMedicaParaAsignar == null) {
                    especialidadMedicaParaAsignar = new EspecialidadMedica();
                    especialidadMedicaParaAsignar.setNombre(especialidadMedica);
                    especialidadMedicaRepository.save(especialidadMedicaParaAsignar);
                    profesionalMedico.setEspecialidadMedica(especialidadMedicaParaAsignar);
                }

                else {
                    profesionalMedico.setEspecialidadMedica(especialidadMedicaParaAsignar);
                }

                // Guardar usuario (se guarda el profesionalMedico por cascada)
                Usuario savedUsuario = usuarioRepository.save(usuario);

                // Enviar email con la contrasenya generada
                new Thread(() -> {
                    emailService.send(
                            email,
                            "Registro Profesional Medico",
                            "Se ha registrado como profesional medico. Su contraseña de acceso es: " + contrasenaGenerada +
                                    ".Al iniciar sesión por primera vez, Se le pedirá cambiar dicha contraseña por una que elija usted."
                    );
                }).start();

                UsuarioData usuarioData = modelMapper.map(savedUsuario, UsuarioData.class);
                registrados.add(usuarioData);
            }
        }
        return registrados;

    }

    @Transactional(readOnly = true)
    public UsuarioData encontrarPorIdPaciente(Long pacienteId) {
        Usuario usuario = usuarioRepository.findByPacienteId(pacienteId).orElse(null);
        if (usuario == null) return null;
        else return modelMapper.map(usuario, UsuarioData.class);
    }

    @Transactional(readOnly = true)
    public UsuarioData encontrarPorIdProfesionalMedico(Long profesionalMedicoId) {
        Usuario usuario = usuarioRepository.findByProfesionalMedicoId(profesionalMedicoId).orElse(null);
        if (usuario == null) return null;
        else return modelMapper.map(usuario, UsuarioData.class);
    }

    public Long obtenerIdProfesionalMedicoAPartirDeIdDelUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        return  usuario.getProfesionalMedico().getId();
    }

    public UsuarioData obtenerUsuarioPacienteAPartirDeNifNie(String nifNie) {
        List<Usuario> usuario = usuarioRepository.findByNifNie(nifNie);
        if (usuario == null) return null;
        Usuario usuarioPaciente = usuario.get(0);
        return modelMapper.map(usuarioPaciente, UsuarioData.class);
    }

}
