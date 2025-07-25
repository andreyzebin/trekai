package info.jtrac.web.api.dto;

import info.jtrac.domain.Item;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ItemResponseDto {
    private Long id;
    private String refId;
    private String summary;
    private String detail;
    private String loggedBy;
    private String assignedTo;
    private Integer status;
    private List<HistoryDto> history;

    public ItemResponseDto(Item item) {
        this.id = item.getId();
        this.refId = item.getRefId();
        if (item.getSummary() != null) {
            this.summary = item.getSummary();
        }
        if (item.getDetail() != null) {
            this.detail = item.getDetail();
        }
        if (item.getLoggedBy() != null) {
            this.loggedBy = item.getLoggedBy().getLoginName();
        }
        if (item.getAssignedTo() != null) {
            this.assignedTo = item.getAssignedTo().getLoginName();
        }
        this.status = item.getStatus();
        if (item.getHistory() != null) {
            this.history = item.getHistory().stream().map(HistoryDto::new).collect(Collectors.toList());
        }
    }

    // Getters and setters...
}
