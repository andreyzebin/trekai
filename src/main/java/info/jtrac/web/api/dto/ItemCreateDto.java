package info.jtrac.web.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItemCreateDto {
    private Long spaceId;
    private String summary;
    private String detail;
    private Long assignedToId;
}
