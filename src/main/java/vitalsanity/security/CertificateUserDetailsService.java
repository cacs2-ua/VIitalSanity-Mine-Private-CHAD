package vitalsanity.security;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import vitalsanity.model.Usuario;
import vitalsanity.repository.UsuarioRepository;

@Service
public class CertificateUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String nif) throws UsernameNotFoundException {
        List<Usuario> usuarios = usuarioRepository.findByNifNie(nif);
        if (usuarios == null || usuarios.isEmpty()) {
            throw new UsernameNotFoundException("Usuario no encontrado para NIF: " + nif);
        }
        Usuario usuario = usuarios.get(0);
        return new User(nif, "", AuthorityUtils.createAuthorityList("ROLE_USER"));
    }
}