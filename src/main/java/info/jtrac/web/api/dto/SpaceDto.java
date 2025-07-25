package info.jtrac.web.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SpaceDto {
    private String prefixCode;
    private String name;
    private String description;
    private boolean guestAllowed;
}

