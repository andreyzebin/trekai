package info.jtrac.web;

import info.jtrac.domain.User;
import info.jtrac.service.JtracService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WebController {

    private final JtracService jtracService;

    @Autowired
    public WebController(JtracService jtracService) {
        this.jtracService = jtracService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("users", jtracService.findAllUsers());
        return "index";
    }

    @GetMapping("/users/{id}")
    public String userView(@PathVariable long id, Model model) {
        User user = jtracService.findUserByLoginName(String.valueOf(id));
        model.addAttribute("user", user);
        return "user-view";
    }

    @GetMapping("/users/new")
    public String userForm(Model model) {
        model.addAttribute("user", new User());
        return "user-form";
    }

    @PostMapping("/users")
    public String userSubmit(@ModelAttribute User user) {
        jtracService.saveUser(user);
        return "redirect:/";
    }
}
