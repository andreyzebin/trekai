package info.jtrac.web.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSpaceRoleResponseDto {
    private String loginName;
    private String roleKey;
}
