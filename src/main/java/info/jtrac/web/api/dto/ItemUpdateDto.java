package info.jtrac.web.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItemUpdateDto {
    private String summary;
    private String detail;
    private Long assignedToId;
    private Integer status;
    private String comment;
}
