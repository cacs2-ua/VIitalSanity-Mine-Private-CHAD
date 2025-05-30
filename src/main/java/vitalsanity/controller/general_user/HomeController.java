package vitalsanity.controller.general_user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vitalsanity.authentication.ManagerUserSession;

@Controller
public class HomeController {
    @Autowired
    private ManagerUserSession managerUserSession;

    @GetMapping("/api/general/home")
    public String home(Model model) {
        return "general_user/home";
    }
}
