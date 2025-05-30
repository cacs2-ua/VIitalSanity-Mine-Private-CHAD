package vitalsanity.service.paciente;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vitalsanity.dto.general_user.UsuarioData;
import vitalsanity.dto.paciente.BuscarPacienteResponse;
import vitalsanity.dto.paciente.PacienteData;
import vitalsanity.dto.profesional_medico.ProfesionalMedicoData;
import vitalsanity.dto.profesional_medico.SolicitudAutorizacionData;
import vitalsanity.model.*;
import vitalsanity.repository.PacienteRepository;
import vitalsanity.repository.ProfesionalMedicoRepository;
import vitalsanity.repository.SolicitudAutorizacionRepository;
import vitalsanity.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.transaction.annotation.Transactional;
import vitalsanity.service.profesional_medico.ProfesionalMedicoService;


@Service
public class PacienteService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private SolicitudAutorizacionRepository solicitudAutorizacionRepository;

    @Autowired
    private ProfesionalMedicoRepository profesionalMedicoRepository;
    @Autowired
    private ProfesionalMedicoService profesionalMedicoService;

    // Metodo para buscar paciente por nifNie (ignora mayusculas/minusculas)
    public BuscarPacienteResponse buscarPacientePorNifNie(String nifNie) {
        if (nifNie == null || nifNie.trim().isEmpty()) {
            return null;
        }
        List<Usuario> usuarios = usuarioRepository.findByNifNie(nifNie);
        for (Usuario usuario : usuarios) {
            if (usuario.getNifNie().equalsIgnoreCase(nifNie)
                    && usuario.getPaciente() != null
                    && usuario.getTipo() != null
                    && usuario.getTipo().getId().equals(4L)) {
                Paciente paciente = usuario.getPaciente();
                BuscarPacienteResponse response = new BuscarPacienteResponse();
                response.setId(usuario.getPaciente().getId());
                response.setNombreCompleto(usuario.getNombreCompleto());
                response.setNifNie(usuario.getNifNie());
                response.setGenero(paciente.getGenero());
                // Se asume que la fecha de nacimiento esta en formato "yyyy-MM-dd"
                try {
                    LocalDate fechaNacimiento = LocalDate.parse(paciente.getFechaNacimiento(), DateTimeFormatter.ISO_LOCAL_DATE);
                    int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
                    response.setEdad(edad);
                } catch (Exception e) {
                    response.setEdad(0);
                }
                return response;
            }
        }
        return null;
    }

    @Transactional(readOnly = true)
    public PacienteData encontrarPorId(Long pacienteId) {
        Paciente paciente = pacienteRepository.findById(pacienteId).orElse(null);
        if (paciente == null) return null;
        else return modelMapper.map(paciente, PacienteData.class);
    }

    @Transactional
    public PacienteData encontrarPorIdUsuario(Long idUsuarioPaciente) {
        Paciente paciente = pacienteRepository.findByUsuarioId(idUsuarioPaciente).orElse(null);
        if (paciente == null) return null;
        return modelMapper.map(paciente, PacienteData.class);
    }

    @Transactional
    public  void marcarSolicitudAutorizacionComoDenegada(Long idSolicitudAutorizacion) {
        SolicitudAutorizacion solicitudAutorizacion = solicitudAutorizacionRepository.findById(idSolicitudAutorizacion).orElse(null);
        solicitudAutorizacion.setDenegada(true);
        solicitudAutorizacionRepository.save(solicitudAutorizacion);
    }

    @Transactional(readOnly = true)
    public List<SolicitudAutorizacionData> obtenerTodasLasSolicitudesValidas(Long pacienteId) {
        List<SolicitudAutorizacion> solicitudesAutorizacion =
                solicitudAutorizacionRepository.findByPacienteIdAndDenegadaFalseAndFirmadaTrueAndCofirmadaFalseOrderByFechaFirmaDesc(pacienteId);

        List<SolicitudAutorizacionData> solicitudesAutorizacionData = solicitudesAutorizacion.stream()
                .map(solicitudAutorizacion -> modelMapper.map(solicitudAutorizacion, SolicitudAutorizacionData.class))
                .collect(Collectors.toList());

        for (int i = 0; i < solicitudesAutorizacionData.size(); i++) {
            ProfesionalMedico profesionalMedico = solicitudesAutorizacion.get(i).getProfesionalMedico();
            CentroMedico centroMedico = profesionalMedico.getCentroMedico();
            Usuario centroMedicoUsuario = centroMedico.getUsuario();
            solicitudesAutorizacionData.get(i).setCentroMedicoUsuarioProfesional(modelMapper.map(centroMedicoUsuario, UsuarioData.class));
        }

        return solicitudesAutorizacionData;
    }

    @Transactional(readOnly = true)
    public SolicitudAutorizacionData obtenerSolicitudPorId(Long solicitudId) {
        SolicitudAutorizacion solicitudAutorizacion = solicitudAutorizacionRepository.findById(solicitudId).orElse(null);
        if (solicitudAutorizacion == null) return null;
        return modelMapper.map(solicitudAutorizacion, SolicitudAutorizacionData.class);
    }

    @Transactional
    public  void marcarSolicitudAutorizacionComoCofirmada(Long idSolicitudAutorizacion) {
        SolicitudAutorizacion solicitudAutorizacion = solicitudAutorizacionRepository.findById(idSolicitudAutorizacion).orElse(null);
        solicitudAutorizacion.setCofirmada(true);
        solicitudAutorizacionRepository.save(solicitudAutorizacion);
    }

    @Transactional
    public  void establecerFechaCofirmaAutorizacion(Long idSolicitudAutorizacion, LocalDateTime fechaCofirma) {
        SolicitudAutorizacion solicitudAutorizacion = solicitudAutorizacionRepository.findById(idSolicitudAutorizacion).orElse(null);
        solicitudAutorizacion.setFechaCofirma(fechaCofirma);
        solicitudAutorizacionRepository.save(solicitudAutorizacion);
    }

    @Transactional(readOnly = true)
    public PacienteData encontrarPacienteAPartirDeIdSolicitudAutorizacion(Long solicitudId) {
        Paciente paciente = pacienteRepository.findBySolicitudesAutorizacion_Id(solicitudId).orElse(null);
        if (paciente == null) return null;
        return modelMapper.map(paciente, PacienteData.class);
    }

    @Transactional
    public void agregarProfesionalMedicoAutorizado(Long idPaciente, Long idProfesionalMedico) {
        Paciente paciente = pacienteRepository.findById(idPaciente).orElse(null);
        ProfesionalMedico profesionalMedico = profesionalMedicoRepository.findById(idProfesionalMedico).orElse(null);
        paciente.addProfesionalMedicoAutorizado(profesionalMedico);
    }

    @Transactional
    public void agregarProfesionalMedicoDesautorizado(Long idPaciente, Long idProfesionalMedico) {
        Paciente paciente = pacienteRepository.findById(idPaciente).orElse(null);
        ProfesionalMedico profesionalMedico = profesionalMedicoRepository.findById(idProfesionalMedico).orElse(null);
        paciente.addProfesionalMedicoDesautorizado(profesionalMedico);

        pacienteRepository.save(paciente);
    }

    @Transactional(readOnly = true)
    public List<ProfesionalMedicoData> obtenerProfesionalesMedicosAutorizados(Long idPaciente) {
        List<ProfesionalMedico> profesionalMedicosAutorizados = profesionalMedicoRepository.findByPacientesQueHanAutorizado_IdOrderByIdAsc(idPaciente);

        List<ProfesionalMedicoData> profesionalesMedicosAutorizadosData = profesionalMedicosAutorizados.stream()
                .map(profesionalMedicoAutorizado -> modelMapper.map(profesionalMedicoAutorizado, ProfesionalMedicoData.class))
                .collect(Collectors.toList());

        for (int i = 0; i < profesionalesMedicosAutorizadosData.size(); i++) {
            ProfesionalMedico profesionalMedico = profesionalMedicosAutorizados.get(i);
            CentroMedico centroMedico = profesionalMedico.getCentroMedico();
            Usuario centroMedicoUsuario = centroMedico.getUsuario();
            profesionalesMedicosAutorizadosData.get(i).setCentroMedicoUsuarioProfesional(modelMapper.map(centroMedicoUsuario, UsuarioData.class));
        }

        return  profesionalesMedicosAutorizadosData;
    }

    @Transactional(readOnly = true)
    public SolicitudAutorizacionData obtenerUltimaSolicitudAutorizacionValida(Long idProfesionalMedico, Long idPaciente) {
        SolicitudAutorizacion solicitudAutorizacion = solicitudAutorizacionRepository.findTopByProfesionalMedicoIdAndPacienteIdAndDenegadaFalse(idProfesionalMedico, idPaciente).orElse(null);
        return  modelMapper.map(solicitudAutorizacion, SolicitudAutorizacionData.class);
    }

    @Transactional(readOnly = true)
    public  List<PacienteData> obtenerFiltradosPacientesQueHanAutorizado (
            Long profesionalMedicoId,
            String pacienteNombre,
            String nifNiePaciente,
            Integer edadMinima,
            Integer edadMaxima) {

        List<Paciente> pacientes = pacienteRepository.findByProfesionalesMedicosAutorizados_IdOrderByIdAsc(profesionalMedicoId);

        List<PacienteData> pacientesData = pacientes.stream()
                .map(paciente -> modelMapper.map(paciente, PacienteData.class))
                .collect(Collectors.toList());

        for (PacienteData paciente : pacientesData) {
            LocalDate fechaNacimiento = LocalDate.parse(paciente.getFechaNacimiento(), DateTimeFormatter.ISO_LOCAL_DATE);
            int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
            paciente.setEdad(edad);
        }

        Stream<PacienteData> pacientesDataFiltrados = pacientesData.stream();

        if (pacienteNombre != null && !pacienteNombre.trim().isEmpty()) {
            pacientesDataFiltrados = pacientesDataFiltrados
                    .filter(pacienteData -> pacienteData.getUsuario().getNombreCompleto().trim().toLowerCase(Locale.ROOT).startsWith(pacienteNombre.trim().toLowerCase(Locale.ROOT)));
        }

        if (nifNiePaciente != null && !nifNiePaciente.trim().isEmpty()) {
            pacientesDataFiltrados = pacientesDataFiltrados
                    .filter(pacienteData -> pacienteData.getUsuario().getNifNie().trim().equalsIgnoreCase(nifNiePaciente.trim()));
        }

        if (edadMinima != null) {
            pacientesDataFiltrados = pacientesDataFiltrados
                    .filter(pacienteData -> pacienteData.getEdad() >= edadMinima);
        }

        if (edadMaxima != null) {
            pacientesDataFiltrados = pacientesDataFiltrados
                    .filter(pacienteData -> pacienteData.getEdad() <= edadMaxima);
        }

        return pacientesDataFiltrados.collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public  List<PacienteData> obtenerFiltradosPacientesQueHanDesautorizado (
            Long profesionalMedicoId,
            String pacienteNombre,
            String nifNiePaciente,
            Integer edadMinima,
            Integer edadMaxima) {

        List<Paciente> pacientes = pacienteRepository.findByProfesionalesMedicosDesautorizados_IdOrderByIdAsc(profesionalMedicoId);

        List<PacienteData> pacientesData = pacientes.stream()
                .map(paciente -> modelMapper.map(paciente, PacienteData.class))
                .collect(Collectors.toList());

        for (PacienteData paciente : pacientesData) {
            LocalDate fechaNacimiento = LocalDate.parse(paciente.getFechaNacimiento(), DateTimeFormatter.ISO_LOCAL_DATE);
            int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
            paciente.setEdad(edad);
        }

        Stream<PacienteData> pacientesDataFiltrados = pacientesData.stream();

        if (pacienteNombre != null && !pacienteNombre.trim().isEmpty()) {
            pacientesDataFiltrados = pacientesDataFiltrados
                    .filter(pacienteData -> pacienteData.getUsuario().getNombreCompleto().trim().toLowerCase(Locale.ROOT).startsWith(pacienteNombre.trim().toLowerCase(Locale.ROOT)));
        }

        if (nifNiePaciente != null && !nifNiePaciente.trim().isEmpty()) {
            pacientesDataFiltrados = pacientesDataFiltrados
                    .filter(pacienteData -> pacienteData.getUsuario().getNifNie().trim().equalsIgnoreCase(nifNiePaciente.trim()));
        }

        if (edadMinima != null) {
            pacientesDataFiltrados = pacientesDataFiltrados
                    .filter(pacienteData -> pacienteData.getEdad() >= edadMinima);
        }

        if (edadMaxima != null) {
            pacientesDataFiltrados = pacientesDataFiltrados
                    .filter(pacienteData -> pacienteData.getEdad() <= edadMaxima);
        }

        return pacientesDataFiltrados.collect(Collectors.toList());
    }

}
