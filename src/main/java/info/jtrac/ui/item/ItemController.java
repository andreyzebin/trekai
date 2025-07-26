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
import java.util.ArrayList;
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
                                           @RequestParam(required = false) String spacePrefix,
                                           Principal principal, Model model) {

        model.addAttribute("users", jtracService.findAllUsers());
        model.addAttribute("spacePrefix", spacePrefix);


        Space space = jtracService.findSpaceByPrefixCode(spacePrefix);
        List<Item> items = jtracService.findItems(
                Optional.ofNullable(space).map(Space::getId).orElse(null),
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
    public String showCreateForm(@RequestParam(required = false) String spacePrefix, Model model, Principal principal) {
        User user = jtracService.findUserByLoginName(principal.getName());
        ItemCreateDto itemCreateDto = new ItemCreateDto();
        
        List<Space> userSpaces = new ArrayList<>(user.getSpaces());
        model.addAttribute("userSpaces", userSpaces);

        Space space = null;
        if (spacePrefix != null && !spacePrefix.isBlank()) {
            space = jtracService.findSpaceByPrefixCode(spacePrefix);
        } else if (!userSpaces.isEmpty()) {
            space = userSpaces.get(0); // Default to the first space
        }

        if (space != null) {
            itemCreateDto.setSpacePrefix(space.getPrefixCode());
            model.addAttribute("space", space);
            model.addAttribute("customFields", space.getMetadata().getFieldList());
        } else {
            model.addAttribute("customFields", Collections.emptyList());
        }
        
        model.addAttribute("itemCreateForm", itemCreateDto);
        model.addAttribute("assignableUsers", jtracService.findAllUsers());
        return "item-form";
    }

    @PostMapping("/new")
    public String createItem(@ModelAttribute("itemCreateForm") ItemCreateDto itemCreateDto, Principal principal) {
        User user = jtracService.findUserByLoginName(principal.getName());
        Space space = jtracService.loadSpace(itemCreateDto.getSpaceId());
        Item item = new Item();
        if (itemCreateDto.getAssignedToId() != null) {
            User assignedTo = jtracService.loadUser(itemCreateDto.getAssignedToId());
            item.setAssignedTo(assignedTo);
        }
        item.setSpace(space);
        item.setSummary(itemCreateDto.getSummary());
        item.setDetail(itemCreateDto.getDetail());
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
