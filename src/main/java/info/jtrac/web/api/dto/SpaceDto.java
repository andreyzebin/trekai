package info.jtrac.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO for creating or updating a space")
public class SpaceDto {
    @Schema(description = "A short, unique code for the space (e.g., 'PROJ')", example = "TEST")
    private String prefixCode;
    @Schema(description = "The full name of the space", example = "Test Project")
    private String name;
    @Schema(description = "A detailed description of the space", example = "A space for testing jtrac functionality.")
    private String description;
    @Schema(description = "Whether guests are allowed to access this space", example = "false")
    private boolean guestAllowed;
}

