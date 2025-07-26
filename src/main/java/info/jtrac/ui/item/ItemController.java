package info.jtrac.ui.item;

import info.jtrac.domain.Field;
import info.jtrac.domain.Item;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.service.JtracService;
import info.jtrac.web.api.dto.ItemCreateDto;
import info.jtrac.web.api.dto.ItemPatchDto;
import info.jtrac.web.api.dto.ItemUpdateDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        model.addAttribute("statuses", Map.of(1,"Open", 2, "Closed"));
        model.addAttribute("customFields", item.getSpace().getMetadata().getOrderedFields());
        model.addAttribute("itemUpdateForm", new ItemUpdateDto());
        model.addAttribute("users", jtracService.findAllUsers());
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
            itemCreateDto.setSpaceId(space.getId());
            model.addAttribute("space", space);
            model.addAttribute("spaceId", space.getId());
            model.addAttribute("customFields", space.getMetadata().getOrderedFields());
        } else {
            model.addAttribute("customFields", Collections.emptyList());
        }

        model.addAttribute("itemCreateForm", itemCreateDto);
        model.addAttribute("assignableUsers", jtracService.findAllUsers());
        return "item-form";
    }

    @PostMapping("/new")
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


    @PatchMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updateCustomField(
            @PathVariable Long id,
            @RequestBody ItemPatchDto itemDto,
            Principal principal
    ) {
        User user = jtracService.findUserByLoginName(principal.getName());
        Item item = jtracService.findItemById(id);

        // Проверка, что пользователь имеет доступ
        if (!user.getSpaces().contains(item.getSpace())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied to this item");
        }


        if (itemDto.getCustomFields() != null) {
            Map<String, String> customFields = new HashMap<>(itemDto.getCustomFields());

            // assignedToId
            if (itemDto.getCustomFields().containsKey("assignedToId")) {
                itemDto.setAssignedToId(Long.parseLong(itemDto.getCustomFields().get("assignedToId")));
                customFields.remove("assignedToId");
                itemDto.setCustomFields(customFields);
            }

            // status
            if (itemDto.getCustomFields().containsKey("status")) {
                itemDto.setStatus(Long.parseLong(itemDto.getCustomFields().get("status")));
                customFields.remove("status");
                itemDto.setCustomFields(customFields);
            }


            if (itemDto.getCustomFields().keySet().stream()
                    .anyMatch((code) -> !item.getSpace().getMetadata().getFields().containsKey(code))) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            itemDto.getCustomFields().forEach(item::setValue);
        }

        if (itemDto.getAssignedToId() != null) {
            User assignedTo = jtracService.loadUser(itemDto.getAssignedToId());
            if (assignedTo == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            item.setAssignedTo(assignedTo);
        }

        if (itemDto.getStatus() != null) {
            item.setStatus(Math.toIntExact(itemDto.getStatus()));
        }
        // Сохраняем как новый History (если логируется)
        jtracService.updateItem(item, user);

        return ResponseEntity.ok("Field updated");
    }

    @PatchMapping("/{id}/field")
    @ResponseBody
    public ResponseEntity<?> updateCustomField(
            @PathVariable Long id,
            @RequestParam String fieldCode,
            @RequestParam String fieldValue,
            Principal principal
    ) {
        User user = jtracService.findUserByLoginName(principal.getName());
        Item item = jtracService.findItemById(id);

        // Проверка, что пользователь имеет доступ
        if (!user.getSpaces().contains(item.getSpace())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied to this item");
        }

        // Проверка, что поле допустимое
        Map<String, Field> fieldMap = item.getSpace().getMetadata().getFields();
        if (!fieldMap.containsKey(fieldCode)) {
            return ResponseEntity.badRequest().body("Unknown field: " + fieldCode);
        }

        // Установка значения в нужное поле
        item.setValue(fieldCode, fieldValue);  // сохранится как строка; преобразование — отдельно при необходимости

        // Сохраняем как новый History (если логируется)
        jtracService.updateItem(item, user);

        return ResponseEntity.ok("Field updated");
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
