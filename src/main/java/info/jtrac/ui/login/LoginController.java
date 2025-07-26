package info.jtrac.ui.login;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/web/login")
    public String login(Model model) {
        model.addAttribute("loginPage", true);
        return "login";
    }
}
