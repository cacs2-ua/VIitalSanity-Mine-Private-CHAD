package vitalsanity.controller.debug;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class DebugTemplatesCheck {

    @GetMapping("/api/profesional-medico/profesional-crear-nuevo-informe/check")
    public String profesionalCrearNuevoInformeCheck(Model model) {
        return "profesional-medico-crear-nuevo-informe";
    }

    @GetMapping("/api/profesional-medico/profesional-informes-del-paciente/check")
    public String profesionalVerInformesDelPaciente(Model model) {
        return "profesional-medico-crear-nuevo-informe";
    }

}



