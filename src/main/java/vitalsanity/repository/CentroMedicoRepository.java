package vitalsanity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vitalsanity.model.CentroMedico;

import java.util.List;
import java.util.Optional;

public interface CentroMedicoRepository extends JpaRepository<CentroMedico, Long> {
    @Query("select cm from CentroMedico cm left join fetch cm.profesionalesMedicos where cm.usuario.id = :usuarioId")
    Optional<CentroMedico> findByUsuarioId(@Param("usuarioId") Long usuarioId);

}
