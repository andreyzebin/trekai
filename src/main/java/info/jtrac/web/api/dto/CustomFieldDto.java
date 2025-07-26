package info.jtrac.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@Schema(description = "DTO for creating a new custom field within a space")
public class CustomFieldDto {

    @Schema(description = "The internal name of the field to activate", example = "CUS_INT_01")
    private String name;

    @Schema(description = "The user-visible label for the field", example = "Priority")
    private String label;

    @Schema(description = "The type of the field (1 = Dropdown, 2 = Text, 3 = Number, etc.)", example = "1")
    private int type;

    @Schema(description = "A map of options for dropdown fields (key = value, value = display text)", example = "{\"1\": \"High\", \"2\": \"Medium\", \"3\": \"Low\"}")
    private Map<String, String> options;
}
