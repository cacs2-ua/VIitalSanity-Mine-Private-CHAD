package vitalsanity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vitalsanity.model.Paciente;
import vitalsanity.model.ProfesionalMedico;
import vitalsanity.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    Optional<Paciente> findByUsuarioId(Long usuarioId);

    Optional<Paciente> findByUsuarioNifNie(String nifNie);

    Optional<Paciente> findBySolicitudesAutorizacion_Id(Long id);

    List<Paciente> findByProfesionalesMedicosAutorizados_IdOrderByIdAsc(Long profesionalMedicoId);

    List<Paciente> findByProfesionalesMedicosDesautorizados_IdOrderByIdAsc(Long profesionalMedicoId);



}
