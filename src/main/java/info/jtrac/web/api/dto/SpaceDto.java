package info.jtrac.web.api.dto;

import lombok.Data;

@Data
public class SpaceDto {
    private long id;
    private String prefixCode;
    private String name;
    private String description;
    private boolean guestAllowed;
}
