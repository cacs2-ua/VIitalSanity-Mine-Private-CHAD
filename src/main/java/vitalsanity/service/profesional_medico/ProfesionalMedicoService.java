package vitalsanity.service.profesional_medico;

import jakarta.validation.constraints.NotNull;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import vitalsanity.dto.general_user.UsuarioData;
import vitalsanity.dto.paciente.BuscarPacienteResponse;
import vitalsanity.dto.paciente.PacienteData;
import vitalsanity.dto.profesional_medico.DocumentoData;
import vitalsanity.dto.profesional_medico.InformeData;
import vitalsanity.dto.profesional_medico.ProfesionalMedicoData;
import vitalsanity.dto.profesional_medico.SolicitudAutorizacionData;
import vitalsanity.model.*;
import vitalsanity.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;


@Service
public class ProfesionalMedicoService {

    @Autowired
    private ProfesionalMedicoRepository profesionalMedicoRepository;

    @Autowired
    private SolicitudAutorizacionRepository solicitudAutorizacionRepository;

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private  UsuarioRepository usuarioRepository;

    @Autowired
    private  PacienteRepository pacienteRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    public ProfesionalMedicoData encontrarPorId(Long profesionalMedicoId) {
        ProfesionalMedico profesionalMedico = profesionalMedicoRepository.findById(profesionalMedicoId).orElse(null);
        if (profesionalMedico == null) return null;
        else return modelMapper.map(profesionalMedico, ProfesionalMedicoData.class);
    }

    @Transactional(readOnly = true)
    public String obtenerNombreCentroMedico(Long profesionalMedicoId) {
        ProfesionalMedico profesionalMedico = profesionalMedicoRepository.findById(profesionalMedicoId).orElse(null);
        if (profesionalMedico == null) return null;
        return profesionalMedico.getCentroMedico().getUsuario().getNombreCompleto();
    }

    @Transactional
    public SolicitudAutorizacionData nuevaSolicitudAutorizacion(Long idUsuarioProfesionalMedico,
                                                                Long idUsuarioPaciente,
                                                                String motivo,
                                                                String descripcion) {
            SolicitudAutorizacion solicitudAutorizacion = new SolicitudAutorizacion();

            Usuario usuarioProfesionalMedico = usuarioRepository.findById(idUsuarioProfesionalMedico).orElse(null);
            Usuario usuarioPaciente = usuarioRepository.findById(idUsuarioPaciente).orElse(null);

            ProfesionalMedico profesionalMedico = profesionalMedicoRepository.findByUsuarioId(idUsuarioProfesionalMedico).orElse(null);
            Paciente paciente = pacienteRepository.findByUsuarioId(idUsuarioPaciente).orElse(null);

            CentroMedico centro = profesionalMedico.getCentroMedico();

            Hibernate.initialize(centro);
            Hibernate.initialize(centro.getUsuario());

            solicitudAutorizacion.setNombreProfesionalMedico(usuarioProfesionalMedico.getNombreCompleto());
            solicitudAutorizacion.setNifNieProfesionalMedico(usuarioProfesionalMedico.getNifNie());
            solicitudAutorizacion.setEspecialidadProfesionalMedico(profesionalMedico.getEspecialidadMedica().getNombre());

            solicitudAutorizacion.setNifCentroMedico(centro.getUsuario().getNifNie());
            solicitudAutorizacion.setNombreCentroMedico(centro.getUsuario().getNombreCompleto());

            solicitudAutorizacion.setNombrePaciente(usuarioPaciente.getNombreCompleto());
            solicitudAutorizacion.setNifNiePaciente(usuarioPaciente.getNifNie());

            solicitudAutorizacion.setMotivo(motivo);
            solicitudAutorizacion.setDescripcion(descripcion);
            solicitudAutorizacion.setFirmada(false);
            solicitudAutorizacion.setCofirmada(false);
            solicitudAutorizacion.setFechaCreacion(LocalDateTime.now());
            solicitudAutorizacion.setDenegada(false);

            solicitudAutorizacion.setProfesionalMedico(profesionalMedico);
            solicitudAutorizacion.setPaciente(paciente);

            solicitudAutorizacionRepository.save(solicitudAutorizacion);
            return modelMapper.map(solicitudAutorizacion, SolicitudAutorizacionData.class);
    }

    @Transactional
    public SolicitudAutorizacionData obtenerUltimaAutorizacionCreadaPorUnProfesionalMedico(Long idUsuarioProfesionalMedico) {
        ProfesionalMedico profesionalMedico = profesionalMedicoRepository.findByUsuarioId(idUsuarioProfesionalMedico).orElse(null);
        SolicitudAutorizacion solicitudAutorizacion = solicitudAutorizacionRepository.findTopByProfesionalMedicoIdOrderByIdDesc(profesionalMedico.getId()).orElse(null);
        return modelMapper.map(solicitudAutorizacion, SolicitudAutorizacionData.class);
    }

    @Transactional
    public  void marcarSolicitudAutorizacionComoFirmada(Long idSolicitudAutorizacion) {
        SolicitudAutorizacion solicitudAutorizacion = solicitudAutorizacionRepository.findById(idSolicitudAutorizacion).orElse(null);
        solicitudAutorizacion.setFirmada(true);
        solicitudAutorizacionRepository.save(solicitudAutorizacion);
    }

    @Transactional
    public  void establecerFechaFirmaAutorizacion(Long idSolicitudAutorizacion, LocalDateTime fechaFirma) {
        SolicitudAutorizacion solicitudAutorizacion = solicitudAutorizacionRepository.findById(idSolicitudAutorizacion).orElse(null);
        solicitudAutorizacion.setFechaFirma(fechaFirma);
        solicitudAutorizacionRepository.save(solicitudAutorizacion);
    }

    @Transactional
    public ProfesionalMedicoData encontrarPorIdUsuario(Long idUsuarioProfesionalMedico) {
        ProfesionalMedico profesionalMedico = profesionalMedicoRepository.findByUsuarioId(idUsuarioProfesionalMedico).orElse(null);
        if (profesionalMedico == null) return null;
        else return modelMapper.map(profesionalMedico, ProfesionalMedicoData.class);
    }

    @Transactional
    public DocumentoData guardarEnBdInformacionSobreElDocumentoAsociadoALaSolicitudDeAutorizacion(
            Long idSolicitudAutorizacion,
            String nombre,
            String tipo_archivo,
            Long tamanyo,
            LocalDateTime fechaSubida) {

        Documento documento = new Documento();

        String uuid = "";

        do {
            uuid = UUID.randomUUID().toString();
        } while(documentoRepository.existsByUuid(uuid));

        documento.setUuid(uuid);

        String s3Key = "autorizaciones/firmadas/" + uuid  + "_" + System.currentTimeMillis() + ".pdf";

        documento.setNombre(nombre);
        documento.setS3_key(s3Key);
        documento.setTipo_archivo(tipo_archivo);
        documento.setTamanyo(tamanyo);
        documento.setFechaSubida(fechaSubida);

        SolicitudAutorizacion solicitudAutorizacion = solicitudAutorizacionRepository.findById(idSolicitudAutorizacion).orElse(null);

        documento.setSolicitudAutorizacion(solicitudAutorizacion);

        documentoRepository.save(documento);

        return modelMapper.map(documento, DocumentoData.class);

    }

    @Transactional(readOnly = true)
    public DocumentoData obtenerDocumentoAsociadoALaSolicitudDeAutorizacion(long idSolicitudDeAutorizacion) {

        List<Documento> documentos = documentoRepository.findAllBySolicitudAutorizacionId(idSolicitudDeAutorizacion);

        if (documentos == null) {
            System.out.println("No se han encontrado documentos asociados a la solicitud de autorización con ID: " + idSolicitudDeAutorizacion);
            return null;
        }
        Documento documentoAsociado = documentos.get(0);
        return modelMapper.map(documentoAsociado, DocumentoData.class);
    }
    @Transactional(readOnly = true)
    public SolicitudAutorizacionData obtenerNombreCentroMedicoAPartirDeIdSolicitudAutorizaciond(Long idSolicitudAutorizaciond) {
        SolicitudAutorizacion solicitudAutorizacion = solicitudAutorizacionRepository.findById(idSolicitudAutorizaciond).orElse(null);
        if (solicitudAutorizacion == null) return null;
        else return modelMapper.map(solicitudAutorizacion, SolicitudAutorizacionData.class);
    }

    @Transactional(readOnly = true)
    public SolicitudAutorizacionData obtenerUltimaAutorizacionAsociadaAUnProfesionalMedicoYAUnPaciente(Long idUsuarioProfesionalMedico, Long idUsuarioPaciente) {
        SolicitudAutorizacion solicitudAutorizacion = solicitudAutorizacionRepository.findTopByProfesionalMedicoIdAndPacienteIdOrderByIdDesc(idUsuarioProfesionalMedico, idUsuarioPaciente).orElse(null);
        if (solicitudAutorizacion == null) return null;
        return modelMapper.map(solicitudAutorizacion, SolicitudAutorizacionData.class);
    }

    @Transactional(readOnly = true)
    public ProfesionalMedicoData encontrarProfesionalMedicoAPartirDeIdSolicitudAutorizacion(Long solicitudId) {
        ProfesionalMedico profesionalMedico = profesionalMedicoRepository.findBySolicitudesAutorizacion_Id(solicitudId).orElse(null);
        if (profesionalMedico == null) return null;
        return modelMapper.map(profesionalMedico, ProfesionalMedicoData.class);
    }

    @Transactional(readOnly = true)
    public List<PacienteData> obtenerPacientesQueHanAutorizado(Long idProfesional) {
        List<Paciente> pacientesQueHanAutorizado = pacienteRepository.findByProfesionalesMedicosAutorizados_IdOrderByIdAsc(idProfesional);

        List<PacienteData> pacientesQueHanAutorizadoData = pacientesQueHanAutorizado.stream()
                .map(paciente -> modelMapper.map(paciente, PacienteData.class))
                .collect(Collectors.toList());

        for (PacienteData paciente : pacientesQueHanAutorizadoData) {
            LocalDate fechaNacimiento = LocalDate.parse(paciente.getFechaNacimiento(), DateTimeFormatter.ISO_LOCAL_DATE);
            int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
            paciente.setEdad(edad);
        }

        return  pacientesQueHanAutorizadoData;
    }

    @Transactional(readOnly = true)
    public List<PacienteData> obtenerPacientesQueHanDesautorizado(Long idProfesional) {
        List<Paciente> pacientesQueHanDesautorizado = pacienteRepository.findByProfesionalesMedicosDesautorizados_IdOrderByIdAsc(idProfesional);

        List<PacienteData> pacientesQueHanDesautorizadoData = pacientesQueHanDesautorizado.stream()
                .map(paciente -> modelMapper.map(paciente, PacienteData.class))
                .collect(Collectors.toList());

        for (PacienteData paciente : pacientesQueHanDesautorizadoData) {
            LocalDate fechaNacimiento = LocalDate.parse(paciente.getFechaNacimiento(), DateTimeFormatter.ISO_LOCAL_DATE);
            int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
            paciente.setEdad(edad);
        }

        return  pacientesQueHanDesautorizadoData;
    }


    @Transactional(readOnly = true)
    public List<SolicitudAutorizacionData> obtenerSolicitudesAutorizacionPendientes(Long idProfesional) {
        List<SolicitudAutorizacion> solicitudesAutorizaciones = solicitudAutorizacionRepository
                .findAllWithAllFetchByProfesionalMedicoIdAndDenegadaFalseAndFirmadaTrueAndCofirmadaFalseOrderByFechaFirmaDesc(idProfesional);

        List<SolicitudAutorizacionData> solicitudesAutorizacionesData = solicitudesAutorizaciones.stream()
                .map(solicitudAutorizacion -> modelMapper.map(solicitudAutorizacion, SolicitudAutorizacionData.class))
                .collect(Collectors.toList());

        for (int i = 0; i < solicitudesAutorizacionesData.size(); i++) {
            ProfesionalMedico profesionalMedico = solicitudesAutorizaciones.get(i).getProfesionalMedico();
            CentroMedico centroMedico = profesionalMedico.getCentroMedico();
            Usuario centroMedicoUsuario = centroMedico.getUsuario();
            solicitudesAutorizacionesData.get(i).setCentroMedicoUsuarioProfesional(modelMapper.map(centroMedicoUsuario, UsuarioData.class));

            PacienteData pacienteData = solicitudesAutorizacionesData.get(i).getPaciente();

            LocalDate fechaNacimiento = LocalDate.parse(pacienteData.getFechaNacimiento(), DateTimeFormatter.ISO_LOCAL_DATE);
            int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
            pacienteData.setEdad(edad);
        }

        return solicitudesAutorizacionesData;
    }

}
