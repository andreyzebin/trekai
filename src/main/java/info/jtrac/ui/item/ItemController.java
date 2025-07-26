package info.jtrac.ui.item;

import info.jtrac.domain.Item;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.service.JtracService;
import info.jtrac.web.api.dto.ItemCreateDto;
import info.jtrac.web.api.dto.ItemUpdateDto;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/web/item")
public class ItemController {

    private final JtracService jtracService;

    public ItemController(JtracService jtracService) {
        this.jtracService = jtracService;
    }

    public static Long longOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid long value: '" + value + "'");
        }
    }

    public static Integer intOrNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Invalid long value: '" + value + "'");
        }
    }


    @GetMapping("/search")
    public String showSearchFormAndResults(@ModelAttribute ItemSearchForm itemSearchForm,
                                           @RequestParam(required = false) String search,
                                           @RequestParam(required = false) String spaceId,
                                           Principal principal, Model model) {

        model.addAttribute("users", jtracService.findAllUsers());


        List<Item> items = jtracService.findItems(
                longOrNull(spaceId),
                itemSearchForm.getSummary(),
                longOrNull(itemSearchForm.getAssignedToId()),
                intOrNull(itemSearchForm.getStatus()));
        model.addAttribute("items", items);


        model.addAttribute("itemSearchForm", itemSearchForm);
        return "item-search";
    }

    @GetMapping("/{id}")
    public String showItemView(@PathVariable long id, Model model) {
        Item item = jtracService.findItemById(id);
        if (item == null) {
            return "redirect:/web/dashboard";
        }
        model.addAttribute("item", item);
        model.addAttribute("itemUpdateForm", new ItemUpdateDto());
        return "item-view";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model, Principal principal) {
        User user = jtracService.findUserByLoginName(principal.getName());
        model.addAttribute("itemCreateForm", new ItemCreateDto());
        model.addAttribute("userSpaces", user.getSpaces());
        model.addAttribute("assignableUsers", List.of(user));
        return "item-form";
    }

    @PostMapping("/new")
    public String createItem(@ModelAttribute("itemCreateForm") ItemCreateDto itemCreateDto, Principal principal) {
        User user = jtracService.findUserByLoginName(principal.getName());
        Space space = jtracService.loadSpace(itemCreateDto.getSpaceId());
        User assignedTo = jtracService.loadUser(itemCreateDto.getAssignedToId());

        Item item = new Item();
        item.setSpace(space);
        item.setSummary(itemCreateDto.getSummary());
        item.setDetail(itemCreateDto.getDetail());
        item.setAssignedTo(assignedTo);
        item.setLoggedBy(user);

        Item savedItem = jtracService.storeItem(item, null);

        return "redirect:/web/item/" + savedItem.getId();
    }

    @PostMapping("/{id}")
    public String updateItem(@PathVariable long id, @ModelAttribute("itemUpdateForm") ItemUpdateDto itemUpdateDto, Principal principal) {
        Item item = jtracService.findItemById(id);
        if (item == null) {
            return "redirect:/web/dashboard";
        }

        User user = jtracService.findUserByLoginName(principal.getName());

        if (itemUpdateDto.getComment() != null && !itemUpdateDto.getComment().isEmpty()) {
            item.setEditReason(itemUpdateDto.getComment());
        }
        if (itemUpdateDto.getStatus() != null) {
            item.setStatus(itemUpdateDto.getStatus());
        }
        if (itemUpdateDto.getAssignedToId() != null) {
            User assignedTo = jtracService.loadUser(itemUpdateDto.getAssignedToId());
            if (assignedTo != null) {
                item.setAssignedTo(assignedTo);
            }
        }

        jtracService.updateItem(item, user);

        return "redirect:/web/item/" + id;
    }
}
