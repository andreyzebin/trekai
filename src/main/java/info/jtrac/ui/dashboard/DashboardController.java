package info.jtrac.ui.dashboard;

import info.jtrac.domain.CountsHolder;
import info.jtrac.domain.User;
import info.jtrac.service.JtracService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class DashboardController {

    private final JtracService jtracService;

    public DashboardController(JtracService jtracService) {
        this.jtracService = jtracService;
    }

    @GetMapping("/web/dashboard")
    public String getDashboard(Model model, Principal principal) {
        User user = jtracService.findUserByLoginName(principal.getName());
        CountsHolder countsHolder = jtracService.loadCountsForUser(user);

        model.addAttribute("user", user);
        model.addAttribute("countsHolder", countsHolder);
        model.addAttribute("spaceRoles", user.getSpaceRoles());

        return "dashboard";
    }
}
