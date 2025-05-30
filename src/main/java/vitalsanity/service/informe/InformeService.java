package vitalsanity.service.informe;

import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import vitalsanity.dto.general_user.UsuarioData;
import vitalsanity.dto.profesional_medico.InformeData;
import vitalsanity.model.*;
import vitalsanity.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.transaction.annotation.Transactional;


@Service
public class InformeService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private InformeRepository informeRepository;

    @Autowired
    private ProfesionalMedicoRepository profesionalMedicoRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private CentroMedicoRepository centroMedicoRepository;

    @Autowired
    private  UsuarioRepository usuarioRepository;

    @Transactional
    public InformeData crearNuevoInforme(
                                  Long profesionalMedicoId,
                                  Long pacienteId,
                                  String titulo,
                                  String descripcion,
                                  String observaciones) {
        ProfesionalMedico profesionalMedico = profesionalMedicoRepository.findById(profesionalMedicoId).orElse(null);
        Paciente paciente = pacienteRepository.findById(pacienteId).orElse(null);

        Informe informe = new Informe();

        String uuid = "";

        do {
            uuid = UUID.randomUUID().toString();
        } while(informeRepository.existsByUuid(uuid));

        informe.setUuid(uuid);

        // NUMERO RANDOM DE 10 CIFRAS

        String identificadorPublico = "";

        do {
            long numero = (long)(Math.random() * 9000000000L) + 1000000000L;
            identificadorPublico = "INF-" + String.valueOf(numero);
        } while(informeRepository.existsByIdentificadorPublico(identificadorPublico));

        informe.setIdentificadorPublico(identificadorPublico);

        informe.setTitulo(titulo);
        informe.setDescripcion(descripcion);
        informe.setObservaciones(observaciones);
        informe.setFechaCreacion(LocalDateTime.now());

        informe.setProfesionalMedico(profesionalMedico);
        informe.setPaciente(paciente);

        informeRepository.save(informe);
        return modelMapper.map(informe, InformeData.class);
    }

    @Transactional
    public InformeData editarInforme(
            Long informeId,
            Long profesionalMedicoId,
            Long pacienteId,
            String titulo,
            String descripcion,
            String observaciones) {
        Informe informe = informeRepository.findById(informeId).orElseThrow(() -> new EntityNotFoundException("Informe no encontrado"));
        ProfesionalMedico profesionalMedico = profesionalMedicoRepository.findById(profesionalMedicoId).orElseThrow(() -> new EntityNotFoundException("Profesional no encontrado"));
        Paciente paciente = pacienteRepository.findById(pacienteId).orElse(null);

        String uuid = "";

        do {
            uuid = UUID.randomUUID().toString();
        } while(informeRepository.existsByUuid(uuid));

        informe.setUuid(uuid);

        informe.setTitulo(titulo);
        informe.setDescripcion(descripcion);
        informe.setObservaciones(observaciones);
        informe.setFechaCreacion(LocalDateTime.now());

        informe.setProfesionalMedico(profesionalMedico);
        informe.setPaciente(paciente);

        informeRepository.save(informe);
        return modelMapper.map(informe, InformeData.class);
    }


    @Transactional
    public  void establecerInformacionFirma(Long informeId) {
        Informe informe = informeRepository.findById(informeId).orElse(null);

        informe.setFirmado(true);
        informe.setFechaFirma(LocalDateTime.now());
        informeRepository.save(informe);
    }

    @Transactional(readOnly = true)
    public  InformeData encontrarPorId(Long informeId) {
        Informe informe = informeRepository.findById(informeId).orElse(null);
        return modelMapper.map(informe, InformeData.class);
    }

    @Transactional(readOnly = true)
    public  InformeData encontrarPorUuid(String uuid) {
        Informe informe = informeRepository.findByUuid(uuid).orElse(null);
        return modelMapper.map(informe, InformeData.class);
    }

    @Transactional(readOnly = true)
    public  List<InformeData> obtenerTodosLosInformesDeUnPaciente(Long pacienteId) {
        List<Informe> informes = informeRepository.findAllByPacienteId(pacienteId);

        List<InformeData> informesData = informes.stream()
                .map(informe -> modelMapper.map(informe, InformeData.class))
                .collect(Collectors.toList());

        for (int i = 0; i < informesData.size(); i++) {
            ProfesionalMedico profesionalMedico = informes.get(i).getProfesionalMedico();
            CentroMedico centroMedico = profesionalMedico.getCentroMedico();
            Usuario centroMedicoUsuario = centroMedico.getUsuario();

            informesData.get(i).setCentroMedicoUsuario(modelMapper.map(centroMedicoUsuario, UsuarioData.class));
        }

        return informesData;
    }

    @Transactional(readOnly = true)
    public  List<InformeData> obtenerFiltradosTodosLosInformesDeUnPaciente (
            Long pacienteId,
            String informeIdentificadorPublico,
            String centroMedicoNombre,
            String profesionalMedicoNombre,
            String especialidadNombre,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            boolean propios,
            String paraInformesPropiosProfesionalMedicoId,
            String profesionalMedicoId) {
        List<Informe> informes = informeRepository.findAllByPacienteId(pacienteId);

        List<InformeData> informesData = informes.stream()
                .map(informe -> modelMapper.map(informe, InformeData.class))
                .collect(Collectors.toList());

        for (int i = 0; i < informesData.size(); i++) {
            ProfesionalMedico profesionalMedico = informes.get(i).getProfesionalMedico();
            CentroMedico centroMedico = profesionalMedico.getCentroMedico();
            Usuario centroMedicoUsuario = centroMedico.getUsuario();

            informesData.get(i).setCentroMedicoUsuario(modelMapper.map(centroMedicoUsuario, UsuarioData.class));
        }

        Stream<InformeData> informesDataFiltrados = informesData.stream();

        if (informeIdentificadorPublico != null && !informeIdentificadorPublico.trim().isEmpty()) {
            informesDataFiltrados = informesDataFiltrados
                    .filter(informeData -> informeData.getIdentificadorPublico().trim().toLowerCase(Locale.ROOT).startsWith(informeIdentificadorPublico.trim().toLowerCase(Locale.ROOT)));

            // informesData = informesDataFiltrados.collect(Collectors.toList());
        }

        if (centroMedicoNombre != null && !centroMedicoNombre.trim().isEmpty()) {
            informesDataFiltrados = informesDataFiltrados
                    .filter(informeData -> informeData.getCentroMedicoUsuario().getNombreCompleto().trim().toLowerCase(Locale.ROOT).startsWith(centroMedicoNombre.trim().toLowerCase(Locale.ROOT)));

            // informesData = informesDataFiltrados.collect(Collectors.toList());
        }

        if (profesionalMedicoNombre != null && !profesionalMedicoNombre.trim().isEmpty()) {
            informesDataFiltrados = informesDataFiltrados
                    .filter(informeData -> informeData.getProfesionalMedico().getUsuario().getNombreCompleto().trim().toLowerCase(Locale.ROOT).startsWith(profesionalMedicoNombre.trim().toLowerCase(Locale.ROOT)));

            // informesData = informesDataFiltrados.collect(Collectors.toList());
        }

        if (especialidadNombre != null && !especialidadNombre.trim().isEmpty()) {
            informesDataFiltrados = informesDataFiltrados
                    .filter(informeData -> informeData.getProfesionalMedico().getEspecialidadMedica().getNombre().trim().equalsIgnoreCase(especialidadNombre.trim()));
            // informesData = informesDataFiltrados.collect(Collectors.toList());
        }

        if (fechaDesde != null) {
            informesDataFiltrados = informesDataFiltrados
                    .filter(informeData -> {
                        LocalDate creacion = informeData.getFechaCreacion().toLocalDate();
                        return creacion.isAfter(fechaDesde) || creacion.isEqual(fechaDesde);
                    });

            // informesData = informesDataFiltrados.collect(Collectors.toList());
        }

        if (fechaHasta != null) {
            informesDataFiltrados = informesDataFiltrados
                    .filter(informeData -> {
                        LocalDate creacion = informeData.getFechaCreacion().toLocalDate();
                        return creacion.isBefore(fechaHasta) || creacion.isEqual(fechaHasta);
                    });

            // informesData = informesDataFiltrados.collect(Collectors.toList());
        }

        if (propios && paraInformesPropiosProfesionalMedicoId != null) {
            informesDataFiltrados = informesDataFiltrados
                    .filter(informeData -> informeData.getProfesionalMedico().getId()
                            .equals(paraInformesPropiosProfesionalMedicoId));

            // informesData = informesDataFiltrados.collect(Collectors.toList());
        }

        if (profesionalMedicoId != null && !profesionalMedicoId.trim().isEmpty()) {
            informesDataFiltrados = informesDataFiltrados
                    .filter(informeData -> informeData.getProfesionalMedico().getId()
                            .equals(profesionalMedicoId));
        }

        return informesDataFiltrados.collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public  List<InformeData> obtenerFiltradosTodosLosInformesPropiosDeUnPacienteQueHaDesautorizado (
            Long pacienteId,
            String informeIdentificadorPublico,
            LocalDate fechaDesde,
            LocalDate fechaHasta,
            String profesionalMedicoId) {
        List<Informe> informes = informeRepository.findAllByPacienteId(pacienteId);

        List<InformeData> informesData = informes.stream()
                .map(informe -> modelMapper.map(informe, InformeData.class))
                .collect(Collectors.toList());

        for (int i = 0; i < informesData.size(); i++) {
            ProfesionalMedico profesionalMedico = informes.get(i).getProfesionalMedico();
            CentroMedico centroMedico = profesionalMedico.getCentroMedico();
            Usuario centroMedicoUsuario = centroMedico.getUsuario();

            informesData.get(i).setCentroMedicoUsuario(modelMapper.map(centroMedicoUsuario, UsuarioData.class));
        }

        Stream<InformeData> informesDataFiltrados = informesData.stream();

        informesDataFiltrados = informesDataFiltrados
                .filter(informeData -> informeData.getProfesionalMedico().getId()
                        .equals(profesionalMedicoId));

        if (informeIdentificadorPublico != null && !informeIdentificadorPublico.trim().isEmpty()) {
            informesDataFiltrados = informesDataFiltrados
                    .filter(informeData -> informeData.getIdentificadorPublico().trim().toLowerCase(Locale.ROOT).startsWith(informeIdentificadorPublico.trim().toLowerCase(Locale.ROOT)));

            // informesData = informesDataFiltrados.collect(Collectors.toList());
        }

        if (fechaDesde != null) {
            informesDataFiltrados = informesDataFiltrados
                    .filter(informeData -> {
                        LocalDate creacion = informeData.getFechaCreacion().toLocalDate();
                        return creacion.isAfter(fechaDesde) || creacion.isEqual(fechaDesde);
                    });

            // informesData = informesDataFiltrados.collect(Collectors.toList());
        }

        if (fechaHasta != null) {
            informesDataFiltrados = informesDataFiltrados
                    .filter(informeData -> {
                        LocalDate creacion = informeData.getFechaCreacion().toLocalDate();
                        return creacion.isBefore(fechaHasta) || creacion.isEqual(fechaHasta);
                    });

            // informesData = informesDataFiltrados.collect(Collectors.toList());
        }

        return informesDataFiltrados.collect(Collectors.toList());
    }



    @Transactional(readOnly = true)
    public InformeData encontrarInformeFullGraphPorId (Long informeId) {

        Informe informe = informeRepository.findWithEverythingById(informeId).orElse(null);

        InformeData informeData = modelMapper.map(informe, InformeData.class);

        ProfesionalMedico profesionalMedico = informe.getProfesionalMedico();
        CentroMedico centroMedico = profesionalMedico.getCentroMedico();
        Usuario centroMedicoUsuario = centroMedico.getUsuario();

        informeData.setCentroMedicoUsuario(modelMapper.map(centroMedicoUsuario, UsuarioData.class));

        LocalDate fechaNacimiento = LocalDate.parse(informeData.getPaciente().getFechaNacimiento(), DateTimeFormatter.ISO_LOCAL_DATE);
        int edad = Period.between(fechaNacimiento, LocalDate.now()).getYears();
        informeData.getPaciente().setEdad(edad);

        return informeData;
    }

}
