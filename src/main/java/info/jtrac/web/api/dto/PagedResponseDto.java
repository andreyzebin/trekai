package info.jtrac.web.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@NoArgsConstructor
public class PagedResponseDto<T> {
    private List<T> content;
    private long total;

    public PagedResponseDto(List<T> content, long total) {
        this.content = content;
        this.total = total;
    }

}
