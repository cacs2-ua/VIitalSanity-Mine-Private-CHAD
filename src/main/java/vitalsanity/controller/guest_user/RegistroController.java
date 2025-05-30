package vitalsanity.controller.guest_user;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vitalsanity.authentication.ManagerUserSession;
import vitalsanity.dto.guest_user.RegistroData;
import vitalsanity.dto.general_user.UsuarioData;
import vitalsanity.service.utils.EmailService;
import vitalsanity.service.general_user.UsuarioService;

@Controller
public class RegistroController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ManagerUserSession managerUserSession;

    @Autowired
    private EmailService emailService;

    private Long getUsuarioLogeadoId() {
        return managerUserSession.usuarioLogeado();
    }

    @GetMapping("/registro")
    public String registroForm(Model model) {
        if (getUsuarioLogeadoId() != null) {
            return "redirect:/api/general/home";
        }
        model.addAttribute("registroData", new RegistroData());
        return "guest_user/registro-form"; // Plantilla adaptada con thymeleaf
    }

    @PostMapping("/registro")
    public String registroSubmit(@ModelAttribute RegistroData registroData, HttpSession session, Model model) {
        if (registroData.getContrasenya() == null || registroData.getContrasenya().length() < 8) {
            model.addAttribute("error", "La contrasenya debe tener al menos 8 caracteres");
            return "guest_user/registro-form";
        }

        // Validar que las contrasenyas coincidan
        if (!registroData.getContrasenya().equals(registroData.getConfirmarContrasenya())) {
            model.addAttribute("error", "Las contrasenyas no coinciden");
            return "guest_user/registro-form";
        }
        // Generar codigo de confirmacion (por ejemplo, 6 digitos)
        String codigoConfirmacion = String.valueOf((int)(Math.random() * 900000) + 100000);
        // Almacenar datos de registro y codigo en la sesion
        session.setAttribute("registroData", registroData);
        session.setAttribute("codigoConfirmacion", codigoConfirmacion);

        new Thread(() -> {
            emailService.send(registroData.getEmail(), "Registration Confirmation Code",
                    "Your registration confirmation code is: " + codigoConfirmacion);
        }).start();

        // Redirigir al formulario de codigo de confirmacion
        return "redirect:/registro/codigo-confirmacion-form";
    }

    @GetMapping("/registro/codigo-confirmacion-form")
    public String registroCodigoConfirmacionForm(Model model) {
        return "guest_user/registro-codigo-confirmacion-form"; // Plantilla adaptada con thymeleaf
    }

    @PostMapping("/registro/codigo-confirmacion-form")
    public String codigoConfirmacionSubmit(@RequestParam("codigo") String codigoIngresado,
                                           HttpSession session, Model model) {
        String codigoGuardado = (String) session.getAttribute("codigoConfirmacion");
        RegistroData registroData = (RegistroData) session.getAttribute("registroData");

        if (codigoGuardado == null || registroData == null) {
            model.addAttribute("error", "Sesion expirada. Por favor, reinicie el proceso de registro.");
            return "guest_user/registro-form";
        }

        if (!codigoGuardado.equals(codigoIngresado)) {
            model.addAttribute("error", "Codigo de confirmacion incorrecto");
            return "guest_user/registro-codigo-confirmacion-form";
        }

        // Si el codigo es correcto, completar el registro
        UsuarioData usuarioData = usuarioService.registrarPaciente(registroData);

        // Limpiar datos de la sesion
        session.removeAttribute("registroData");
        session.removeAttribute("codigoConfirmacion");

        // Loguear usuario automaticamente
        managerUserSession.logearUsuario(usuarioData.getId());

        if (usuarioData.getPrimerAcceso()){
            return "redirect:/api/paciente/" + usuarioData.getId() + "/datos-residencia";
        }

        return "redirect:/api/paciente/" + usuarioData.getId() + "/informes";
    }
}
