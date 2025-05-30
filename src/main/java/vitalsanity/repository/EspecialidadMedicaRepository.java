package vitalsanity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vitalsanity.model.*;

import java.util.Optional;

public interface EspecialidadMedicaRepository extends JpaRepository<EspecialidadMedica, Long> {
    boolean existsByNombre(String nombre);
    Optional<EspecialidadMedica> findByNombre(String nombre);
}
