package info.jtrac.web.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Schema(description = "DTO for creating a new user")
public class UserDto {
    @Schema(description = "Unique login name for the user", example = "john.doe")
    private String loginName;
    @Schema(description = "Full name of the user", example = "John Doe")
    private String name;
    @Schema(description = "Email address of the user", example = "john.doe@example.com")
    private String email;
    @Schema(description = "Password for the new user", example = "password123")
    private String password;
    private String avatarUrl;
}

