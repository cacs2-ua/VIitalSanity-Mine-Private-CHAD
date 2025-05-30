package vitalsanity.controller.debug;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class MiddlewareDebugController {

    @GetMapping("/api/auth/check")
    public String authCheck(Model model) {
        return "debug/auth-check";
    }

    @GetMapping("/api/admin/check")
    public String adminCheck(Model model) {
        return "debug/admin-check";
    }

    @GetMapping("/api/centro-medico/check")
    public String centroMedicoCheck(Model model) {
        return "debug/centro-medico-check";
    }

    @GetMapping("/api/profesional-medico/check")
    public String tecnicoOrAdminCheck(Model model) {
        return "debug/profesional-medico-check";
    }

    @GetMapping("/api/paciente/check")
    public String generalCheck(Model model) {
        return "debug/paciente-check";
    }
}


