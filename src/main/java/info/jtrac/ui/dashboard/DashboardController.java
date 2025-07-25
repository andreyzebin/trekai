package info.jtrac.ui.dashboard;

import info.jtrac.domain.Counts;
import info.jtrac.domain.CountsHolder;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        // Mock data for now, will be replaced with service calls
        User user = new User();
        user.setId(1L);
        user.setName("Test User");

        List<UserSpaceRole> spaceRoles = new ArrayList<>();
        Space space1 = new Space();
        space1.setId(1L);
        space1.setName("Test Space 1");
        spaceRoles.add(new UserSpaceRole(user, space1, "ROLE_ADMIN"));

        Space space2 = new Space();
        space2.setId(2L);
        space2.setName("Test Space 2");
        spaceRoles.add(new UserSpaceRole(user, space2, "ROLE_USER"));

        CountsHolder countsHolder = new CountsHolder();
        // space 1 counts
        countsHolder.addLoggedByMe(1L, 5);
        countsHolder.addAssignedToMe(1L, 2);
        countsHolder.addTotal(1L, 7);

        // space 2 counts
        countsHolder.addLoggedByMe(2L, 5);
        countsHolder.addAssignedToMe(2L, 3);
        countsHolder.addTotal(2L, 8);


        model.addAttribute("user", user);
        model.addAttribute("spaceRoles", spaceRoles);
        model.addAttribute("countsHolder", countsHolder);

        return "dashboard";
    }
}
