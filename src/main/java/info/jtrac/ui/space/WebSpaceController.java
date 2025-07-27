package info.jtrac.ui.space;

import info.jtrac.domain.Item;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.service.JtracService;
import info.jtrac.web.api.dto.ItemCreateDto;
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

@Controller
@RequestMapping("/web/spaces")
public class WebSpaceController {
    private final JtracService jtracService;

    public WebSpaceController(JtracService jtracService) {
        this.jtracService = jtracService;
    }

    @GetMapping("/{spacePrefix}/create")
    public String showCreateForm(@PathVariable String spacePrefix, Model model, Principal principal) {
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
            itemCreateDto.setSpaceId(space.getId());
            model.addAttribute("space", space);
            model.addAttribute("spaceId", space.getId());
            model.addAttribute("customFields", space.getMetadata().getOrderedFields());
        } else {
            model.addAttribute("customFields", Collections.emptyList());
        }

        model.addAttribute("currentPath", "/web/item/new");
        model.addAttribute("itemCreateForm", itemCreateDto);
        model.addAttribute("assignableUsers", jtracService.findAllUsers());
        return "item-form";
    }

    @PostMapping("/{spacePrefix}/create")
    public String createItem(@RequestParam(required = false) Long spaceId, @ModelAttribute("itemCreateForm") ItemCreateDto itemCreateDto, Principal principal) {
        User user = jtracService.findUserByLoginName(principal.getName());
        Space space = jtracService.findSpaceByPrefixCode(itemCreateDto.getSpacePrefix());
        Item item = new Item();
        if (itemCreateDto.getAssignedToId() != null) {
            User assignedTo = jtracService.loadUser(itemCreateDto.getAssignedToId());
            item.setAssignedTo(assignedTo);
        }
        item.setSpace(space);
        item.setSummary(itemCreateDto.getSummary());
        item.setDetail(itemCreateDto.getDetail());
        item.setLoggedBy(user);

        if (itemCreateDto.getCustomFields() != null) {
            itemCreateDto.getCustomFields().keySet()
                    .forEach((code) -> {
                        if (!space.getMetadata().getFields().containsKey(code)) {
                            throw new IllegalArgumentException("Unknown field: " + code);
                        }
                    });
            itemCreateDto.getCustomFields().forEach(item::setValue);
        }

        Item savedItem = jtracService.storeItem(item, null);

        return "redirect:/web/item/" + savedItem.getId();
    }
}
