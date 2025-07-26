package info.jtrac.ui.item;

import info.jtrac.domain.Item;
import info.jtrac.domain.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/web/item")
public class ItemController {

    @GetMapping("/search")
    public String showSearchForm(
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) String spaceId,
            @RequestParam(required = false) String detail,
            Model model) {
        System.out.println("Searching for: " + summary);

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


        model.addAttribute("items", mockItems);
        ItemSearchForm attributeValue = new ItemSearchForm();
        attributeValue.setDetail(detail);
        attributeValue.setSummary(summary);

        model.addAttribute("spaceId", spaceId);
        model.addAttribute("itemSearchForm", attributeValue);
        return "item-search";
    }

}
