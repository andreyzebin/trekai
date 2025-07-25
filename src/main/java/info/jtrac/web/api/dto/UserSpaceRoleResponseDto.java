package info.jtrac.web.api.dto;

public class UserSpaceRoleResponseDto {
    private String loginName;
    private String roleKey;

    public UserSpaceRoleResponseDto(String loginName, String roleKey) {
        this.loginName = loginName;
        this.roleKey = roleKey;
    }

    // Getters and setters
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }
}
