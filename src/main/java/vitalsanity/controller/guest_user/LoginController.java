package vitalsanity.controller.guest_user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import vitalsanity.authentication.ManagerUserSession;
import vitalsanity.dto.guest_user.LoginData;
import vitalsanity.dto.general_user.UsuarioData;
import vitalsanity.service.general_user.UsuarioService;
import vitalsanity.service.utils.EmailService;

@Controller
public class LoginController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    @Autowired
    private EmailService emailService;

    private Long getUsuarioLogeadoId() {
        return managerUserSession.usuarioLogeado();
    }

    @GetMapping("/")
    public String home(Model model) {
        return "redirect:/api/general/home";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        if (getUsuarioLogeadoId() != null) {
            return "redirect:/api/general/home";
        }
        model.addAttribute("loginData", new LoginData());
        return "guest_user/login-form"; // Plantilla adaptada con thymeleaf
    }

    // Mapping para /login/certificate (fallback cuando no se selecciona certificado)
    @GetMapping("/login/certificate")
    public String certificateLoginFallback(Model model) {
        model.addAttribute("error", "No se selecciono certificado. Por favor, inicie sesion manualmente.");
        model.addAttribute("loginData", new LoginData());
        return "guest_user/login-form";
    }

    @PostMapping("/login")
    public String loginSubmit(@ModelAttribute LoginData loginData, Model model) {

        System.out.println("Intento de inicio de sesión: " + "a fecha de " + java.time.LocalDateTime.now());


        new Thread(() -> {
            emailService.send("perjv92@gmail.com", "buenas tardes", "buenas noches");
        }).start();



        Long idUsuario = getUsuarioLogeadoId();

        if (loginData.getContrasenya() == null || loginData.getContrasenya().length() < 8) {
            model.addAttribute("error", "Ha habido algún error al iniciar sesion");
            return "guest_user/login-form";
        }

        UsuarioService.LoginStatus loginStatus = usuarioService.login(
                loginData.getEmail(),
                loginData.getContrasenya()
        );

        if (loginStatus == UsuarioService.LoginStatus.LOGIN_OK) {
            System.out.println("Inicio de sesión completado con éxito");
            UsuarioData usuario = usuarioService.findByEmail(loginData.getEmail());
            managerUserSession.logearUsuario(usuario.getId());

            if (usuario.getTipoId() == 1){
                return "redirect:/api/admin/registro-centro-medico";
            }
            if (usuario.getTipoId() == 2){
                if (usuario.getPrimerAcceso()){
                    return "redirect:/api/general/usuarios/" + usuario.getId() + "/contrasenya";
                }

                return "redirect:/api/centro-medico/profesionales-medicos";
            }
            if (usuario.getTipoId() == 3){
                if (usuario.getPrimerAcceso()){
                    return "redirect:/api/general/usuarios/" + usuario.getId() + "/contrasenya";
                }

                return "redirect:/api/profesional-medico/buscar-paciente";
            }
            if (usuario.getTipoId() == 4){
                if (usuario.getPrimerAcceso()){
                    return "redirect:/api/paciente/" + usuario.getId() + "/datos-residencia";
                }

                return "redirect:/api/paciente/informes";
            }
            return "redirect:/api/general/home";

        } else if (loginStatus == UsuarioService.LoginStatus.USER_DISABLED) {
            System.out.println("No puedes iniciar sesión. Tu usuario esta deshabilitado");
            model.addAttribute("error", "No puedes iniciar sesión. Tu usuario esta deshabilitado");
            return "guest_user/login-form";
        } else {
            System.out.println("Ha ocurrido un error durante el inicio de sesión");
            model.addAttribute("error", "Ha ocurrido un error durante el inicio de sesión");
            return "guest_user/login-form";
        }
    }

    @GetMapping("/logout")
    public String logout() {
        managerUserSession.logout();
        return "redirect:/login";
    }
}