package info.jtrac.web.api.dto;

import lombok.Data;

@Data
public class UserDto {
    private long id;
    private String loginName;
    private String name;
    private String email;
    private String password;
}
