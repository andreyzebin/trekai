package info.jtrac.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@Schema(description = "DTO for partially updating an item (PATCH)")
public class ItemPatchDto {

    @Schema(description = "ID of the new user to assign the item to (optional)", example = "3")
    private Long assignedToId;

    @Schema(description = "A map of custom field values to update. The key is the internal field name (e.g., 'CUS_INT_01').",
            example = "{\"CUS_INT_01\": 1, \"CUS_STR_01\": \"Customer ABC\"}")
    private Map<String, String> customFields;
}
