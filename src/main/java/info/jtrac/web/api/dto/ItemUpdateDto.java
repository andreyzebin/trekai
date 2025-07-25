package info.jtrac.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO for updating an existing item. All fields are optional.")
public class ItemUpdateDto {
    @Schema(description = "A new summary for the item", example = "Login button alignment issue")
    private String summary;
    @Schema(description = "A new detailed description for the item", example = "The button is off by 5 pixels to the left.")
    private String detail;
    @Schema(description = "ID of the new user to assign the item to", example = "3")
    private Long assignedToId;
    @Schema(description = "A new status for the item", example = "1")
    private Integer status;
    @Schema(description = "A comment explaining the reason for the update", example = "Re-assigning to the front-end team.")
    private String comment;
}
