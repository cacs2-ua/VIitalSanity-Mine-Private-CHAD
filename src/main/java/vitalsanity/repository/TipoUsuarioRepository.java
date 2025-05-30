package vitalsanity.repository;

import vitalsanity.model.TipoUsuario;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoUsuarioRepository extends JpaRepository<TipoUsuario, Long> {
    Optional<TipoUsuario> findByTipo(String nombre);
}
