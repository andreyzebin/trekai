package info.jtrac.web.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserSpaceRoleDto {
    private String loginName;
    private String roleKey;
}
