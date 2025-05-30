package vitalsanity.controller.profesional_medico;

import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import vitalsanity.authentication.ManagerUserSession;
import vitalsanity.controller.exception.NotFoundException;
import vitalsanity.dto.general_user.UsuarioData;
import vitalsanity.dto.paciente.BuscarPacienteData;
import vitalsanity.dto.paciente.BuscarPacienteResponse;
import vitalsanity.dto.paciente.PacienteData;
import vitalsanity.dto.profesional_medico.*;
import vitalsanity.repository.InformeRepository;
import vitalsanity.service.documento.DocumentoService;
import vitalsanity.service.especialidad_medica.EspecialidadMedicaService;
import vitalsanity.service.general_user.UsuarioService;
import vitalsanity.service.informe.InformeService;
import vitalsanity.service.paciente.PacienteService;
import vitalsanity.service.profesional_medico.ProfesionalMedicoService;
import vitalsanity.service.utils.EmailService;
import vitalsanity.service.utils.autofirma.GenerarPdf;
import vitalsanity.service.utils.aws.S3VitalSanityService;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class ProfesionalMedicoController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private InformeService informeService;

    @Autowired
    private DocumentoService documentoService;

    @Autowired
    private ProfesionalMedicoService profesionalMedicoService;

    @Autowired
    private EspecialidadMedicaService especialidadMedicaService;

    @Autowired
    private PacienteService pacienteService;

    @Autowired
    EmailService emailService;

    @Autowired
    private GenerarPdf generarPdf;

    @Autowired
    private S3VitalSanityService s3VitalSanityService;

    @Autowired
    private ManagerUserSession managerUserSession;
    private InformeRepository informeRepository;
    @Autowired
    private ModelMapper modelMapper;

    private Long getUsuarioLogeadoId() {
        return managerUserSession.usuarioLogeado();
    }

    @GetMapping("/api/profesional-medico/buscar-paciente")
    public String buscarPacienteForm(Model model) {
        model.addAttribute("buscarPacienteData", new BuscarPacienteData());
        return "profesional_medico/buscar-paciente";
    }

    @PostMapping("/api/profesional-medico/buscar-paciente")
    public String buscarPacienteSubmit(@ModelAttribute("buscarPacienteData") BuscarPacienteData buscarPacienteData,
                                       Model model) {
        Long idUsuarioProfesionalMedico = getUsuarioLogeadoId();
        String nif = buscarPacienteData.getNifNie().trim();
        BuscarPacienteResponse pacienteResponse = pacienteService.buscarPacientePorNifNie(nif);

        if (pacienteResponse == null) {
            model.addAttribute("error", "Paciente no encontrado");
        } else {

            Long idProfesional = Long.parseLong(profesionalMedicoService.encontrarPorIdUsuario(idUsuarioProfesionalMedico).getId());
            Long idPaciente = pacienteResponse.getId();
            UsuarioData usuarioPaciente = usuarioService.encontrarPorIdPaciente(pacienteResponse.getId());
            SolicitudAutorizacionData solicitudAutorizacion = profesionalMedicoService.obtenerUltimaAutorizacionAsociadaAUnProfesionalMedicoYAUnPaciente(
                    idProfesional, idPaciente);

            boolean solicitada = false;
            if (solicitudAutorizacion != null
                && !solicitudAutorizacion.isDenegada()
                && solicitudAutorizacion.isFirmada()) {
                solicitada = true;
            }
            model.addAttribute("paciente", pacienteResponse);
            model.addAttribute("usuarioPaciente", usuarioPaciente);
            model.addAttribute("solicitada", solicitada);
        }
        return "profesional_medico/buscar-paciente";
    }

    // LOGICA AUTOFIRMA

    @GetMapping("/api/profesional-medico/solicitar-autorizacion/{idUsuarioPaciente}")
    public String solicitarAutorizacion(@PathVariable(value="idUsuarioPaciente") Long idUsuarioPaciente,
                                        Model model) {
        Long idUsuarioProfesionalMedico = getUsuarioLogeadoId();
        UsuarioData usuarioProfesionalMedico = usuarioService.findById(idUsuarioProfesionalMedico);
        UsuarioData usuarioPaciente = usuarioService.findById(idUsuarioPaciente);

        Long idProfesionalMedico = usuarioService.obtenerIdProfesionalMedicoAPartirDeIdDelUsuario(idUsuarioProfesionalMedico);

        String nombreCentroMedico = profesionalMedicoService.obtenerNombreCentroMedico(idProfesionalMedico);

        model.addAttribute("usuarioProfesionalMedico", usuarioProfesionalMedico);
        model.addAttribute("usuarioPaciente", usuarioPaciente);
        model.addAttribute("nombreCentroMedico", nombreCentroMedico);

        return "profesional_medico/solicitar-autorizacion";
    }


    @PostMapping("/api/profesional-medico/generar-pdf-autorizacion")
    @ResponseBody
    public String generarPdfAutorizacionYAlmacenarSolicitudDeAutorizacionEnBaseDeDatos(
                              @RequestParam String nombreProfesional,
                              @RequestParam String nifNieProfesional,
                              @RequestParam String nombreCentroMedico,
                              @RequestParam String nombrePaciente,
                              @RequestParam String nifNiePaciente,
                              @RequestParam String motivo,
                              @RequestParam String descripcion) {

        Long idUsuarioProfesionalMedico = getUsuarioLogeadoId();
        Long idUsuarioPaciente = usuarioService.obtenerUsuarioPacienteAPartirDeNifNie(nifNiePaciente).getId();
        profesionalMedicoService.nuevaSolicitudAutorizacion(
                idUsuarioProfesionalMedico,
                idUsuarioPaciente,
                motivo,
                descripcion
        );

        byte[] pdfBytes = generarPdf.generarPdfAutorizacion(
                idUsuarioProfesionalMedico,
                idUsuarioPaciente,
                motivo,
                descripcion);
        return Base64.getEncoder().encodeToString(pdfBytes);
    }

    @PostMapping("/api/profesional-medico/pdf-autorizacion-firmada")
    @ResponseBody
    public String subirPdfAutorizacionFirmadaEnAws(@RequestParam String signedPdfBase64) throws IOException {
            Long idUsuarioProfesionalMedico = getUsuarioLogeadoId();
            SolicitudAutorizacionData ultimaSolicitudCreadaDelProfesionalMedico =
                    profesionalMedicoService.obtenerUltimaAutorizacionCreadaPorUnProfesionalMedico(idUsuarioProfesionalMedico);

            Long idUltimaSolicitudCreadaDelProfesionalMedico = ultimaSolicitudCreadaDelProfesionalMedico.getId();
            profesionalMedicoService.establecerFechaFirmaAutorizacion(idUltimaSolicitudCreadaDelProfesionalMedico, LocalDateTime.now());
            profesionalMedicoService.marcarSolicitudAutorizacionComoFirmada(idUltimaSolicitudCreadaDelProfesionalMedico);

            String nifNiePaciente = ultimaSolicitudCreadaDelProfesionalMedico.getNifNiePaciente();
            UsuarioData usuarioPaciente = usuarioService.obtenerUsuarioPacienteAPartirDeNifNie(nifNiePaciente);
            UsuarioData usuarioProfesionalMedico = usuarioService.findById(idUsuarioProfesionalMedico);

            String uuidUsuarioPaciente = usuarioPaciente.getUuid();
            String uuidUsuarioProfesionalMedico = usuarioProfesionalMedico.getUuid();

            byte[] signedPdf = Base64.getDecoder().decode(signedPdfBase64);
            String nombreArchivo = uuidUsuarioProfesionalMedico + "_" + uuidUsuarioPaciente  + "_" + System.currentTimeMillis() + ".pdf";
            String tipoArchivo = "application/pdf";
            Long tamano = (long) signedPdf.length;
            LocalDateTime fechaSubida = LocalDateTime.now();

            DocumentoData documento = profesionalMedicoService.guardarEnBdInformacionSobreElDocumentoAsociadoALaSolicitudDeAutorizacion(
                    idUltimaSolicitudCreadaDelProfesionalMedico,
                    nombreArchivo,
                    tipoArchivo,
                    tamano,
                    fechaSubida
            );

            s3VitalSanityService.subirFicheroBytes(documento.getS3_key(), signedPdf);

            String emailPaciente = usuarioPaciente.getEmail();

            String nombrePaciente = usuarioPaciente.getNombreCompleto();


            String nifNieProfesionalMedico = usuarioProfesionalMedico.getNifNie();
            String nombreProfesionalMedico = usuarioProfesionalMedico.getNombreCompleto();

            Long idProfesionalMedico = usuarioService.obtenerIdProfesionalMedicoAPartirDeIdDelUsuario(idUsuarioProfesionalMedico);

            String nombreCentroMedico = profesionalMedicoService.obtenerNombreCentroMedico(idProfesionalMedico);

            String subject = "Solicitud de autorización por parte del profesional médico: " + nombreProfesionalMedico;

            String text = "Estimad@ :'" + usuarioPaciente.getNombreCompleto() + "'. El profesional médico: '" + nombreProfesionalMedico + "' con NIF/NIE: '"
                    + nifNieProfesionalMedico + "' le ha solicitado autorización para acceder a su historial clínico desde el centro médico: '"
                    + nombreCentroMedico + "' . Puede ver esta solicitud dentro del apartado de 'Solicitudes de autorización'.  "
                    + "Una vez haya revisado la solicitud, usted podrá autorizar o denegar el acceso a su historial médico. "
                    + "Si usted autoriza el acceso al profesional médico, dicho profesional médico podrá acceder a su historial clínico centralizado, "
                    + "lo cual podría ayudar a agilizar el proceso de diagnóstico y tratamiento, mejorando así su atención médica y la calidad de su servicio. "
                    + "Le recordamos que cualquier tratamiento de datos está sujeto a las leyes de protección de datos vigentes. ";

            new Thread(() -> {
                emailService.send(emailPaciente, subject, text);
            }).start();

            return documento.getUuid();
    }

    @GetMapping("/api/profesional-medico/pdf-autorizacion-firmada")
    public String descargarPdfAutorizacionFirmadaDeAws(Model model) {
        Long idUsuarioProfesionalMedico = getUsuarioLogeadoId();
        SolicitudAutorizacionData ultimaSolicitudCreadaDelProfesionalMedico =
                profesionalMedicoService.obtenerUltimaAutorizacionCreadaPorUnProfesionalMedico(idUsuarioProfesionalMedico);

        Long idSolicitudAutorizacion = ultimaSolicitudCreadaDelProfesionalMedico.getId();

        DocumentoData documentoData = profesionalMedicoService.obtenerDocumentoAsociadoALaSolicitudDeAutorizacion(idSolicitudAutorizacion);

        String s3Key = documentoData.getS3_key();

        String urlPrefirmada = s3VitalSanityService.generarUrlPrefirmada(
                s3Key,
                Duration.ofMinutes(5));
        model.addAttribute("urlPrefirmada", urlPrefirmada);
        return "profesional_medico/descargar-pdf-autorizacion-firmada";
    }


    @GetMapping("/api/profesional-medico/pacientes-que-han-autorizado")
    public String verPacientesQueHanAutorizado(Model model,
                                               @RequestParam(required = false) String pacienteNombre,
                                               @RequestParam(required = false) String nifNiePaciente,
                                               @RequestParam(required = false) Integer edadMinima,
                                               @RequestParam(required = false) Integer edadMaxima) {
        Long idUsuarioPaciente = getUsuarioLogeadoId();
        ProfesionalMedicoData profesionalMedicoData = profesionalMedicoService.encontrarPorIdUsuario(idUsuarioPaciente);
        List<PacienteData> pacientesData = pacienteService
                                .obtenerFiltradosPacientesQueHanAutorizado(
                                    Long.parseLong(profesionalMedicoData.getId()),
                                    pacienteNombre,
                                    nifNiePaciente,
                                    edadMinima,
                                    edadMaxima);


        boolean noHayPacientes = false;
        if (pacientesData.isEmpty()) {
            noHayPacientes = true;
        }
        model.addAttribute("pacientes", pacientesData);
        model.addAttribute("noHayPacientes", noHayPacientes);

        // FILTROS
        model.addAttribute("pacienteNombre", pacienteNombre);
        model.addAttribute("nifNiePaciente", nifNiePaciente);
        model.addAttribute("edadMinima", edadMinima);
        model.addAttribute("edadMaxima", edadMaxima);

        return "profesional_medico/listado-pacientes-que-han-autorizado";
    }

    @GetMapping("/api/profesional-medico/pacientes-que-han-desautorizado")
    public String verPacientesQueHanDesautorizado(Model model,
                                                  @RequestParam(required = false) String pacienteNombre,
                                                  @RequestParam(required = false) String nifNiePaciente,
                                                  @RequestParam(required = false) Integer edadMinima,
                                                  @RequestParam(required = false) Integer edadMaxima) {

        Long idprofesionalMedicoUsuario = getUsuarioLogeadoId();
        ProfesionalMedicoData profesionalMedicoData = profesionalMedicoService.encontrarPorIdUsuario(idprofesionalMedicoUsuario);
        List<PacienteData> pacientesData = pacienteService
                .obtenerFiltradosPacientesQueHanDesautorizado(
                        Long.parseLong(profesionalMedicoData.getId()),
                        pacienteNombre,
                        nifNiePaciente,
                        edadMinima,
                        edadMaxima);

        boolean noHayPacientes = false;
        if (pacientesData.isEmpty()) {
            noHayPacientes = true;
        }

        model.addAttribute("profesionalMedicoId", profesionalMedicoData.getId());
        model.addAttribute("pacientes", pacientesData);
        model.addAttribute("noHayPacientes", noHayPacientes);

        // FILTROS
        model.addAttribute("pacienteNombre", pacienteNombre);
        model.addAttribute("nifNiePaciente", nifNiePaciente);
        model.addAttribute("edadMinima", edadMinima);
        model.addAttribute("edadMaxima", edadMaxima);

        return "profesional_medico/listado-pacientes-que-han-desautorizado";
    }

    @GetMapping("/api/profesional-medico/pacientes/{pacienteId}/informes/nuevo")
    public String crearNuevoInforme(@PathVariable(value="pacienteId") Long pacienteId,
                                    Model model,
                                    HttpServletRequest request) {
        Long idUsuarioProfesionalMedico = getUsuarioLogeadoId();
        Long profesionalMedicoId = usuarioService.obtenerIdProfesionalMedicoAPartirDeIdDelUsuario(idUsuarioProfesionalMedico);
        model.addAttribute("contextPath", request.getContextPath());
        model.addAttribute("profesionalMedicoId", profesionalMedicoId);
        model.addAttribute("pacienteId", pacienteId);
        return "profesional_medico/crear-nuevo-informe";
    }


    // LÓGICA DE LA GENERACIÓN DEL INFORME

    @PostMapping("/api/profesional-medico/generar-pdf-informe")
    @ResponseBody
    public SubirInformeResponse generarPdfDelInforme(
            @RequestParam(required = false) Long informeId,
            @RequestParam Long profesionalMedicoId,
            @RequestParam Long pacienteId,
            @RequestParam String titulo,
            @RequestParam String descripcion,
            @RequestParam String observaciones) {

        InformeData informe = new InformeData();

        byte[] pdfBytes = new byte[0];
        if (informeId != null) {
            informe = informeService.editarInforme(
                    informeId,
                    profesionalMedicoId,
                    pacienteId,
                    titulo,
                    descripcion,
                    observaciones
            );

            pdfBytes = generarPdf.generarPdfEdicionInforme(
                    profesionalMedicoId,
                    pacienteId,
                    titulo,
                    descripcion,
                    observaciones);
        }

        else {
            informe = informeService.crearNuevoInforme(
                    profesionalMedicoId,
                    pacienteId,
                    titulo,
                    descripcion,
                    observaciones);

            pdfBytes = generarPdf.generarPdfInforme(
                    profesionalMedicoId,
                    pacienteId,
                    titulo,
                    descripcion,
                    observaciones);
        }

        String pdfBase64 = Base64.getEncoder().encodeToString(pdfBytes);

        return new SubirInformeResponse(informe.getId(), pdfBase64);
    }

    @PostMapping("/api/profesional-medico/pdf-informe-firmado")
    @ResponseBody
    public String subirPdfInformeFirmadoEnAws(@RequestBody SubirInformeResponse body) throws IOException {
        Long idUsuarioProfesionalMedico = getUsuarioLogeadoId();
        Long informeId = body.getIdInforme();
        InformeData informeData = informeService.encontrarPorId(informeId);

        informeService.establecerInformacionFirma(informeId);

        String informeFirmado = body.getPdfBase64();
        byte[] informeFirmadoBytes = Base64.getDecoder().decode(informeFirmado);

        String uuid = informeData.getUuid();
        String nombreArchivo = uuid;

        String s3Key = "informes/firmados/" + nombreArchivo + "_" + System.currentTimeMillis() + ".pdf";

        String tipoArchivo = "application/pdf";

        Long tamano = (long) informeFirmadoBytes.length;

        documentoService.crearNuevoDocumento(
                informeId,
                informeData.getUuid(),
                nombreArchivo,
                s3Key,
                tipoArchivo,
                tamano
        );

        s3VitalSanityService.subirFicheroBytes(s3Key, informeFirmadoBytes);

        Long pacienteId = informeData.getPaciente().getId();

        UsuarioData pacienteUsuario = usuarioService.encontrarPorIdPaciente(pacienteId);

        UsuarioData usuarioProfesionalMedico = usuarioService.findById(idUsuarioProfesionalMedico);
        String email = pacienteUsuario.getEmail();
        String subject = "Nuevo informe subido con éxito";
        String text = "Estimad@: '" + pacienteUsuario.getNombreCompleto() + "'. El profesional médico: '" + usuarioProfesionalMedico.getNombreCompleto() + "' " +
                "con Nº de documento de identidad: '" + usuarioProfesionalMedico.getNifNie() + "' ha subido a su historial médico centralizado un informe con título: '"
                + informeData.getTitulo() + "'. Podrá acceder a toda la información completa de dicho informe desde la pestaña 'Mis informes'. " +
                "Le recordamos que cualquier tratamiento de datos está sujeto a las leyes de protección de datos vigentes.";

        new Thread(() -> {
            emailService.send(email, subject, text);
        }).start();

        return uuid;
    }

    @GetMapping("/api/profesional-medico/descargar-pdf-informe-firmado")
    public String descargarPdfInformeFirmadoDeAws(@RequestParam String uuid,
                                                         Model model) {

        DocumentoData documentoData = documentoService.encontrarPorUuid(uuid);
        String s3Key = documentoData.getS3_key();
        String urlPrefirmada = s3VitalSanityService.generarUrlPrefirmada(
                s3Key,
                Duration.ofMinutes(5));
        model.addAttribute("urlPrefirmada", urlPrefirmada);
        return "profesional_medico/descargar-pdf-informe-firmado";

    }

    @GetMapping("/api/profesional-medico/pacientes/{pacienteId}/informes")
    public String verInformesPaciente(@PathVariable(value="pacienteId") Long pacienteId,
                                      @RequestParam(required = false) String informeIdentificadorPublico,
                                      @RequestParam(required = false) String centroMedicoNombre,
                                      @RequestParam(required = false) String profesionalMedicoNombre,
                                      @RequestParam(required = false) String especialidadNombre,
                                      @RequestParam(required = false) LocalDate fechaDesde,
                                      @RequestParam(required = false) LocalDate fechaHasta,
                                      @RequestParam(required = false) boolean propios,
                                      @RequestParam(required = false) String profMedId,
                                      Model model) {
        Long idUsuarioProfesionalMedico = getUsuarioLogeadoId();
        String profesionalMedicoId = String.valueOf(
                usuarioService.obtenerIdProfesionalMedicoAPartirDeIdDelUsuario(idUsuarioProfesionalMedico)
        );

        List<EspecialidadMedicaData> especialidadesMedicas = especialidadMedicaService.encontrarTodasLasEspecialidadesMedicas();

        UsuarioData pacienteUsuario = usuarioService.encontrarPorIdPaciente(pacienteId);
        String pacienteNombre = pacienteUsuario.getNombreCompleto();
        String pacienteNifNie = pacienteUsuario.getNifNie();
        List<InformeData> informes = informeService.
                obtenerFiltradosTodosLosInformesDeUnPaciente(
                        pacienteId,
                        informeIdentificadorPublico,
                        centroMedicoNombre,
                        profesionalMedicoNombre,
                        especialidadNombre,
                        fechaDesde,
                        fechaHasta,
                        propios,
                        profesionalMedicoId,
                        profMedId);

        boolean noHayInformes = false;
        if (informes.isEmpty()) {
            noHayInformes = true;
        }

        model.addAttribute("profesionalMedicoAutenticadoId", profesionalMedicoId);
        model.addAttribute("especialidadesMedicas", especialidadesMedicas);
        model.addAttribute("pacienteNombre", pacienteNombre);
        model.addAttribute("pacienteNifNie", pacienteNifNie);
        model.addAttribute("noHayInformes", noHayInformes);
        model.addAttribute("informes", informes);


        // FILTROS

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String fechaDesdeStr = (fechaDesde != null)
                ? fechaDesde.format(fmt)
                : "";

        String fechaHastaStr = (fechaHasta != null)
                ? fechaHasta.format(fmt)
                : "";

        model.addAttribute("pacienteId", pacienteId);
        model.addAttribute("informeIdentificadorPublico", informeIdentificadorPublico);
        model.addAttribute("centroMedicoNombre", centroMedicoNombre);
        model.addAttribute("profesionalMedicoNombre", profesionalMedicoNombre);
        model.addAttribute("especialidadNombre", especialidadNombre);
        model.addAttribute("fechaDesdeStr", fechaDesdeStr);
        model.addAttribute("fechaHastaStr", fechaHastaStr);
        model.addAttribute("propios", propios);
        model.addAttribute("profMedId", profMedId);


        return "profesional_medico/ver-informes-del-paciente";
    }


    @GetMapping("/api/profesional-medico/pacientes-desautorizados/{pacienteId}/informes")
    public String verInformesPropiosDeUnPacienteQueHaDesautorizado(@PathVariable(value="pacienteId") Long pacienteId,
                                      @RequestParam(required = false) String informeIdentificadorPublico,
                                      @RequestParam(required = false) LocalDate fechaDesde,
                                      @RequestParam(required = false) LocalDate fechaHasta,
                                      Model model) {
        Long idUsuarioProfesionalMedico = getUsuarioLogeadoId();
        String profesionalMedicoId = String.valueOf(
                usuarioService.obtenerIdProfesionalMedicoAPartirDeIdDelUsuario(idUsuarioProfesionalMedico)
        );

        if (pacienteId == null) {
            throw new NotFoundException("Ha ocurrido algún error");
        }

        List<EspecialidadMedicaData> especialidadesMedicas = especialidadMedicaService.encontrarTodasLasEspecialidadesMedicas();

        UsuarioData pacienteUsuario = usuarioService.encontrarPorIdPaciente(pacienteId);
        String pacienteNombre = pacienteUsuario.getNombreCompleto();
        String pacienteNifNie = pacienteUsuario.getNifNie();
        List<InformeData> informes = informeService.
                obtenerFiltradosTodosLosInformesPropiosDeUnPacienteQueHaDesautorizado(
                        pacienteId,
                        informeIdentificadorPublico,
                        fechaDesde,
                        fechaHasta,
                        profesionalMedicoId);

        boolean noHayInformes = false;
        if (informes.isEmpty()) {
            noHayInformes = true;
        }

        model.addAttribute("profesionalMedicoAutenticadoId", profesionalMedicoId);
        model.addAttribute("especialidadesMedicas", especialidadesMedicas);
        model.addAttribute("pacienteNombre", pacienteNombre);
        model.addAttribute("pacienteNifNie", pacienteNifNie);
        model.addAttribute("noHayInformes", noHayInformes);
        model.addAttribute("informes", informes);


        // FILTROS

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String fechaDesdeStr = (fechaDesde != null)
                ? fechaDesde.format(fmt)
                : "";

        String fechaHastaStr = (fechaHasta != null)
                ? fechaHasta.format(fmt)
                : "";

        model.addAttribute("pacienteId", pacienteId);
        model.addAttribute("informeIdentificadorPublico", informeIdentificadorPublico);
        model.addAttribute("fechaDesdeStr", fechaDesdeStr);
        model.addAttribute("fechaHastaStr", fechaHastaStr);

        return "profesional_medico/ver-informes-propios-de-un-paciente-que-ha-desautorizado";
    }


    @GetMapping("/api/profesional-medico/pacientes/{pacienteId}/informes/{informeId}/editar")
    public String editarInforme(@PathVariable(value="pacienteId") Long pacienteId,
                                @PathVariable(value="informeId") Long informeId,
                                Model model,
                                HttpServletRequest request) {
        InformeData informe = informeService.encontrarPorId(informeId);
        Long idUsuarioProfesionalMedico = getUsuarioLogeadoId();
        Long profesionalMedicoId = usuarioService.obtenerIdProfesionalMedicoAPartirDeIdDelUsuario(idUsuarioProfesionalMedico);
        model.addAttribute("contextPath", request.getContextPath());
        model.addAttribute("profesionalMedicoId", profesionalMedicoId);
        model.addAttribute("pacienteId", pacienteId);
        model.addAttribute("informe", informe);
        model.addAttribute("informeId", informeId);
        return "profesional_medico/editar-informe";
    }



    @GetMapping("/api/profesional-medico/pacientes/informes/{informeId}/ver-detalles")
    public String verDetallesInformePaciente(@PathVariable(value="informeId") Long informeId,
                                             Model model) {
        Long idUsuarioProfesionalMedico = getUsuarioLogeadoId();
        String profesionalMedicoId = String.valueOf(
                usuarioService.obtenerIdProfesionalMedicoAPartirDeIdDelUsuario(idUsuarioProfesionalMedico)
        );
        InformeData informe = informeService.encontrarInformeFullGraphPorId(informeId);

        List <DocumentoData> documentos = documentoService.obtenerDocumentosAsociadosAUnInforme(informeId);

        boolean noHayDocumentos = false;

        if (documentos.isEmpty()) {
            noHayDocumentos = true;
        }

        model.addAttribute("profesionalMedicoAutenticadoId", profesionalMedicoId);
        model.addAttribute("informeId", informeId );
        model.addAttribute("informe", informe);
        model.addAttribute("documentos", documentos);
        model.addAttribute("noHayDocumentos", noHayDocumentos);
        return "profesional_medico/ver-detalles-informe";
    }

    @GetMapping("/api/profesional-medico/pacientes-que-han-desautorizado/informes/{informeId}/ver-detalles")
    public String verDetallesInformePacienteQueHaDesautorizado(@PathVariable(value="informeId") Long informeId,
                                             Model model) {
        Long idUsuarioProfesionalMedico = getUsuarioLogeadoId();
        String profesionalMedicoId = String.valueOf(
                usuarioService.obtenerIdProfesionalMedicoAPartirDeIdDelUsuario(idUsuarioProfesionalMedico)
        );
        InformeData informe = informeService.encontrarInformeFullGraphPorId(informeId);

        List <DocumentoData> documentos = documentoService.obtenerDocumentosAsociadosAUnInforme(informeId);

        boolean noHayDocumentos = false;

        if (documentos.isEmpty()) {
            noHayDocumentos = true;
        }

        model.addAttribute("profesionalMedicoAutenticadoId", profesionalMedicoId);
        model.addAttribute("informeId", informeId );
        model.addAttribute("informe", informe);
        model.addAttribute("documentos", documentos);
        model.addAttribute("noHayDocumentos", noHayDocumentos);
        return "profesional_medico/ver-detalles-informe-paciente-que-ha-desautorizado";
    }

    @PostMapping("/api/profesional-medico/pacientes/informes/{informeId}/subir-documentos")
    public String subirDocumentos(@PathVariable(value="informeId") Long informeId,
                                    @RequestParam("documentos") MultipartFile[] documentos,
                                             Model model)  {
        for (MultipartFile documento : documentos) {
            try {
                String nombreDocumento = documento.getOriginalFilename();
                String tipoArchivo = documento.getContentType() != null ? documento.getContentType() : "application/octet-stream";
                Long tamanyo = documento.getSize();

                DocumentoData documentoCreado = documentoService.crearNuevoDocumentoVersionDos(
                        informeId,
                        nombreDocumento,
                        tipoArchivo,
                        tamanyo
                );

                String s3Key = documentoCreado.getS3_key();

                s3VitalSanityService.subirFichero(s3Key, documento);

            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "Error al subir el documento: " + documento.getOriginalFilename());
            }

        }

        return "redirect:/api/profesional-medico/pacientes/informes/"
                + informeId
                + "/ver-detalles";

    }

    @GetMapping("/api/profesional-medico/solicitudes-autorizacion-pendientes")
    public String verSolicitudesAutorizacionPendientes(Model model) {

        Long profesionalMedicoUsuarioId = getUsuarioLogeadoId();

        ProfesionalMedicoData profesionalMedicoData = profesionalMedicoService.encontrarPorIdUsuario(profesionalMedicoUsuarioId);

        Long profesionalId = Long.parseLong(profesionalMedicoData.getId());
        boolean noHaySolicitudes = false;

        List<SolicitudAutorizacionData> solicitudesAutorizacion = profesionalMedicoService.obtenerSolicitudesAutorizacionPendientes(profesionalId);
        if (solicitudesAutorizacion.isEmpty()) {
            noHaySolicitudes = true;
        }
        model.addAttribute("solicitudesAutorizacion", solicitudesAutorizacion);
        model.addAttribute("noHaySolicitudes", noHaySolicitudes);
        return "profesional_medico/ver-solicitudes-autorizacion-pendientes";
    }

}



