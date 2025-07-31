package info.jtrac.web.api.dto;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FieldUpdateDto {
    private String fieldName;
    private String valueAfter;
}
