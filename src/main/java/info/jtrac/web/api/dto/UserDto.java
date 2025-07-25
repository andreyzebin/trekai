package info.jtrac.web.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {
    private String loginName;
    private String name;
    private String email;
    private String password;
}

