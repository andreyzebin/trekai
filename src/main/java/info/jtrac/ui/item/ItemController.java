package info.jtrac.ui.item;

import info.jtrac.domain.Item;
import info.jtrac.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/web/item")
public class ItemController {

    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("itemSearchForm", new ItemSearchForm());
        return "item-search";
    }

    @PostMapping("/search")
    public String handleSearch(@ModelAttribute ItemSearchForm itemSearchForm, RedirectAttributes redirectAttributes) {
        // Mock search logic
        System.out.println("Searching for: " + itemSearchForm);

        User mockUser = new User();
        mockUser.setName("Mock User");

        Item item1 = new Item();
        item1.setId(1L);
        item1.setSummary("First mock item");
        item1.setStatus(1);
        item1.setLoggedBy(mockUser);
        item1.setAssignedTo(mockUser);
        item1.setTimeStamp(Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));

        Item item2 = new Item();
        item2.setId(2L);
        item2.setSummary("Second mock item with detail");
        item2.setStatus(2);
        item2.setLoggedBy(mockUser);
        item2.setAssignedTo(null);
        item2.setTimeStamp(Date.from(LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()));

        List<Item> mockItems = List.of(item1, item2);

        redirectAttributes.addFlashAttribute("items", mockItems);
        redirectAttributes.addFlashAttribute("searchPerformed", true);

        return "redirect:/item/list";
    }


    @GetMapping("/list")
    public String showListPage(Model model) {
        // This ensures the model attributes are not lost after the redirect
        if (!model.containsAttribute("searchPerformed")) {
            model.addAttribute("items", Collections.emptyList());
            model.addAttribute("searchPerformed", false);
        }
        return "item-list";
    }
}
