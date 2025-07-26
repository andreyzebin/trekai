package info.jtrac.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@Schema(description = "DTO for creating a new item. Use either spaceId or spacePrefix.")
public class ItemCreateDto {
    @Schema(description = "ID of the space this item belongs to (optional, use if spacePrefix is not provided)", example = "1")
    private Long spaceId;
    @Schema(description = "Prefix code of the space this item belongs to (optional, use if spaceId is not provided)", example = "PROJ1")
    private String spacePrefix;
    @Schema(description = "A brief summary of the item", example = "UI button is not aligned")
    private String summary;
    @Schema(description = "Detailed description of the item", example = "The 'Submit' button on the login page is misaligned on mobile devices.")
    private String detail;
    @Schema(description = "ID of the user this item is assigned to", example = "2")
    private Long assignedToId;

    @Schema(description = "Custom field values, mapped by field code", example = "{\"priority\":\"high\",\"dueDate\":\"2025-08-01\"}")
    private Map<String, String> customFields = new HashMap<>();
}
