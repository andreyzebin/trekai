package info.jtrac.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO for creating a new item")
public class ItemCreateDto {
    @Schema(description = "ID of the space this item belongs to", example = "1")
    private Long spaceId;
    @Schema(description = "A brief summary of the item", example = "UI button is not aligned")
    private String summary;
    @Schema(description = "Detailed description of the item", example = "The 'Submit' button on the login page is misaligned on mobile devices.")
    private String detail;
    @Schema(description = "ID of the user this item is assigned to", example = "2")
    private Long assignedToId;
}
