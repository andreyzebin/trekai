package info.jtrac.web.api.dto;


import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldUpdateDto {
    private String fieldName;
    private String valueBefore;
    @Column(columnDefinition = "TEXT")
    private String valueAfter;
}
