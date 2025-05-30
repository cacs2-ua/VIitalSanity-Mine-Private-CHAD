package vitalsanity.service.documento;

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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.transaction.annotation.Transactional;
import vitalsanity.service.utils.aws.S3VitalSanityService;


@Service
public class DocumentoService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private DocumentoRepository documentoRepository;

    @Autowired
    private InformeRepository informeRepository;

    @Autowired
    private S3VitalSanityService s3VitalSanityService;

    @Transactional
    public DocumentoData crearNuevoDocumento(
            Long informeId,
            String uuid,
            String nombre,
            String s3Key,
            String tipoArchivo,
            Long tamanyo) {
        Documento documento = new Documento();

        Informe informe = informeRepository.findById(informeId).orElse(null);

        documento.setInforme(informe);
        documento.setUuid(uuid);
        documento.setNombre(nombre);
        documento.setS3_key(s3Key);
        documento.setTipo_archivo(tipoArchivo);
        documento.setTamanyo(tamanyo);
        documento.setFechaSubida(LocalDateTime.now());

        documentoRepository.save(documento);

        return modelMapper.map(documento, DocumentoData.class);
    }

    //Este otro método generará el uuid de forma interna. No sobreescribo el método anterior para evitar confundirlos por sus nombres a la hora de utilizar cualquiera de los dos métodos
    @Transactional
    public DocumentoData crearNuevoDocumentoVersionDos(
            Long informeId,
            String nombre,
            String tipoArchivo,
            Long tamanyo) {

        String uuid = "";
        do {
            uuid = UUID.randomUUID().toString();
        } while(documentoRepository.existsByUuid(uuid));

        Documento documento = new Documento();

        Informe informe = informeRepository.findById(informeId).orElse(null);

        documento.setInforme(informe);

        documento.setUuid(uuid);
        documento.setNombre(nombre);

        // 2) Calcular extensión real
        String extension = "";
        int idx = nombre != null ? nombre.lastIndexOf('.') : -1;
        if (idx > 0) {
            extension = nombre.substring(idx).toLowerCase();
        } else {
            // Fallback según MIME
            switch (tipoArchivo) {
                case "application/pdf": extension = ".pdf"; break;
                case "image/jpeg":     extension = ".jpg"; break;
                case "image/png":      extension = ".png"; break;
                default:               extension = "";     break;
            }
        }

        if (extension.isEmpty()) {
            throw new IllegalArgumentException("Extensión de fichero no soportada: " + nombre);
        }

        // 3) Montar la key con extensión correcta
        String s3Key = "informes/documentos/"
                + uuid
                + "_"
                + System.currentTimeMillis()
                + extension;

        documento.setS3_key(s3Key);
        documento.setTipo_archivo(tipoArchivo);
        documento.setTamanyo(tamanyo);
        documento.setFechaSubida(LocalDateTime.now());

        documentoRepository.save(documento);

        return modelMapper.map(documento, DocumentoData.class);
    }

    @Transactional(readOnly = true)
    public  DocumentoData encontrarPorUuid(String uuid) {
        Documento documento = documentoRepository.findByUuid(uuid).orElse(null);
        return modelMapper.map(documento, DocumentoData.class);
    }

    @Transactional(readOnly = true)
    public  List<DocumentoData> obtenerDocumentosAsociadosAUnInforme(Long informeId) {
        return documentoRepository.findAllByInformeIdOrderByIdAsc(informeId).stream()
                .filter(documento -> documento.getUuid() != null
                        && !documento.getUuid().equals(documento.getNombre()))
                .map(documento -> {
                    DocumentoData dto = modelMapper.map(documento, DocumentoData.class);
                    String urlPrefirmada = s3VitalSanityService.generarUrlPrefirmada(
                            dto.getS3_key(),
                            Duration.ofMinutes(33)
                    );
                    dto.setUrlPrefirmada(urlPrefirmada);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}
