package info.jtrac.web.api;

import info.jtrac.config.ItemWebSocketController;
import info.jtrac.domain.Item;
import info.jtrac.domain.Metadata;
import info.jtrac.domain.Space;
import info.jtrac.domain.User;
import info.jtrac.service.JtracService;
import info.jtrac.web.api.dto.CommentDto;
import info.jtrac.web.api.dto.FieldUpdateDto;
import info.jtrac.web.api.dto.ItemCreateDto;
import info.jtrac.web.api.dto.ItemPatchDto;
import info.jtrac.web.api.dto.ItemResponseDto;
import info.jtrac.web.api.dto.ItemUpdateDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/items")
@Tag(name = "Items API", description = "Endpoints for managing items")
public class ApiItemController {

    private static final Logger logger = LoggerFactory.getLogger(ApiItemController.class);

    private final JtracService jtracService;
    private final ItemWebSocketController ws;

    public ApiItemController(JtracService jtracService, ItemWebSocketController ws) {
        this.jtracService = jtracService;
        this.ws = ws;
    }

    @PostMapping
    public ResponseEntity<ItemResponseDto> createItem(@RequestBody ItemCreateDto itemDto) {
        logger.info("Creating item with summary: {}", itemDto.getSummary());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = jtracService.findUserByLoginName(auth.getName());

        Space space = null;
        if (itemDto.getSpacePrefix() != null) {
            space = jtracService.findSpaceByPrefixCode(itemDto.getSpacePrefix());
        } else if (itemDto.getSpaceId() != null) {
            space = jtracService.loadSpace(itemDto.getSpaceId());
        }

        if (space == null) {
            return ResponseEntity.badRequest().body(null); // Or a proper error DTO
        }

        Item item = new Item();
        if (itemDto.getAssignedToId() != null) {
            User assignedTo = jtracService.loadUser(itemDto.getAssignedToId());
            if (assignedTo == null) {
                return ResponseEntity.badRequest().body(null);
            }
            item.setAssignedTo(assignedTo);
        }

        item.setSpace(space);
        item.setSummary(itemDto.getSummary());
        item.setDetail(itemDto.getDetail());
        item.setLoggedBy(loggedInUser);

        Item savedItem = jtracService.storeItem(item, null);

        return new ResponseEntity<>(new ItemResponseDto(savedItem), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponseDto> getItemById(@PathVariable long id) {
        Item item = jtracService.findItemById(id);
        if (item == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(new ItemResponseDto(item), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponseDto> updateItem(@PathVariable long id, @RequestBody ItemUpdateDto itemDto) {
        Item item = jtracService.findItemById(id);
        if (item == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = jtracService.findUserByLoginName(auth.getName());

        if (itemDto.getSummary() != null) {
            item.setSummary(itemDto.getSummary());
        }
        if (itemDto.getDetail() != null) {
            item.setDetail(itemDto.getDetail());
        }
        if (itemDto.getAssignedToId() != null) {
            User assignedTo = jtracService.loadUser(itemDto.getAssignedToId());
            if (assignedTo == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            item.setAssignedTo(assignedTo);
        }
        if (itemDto.getStatus() != null) {
            item.setStatus(itemDto.getStatus());
        }
        if (itemDto.getComment() != null) {
            item.setEditReason(itemDto.getComment());
        }

        jtracService.updateItem(item, loggedInUser);

        return new ResponseEntity<>(new ItemResponseDto(item), HttpStatus.OK);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemResponseDto> patchItem(@PathVariable long id, @RequestBody ItemPatchDto itemDto) {
        Item item = jtracService.findItemById(id);
        Metadata metadata = item.getSpace().getMetadata();
        if (item == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = jtracService.findUserByLoginName(auth.getName());
        List<FieldUpdateDto> upd = new ArrayList<>();

        // Обработка назначения по ID
        if (itemDto.getAssignedToId() != null) {
            User assignedTo = jtracService.loadUser(itemDto.getAssignedToId());
            if (assignedTo == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            upd.add(FieldUpdateDto.builder()
                    .fieldName("assignedTo")
                    .valueBefore(item.getAssignedTo() != null ? item.getAssignedTo().getLoginName() : "")
                    .valueAfter(assignedTo.getLoginName())
                    .build());
            item.setAssignedTo(assignedTo);
        }
        // Обработка назначения по логину
        else if (itemDto.getAssignedToLogin() != null && !itemDto.getAssignedToLogin().isBlank()) {
            User users = jtracService.findUserByLoginName(itemDto.getAssignedToLogin());
            if (users == null) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            upd.add(FieldUpdateDto.builder()
                    .fieldName("assignedTo")
                    .valueBefore(item.getAssignedTo() != null ? item.getAssignedTo().getLoginName() : "")
                    .valueAfter(users.getLoginName())
                    .build());
            item.setAssignedTo(users);
        }

        if (itemDto.getStatus() != null) {
            if (!List.of(1L, 2L).contains(itemDto.getStatus())) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            upd.add(FieldUpdateDto.builder()
                    .fieldName("status")
                    .valueBefore(String.valueOf(item.getStatus()))
                    .valueAfter(String.valueOf(itemDto.getStatus().intValue()))
                    .build());
            item.setStatus(itemDto.getStatus().intValue());
        }

        if (itemDto.getCustomFields() != null) {
            if (itemDto.getCustomFields().keySet().stream()
                    .anyMatch((code) -> !metadata.getFields().containsKey(code))) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            for (Map.Entry<String, String> entry : itemDto.getCustomFields().entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();

                upd.add(FieldUpdateDto.builder()
                        .fieldName(key)
                        .valueBefore(item.getValue(key))
                        .valueAfter(value)
                        .build());
                item.setValue(key, value);
            }
        }

        jtracService.updateItem(item, loggedInUser, upd);
        ws.notifyItemUpdate(item.getId());

        return new ResponseEntity<>(new ItemResponseDto(item), HttpStatus.OK);
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ItemResponseDto> addComment(
            @PathVariable long id,
            @RequestBody CommentDto dto
    ) {
        Item item = jtracService.findItemById(id);
        if (item == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User loggedInUser = jtracService.findUserByLoginName(auth.getName());

        if (dto.getText() == null || dto.getText().trim().isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (dto.getText().length() > 250) {
            dto.setText(dto.getText().substring(0, 250) + "...");
        }

        item.setEditReason(dto.getText());
        jtracService.updateItem(item, loggedInUser);

        ws.notifyItemUpdate(id); // если WebSocket используется

        return new ResponseEntity<>(new ItemResponseDto(item), HttpStatus.NO_CONTENT);
    }


    @GetMapping
    public ResponseEntity<List<ItemResponseDto>> findItems(
            @RequestParam long spaceId,
            @RequestParam(required = false) String summary,
            @RequestParam(required = false) Long assignedToId,
            @RequestParam(required = false) Integer status) {
        List<Item> items = jtracService.findItems(spaceId, summary, assignedToId, status);
        List<ItemResponseDto> dtos = items.stream().map(ItemResponseDto::new).collect(Collectors.toList());
        return new ResponseEntity<>(dtos, HttpStatus.OK);
    }
}
