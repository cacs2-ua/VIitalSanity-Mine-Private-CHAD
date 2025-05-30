package vitalsanity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vitalsanity.model.ProfesionalMedico;
import vitalsanity.model.SolicitudAutorizacion;

import java.util.List;
import java.util.Optional;

public interface SolicitudAutorizacionRepository extends JpaRepository<SolicitudAutorizacion, Long> {
    Optional<SolicitudAutorizacion> findTopByProfesionalMedicoIdOrderByIdDesc(Long profesionalMedicoId);

    Optional<SolicitudAutorizacion> findTopByProfesionalMedicoIdAndPacienteIdOrderByIdDesc(Long profesionalMedicoId, Long pacienteId);

    Optional<SolicitudAutorizacion> findTopByProfesionalMedicoIdAndPacienteIdAndDenegadaFalse(Long profesionalMedicoId, Long pacienteId);

    List<SolicitudAutorizacion> findByPacienteIdAndDenegadaFalse(Long pacienteId);

    /**
     * Recupera todas las solicitudes del paciente (por su id) con denegada=false, firmada=true y cofirmada=false,
     * precargando Paciente, Usuario del Paciente, Profesional, Usuario del Profesional, Especialidad del Profesional,
     * Centro Médico del Profesional y Usuario del Centro Médico, ordenadas por fechaFirma descendente.
     */
    @Query("""
      SELECT sa
        FROM SolicitudAutorizacion sa
        JOIN FETCH sa.paciente p
        JOIN FETCH p.usuario pu
        JOIN FETCH sa.profesionalMedico pm
        JOIN FETCH pm.usuario pmu
        JOIN FETCH pm.especialidadMedica em
        JOIN FETCH pm.centroMedico cm
        JOIN FETCH cm.usuario cmu
       WHERE p.id = :pacienteId
         AND sa.denegada = false
         AND sa.firmada = true
         AND sa.cofirmada = false
       ORDER BY sa.fechaFirma DESC
      """)
    List<SolicitudAutorizacion> findByPacienteIdAndDenegadaFalseAndFirmadaTrueAndCofirmadaFalseOrderByFechaFirmaDesc(
            @Param("pacienteId") Long pacienteId
    );

    /**
     * Recupera todas las solicitudes de autorización de un profesional médico (por su id)
     * con denegada = false, firmada = true, cofirmada = false,
     * precargando Paciente, Usuario del Paciente, Profesional, Usuario del Profesional,
     * Especialidad del Profesional, Centro Médico del Profesional y Usuario del Centro Médico.
     * Ordenadas por fechaFirma descendente.
     */
    @Query("""
      SELECT sa
        FROM SolicitudAutorizacion sa
        JOIN FETCH sa.paciente p
        JOIN FETCH p.usuario pu
        JOIN FETCH sa.profesionalMedico pm
        JOIN FETCH pm.usuario pmu
        JOIN FETCH pm.especialidadMedica em
        JOIN FETCH pm.centroMedico cm
        JOIN FETCH cm.usuario cmu
       WHERE pm.id = :profesionalMedicoId
         AND sa.denegada = false
         AND sa.firmada = true
         AND sa.cofirmada = false
       ORDER BY sa.fechaFirma DESC
      """)
    List<SolicitudAutorizacion> findAllWithAllFetchByProfesionalMedicoIdAndDenegadaFalseAndFirmadaTrueAndCofirmadaFalseOrderByFechaFirmaDesc(
            @Param("profesionalMedicoId") Long profesionalMedicoId
    );

}
