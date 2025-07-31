package info.jtrac.web.api;

import info.jtrac.web.api.dto.CommentDto;
import info.jtrac.web.api.dto.FieldUpdateDto;
import info.jtrac.web.api.dto.ItemNotificationDto;
import info.jtrac.web.api.dto.PagedResponseDto;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications API", description = "Endpoints for getting notifications")
public class NotificationController {

    @GetMapping
    public PagedResponseDto<ItemNotificationDto> getNotifications(
            @RequestParam int start,
            @RequestParam(defaultValue = "15", required = false) Integer limit
    ) {
        // ⚠ Здесь должен быть вызов сервиса или БД
        return new PagedResponseDto<>(mockNotifications().stream()
                .skip(start)
                .limit(limit)
                .toList(), Integer.MAX_VALUE);
    }

    private List<ItemNotificationDto> mockNotifications() {
        List<ItemNotificationDto> list = new ArrayList<>();

        ItemNotificationDto notification = new ItemNotificationDto();
        notification.setItemId(101L);
        notification.setAuthor("alice");
        notification.setType(ItemNotificationDto.ChangeType.UPDATED);

        FieldUpdateDto update = new FieldUpdateDto();
        update.setFieldName("status");
        update.setValueAfter("Done");
        notification.setUpdates(List.of(update));

        CommentDto comment = new CommentDto();
        comment.setText("Item marked as done.");
        notification.setComment(comment);

        list.add(notification);

        return list;
    }
}
