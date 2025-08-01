package info.jtrac.web.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ItemNotificationDto {
    private Long itemId;
    private String author;
    private ChangeType type;
    private List<FieldUpdateDto> updates;
    private CommentDto comment;

    public enum ChangeType {
        CREATED, UPDATED, COMMENTED
    }

    // Getters and setters


}
