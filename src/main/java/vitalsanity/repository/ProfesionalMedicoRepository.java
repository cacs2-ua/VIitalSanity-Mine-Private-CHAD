package vitalsanity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vitalsanity.model.Paciente;
import vitalsanity.model.ProfesionalMedico;
import vitalsanity.model.SolicitudAutorizacion;
import vitalsanity.model.Usuario;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProfesionalMedicoRepository extends JpaRepository<ProfesionalMedico, Long> {
    Optional<ProfesionalMedico> findByUsuarioId(Long usuarioId);

    Optional<ProfesionalMedico> findBySolicitudesAutorizacion_Id(Long id);

    boolean existsByNafAndCcc(String naf, String ccc);

    @Query("""
  SELECT DISTINCT pm
    FROM ProfesionalMedico pm
    JOIN FETCH pm.pacientesQueHanAutorizado p
    JOIN FETCH p.usuario pu
    JOIN FETCH pm.usuario pmu
    JOIN FETCH pm.especialidadMedica em
    JOIN FETCH pm.centroMedico cm
    JOIN FETCH cm.usuario cmu
   WHERE p.id = :pacienteId
   ORDER BY pm.id ASC
""")
    List<ProfesionalMedico> findByPacientesQueHanAutorizado_IdOrderByIdAsc(
            @Param("pacienteId") Long pacienteId
    );



    List<ProfesionalMedico> findByPacientesQueHanDesautorizado_IdOrderByIdAsc(Long pacienteId);



}





