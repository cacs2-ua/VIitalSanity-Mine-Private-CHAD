package vitalsanity.service.general_user;


import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotNull;
import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import vitalsanity.dto.general_user.ParametroData;
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
public class ParametroService {

    @Autowired
    private ParametroRepository parametroRepository;

    @Transactional(readOnly = true)
    public String encontrarValorPorNombre(String nombre) {
        Parametro parametro = parametroRepository.findByNombre(nombre).orElseThrow(() -> new EntityNotFoundException("Parámetro no encontrado"));
        return parametro.getValor();
    }
}
