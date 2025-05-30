package vitalsanity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vitalsanity.model.Informe;

import java.util.List;
import java.util.Optional;

public interface InformeRepository extends JpaRepository<Informe, Long> {

    boolean existsByUuid(String uuid);

    boolean existsByIdentificadorPublico(String identificadorPublico);

    Optional<Informe> findByUuid(String uuid);

    /**
     * Anulado el findById por defecto para que cargue:
     * - Informe.paciente + paciente.usuario
     * - Informe.profesionalMedico + profesionalMedico.usuario + profesionalMedico.especialidadMedica
     *   + profesionalMedico.centroMedico + centroMedico.usuario
     */
    @Query("""
        SELECT DISTINCT i
          FROM Informe i
          JOIN FETCH i.paciente p
          JOIN FETCH p.usuario pu
          JOIN FETCH i.profesionalMedico pr
          JOIN FETCH pr.usuario pru
          JOIN FETCH pr.especialidadMedica em
          JOIN FETCH pr.centroMedico cm
          JOIN FETCH cm.usuario cmu
         WHERE i.id = :id
        """)
    Optional<Informe> findWithEverythingById(@Param("id") Long id);


    /**
     * Devuelve todos los informes del paciente cuyo id es :pacienteId
     * con todas las relaciones precargadas:
     * Paciente → Usuario,
     * ProfesionalMedico → Usuario, EspecialidadMedica, CentroMedico → Usuario.
     */
    @Query("""
    SELECT DISTINCT i
      FROM Informe i
      JOIN FETCH i.paciente p
      JOIN FETCH p.usuario pu
      JOIN FETCH i.profesionalMedico pr
      JOIN FETCH pr.usuario pru
      JOIN FETCH pr.especialidadMedica em
      JOIN FETCH pr.centroMedico cm
      JOIN FETCH cm.usuario cmu
     WHERE p.id = :pacienteId
     ORDER BY i.id DESC
    """)
    List<Informe> findAllByPacienteId(
            @Param("pacienteId") Long pacienteId
    );

}
