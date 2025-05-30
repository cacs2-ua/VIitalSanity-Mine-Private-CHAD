package vitalsanity.controller.global;


import vitalsanity.authentication.ManagerUserSession;
import vitalsanity.dto.general_user.UsuarioData;
import vitalsanity.service.general_user.ParametroService;
import vitalsanity.service.general_user.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import vitalsanity.service.utils.aws.S3VitalSanityService;

import java.time.Duration;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private ManagerUserSession managerUserSession;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ParametroService parametroService;

    @Autowired
    private S3VitalSanityService s3VitalSanityService;

    @ModelAttribute
    public void addAttributes(Model model) {
        Long idUsuarioLogeado = managerUserSession.usuarioLogeado();
        if (idUsuarioLogeado != null) {
            UsuarioData usuario = usuarioService.findById(idUsuarioLogeado);
            model.addAttribute("usuario", usuario);
        }

        String s3KeyPoliticaPrivacidad = parametroService.encontrarValorPorNombre("politica-de-privacidad");

        String urlPrefirmadaPoliticaPrivacidad = s3VitalSanityService.generarUrlPrefirmada(
                s3KeyPoliticaPrivacidad,
                Duration.ofMinutes(33));
        model.addAttribute("urlPrefirmadaPoliticaPrivacidad", urlPrefirmadaPoliticaPrivacidad);
    }
}