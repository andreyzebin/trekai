package info.jtrac.web.api.dto;

import info.jtrac.domain.Item;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
    private Map<String, String> fieldValues;
    private List<HistoryDto> history;

    public ItemResponseDto(Item item) {
        this.id = item.getId();
        this.refId = item.getRefId();
        this.summary = item.getSummary();
        this.detail = item.getDetail();
        this.loggedBy = item.getLoggedBy() != null ? item.getLoggedBy().getLoginName() : null;
        this.assignedTo = item.getAssignedTo() != null ? item.getAssignedTo().getLoginName() : null;
        this.status = item.getStatus();
        this.history = item.getHistory() != null
                ? item.getHistory().stream().map(HistoryDto::new).collect(Collectors.toList())
                : Collections.emptyList();
        this.fieldValues = item.getFieldValues();
    }


    // Getters and setters...
}
