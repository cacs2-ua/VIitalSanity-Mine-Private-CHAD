package vitalsanity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vitalsanity.model.Documento;

import java.util.List;
import java.util.Optional;

public interface DocumentoRepository extends JpaRepository<Documento, Long> {

    List<Documento> findAllBySolicitudAutorizacionId(Long solicitudAutorizacionId);

    Optional<Documento> findByUuid(String uuid);

    boolean existsByUuid(String uuid);

    List<Documento> findAllByInformeIdOrderByIdAsc(Long solicitudAutorizacionId);
}
