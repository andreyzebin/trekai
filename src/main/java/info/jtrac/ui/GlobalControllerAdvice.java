package info.jtrac.ui;

import info.jtrac.domain.User;
import info.jtrac.service.JtracService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;

@ControllerAdvice
public class GlobalControllerAdvice {
    private final JtracService jtracService;

    public GlobalControllerAdvice(JtracService jtracService) {
        this.jtracService = jtracService;
    }

    @ModelAttribute("currentUser")
    public User currentUser(Principal principal) {
        return principal != null ? jtracService.findUserByLoginName(principal.getName()) : null;
    }
}
