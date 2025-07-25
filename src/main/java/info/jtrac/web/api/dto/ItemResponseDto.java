package info.jtrac.web.api.dto;

import info.jtrac.domain.History;
import info.jtrac.domain.Item;

import java.util.List;
import java.util.stream.Collectors;

public class ItemResponseDto {
    private long id;
    private String refId;
    private String summary;
    private String detail;
    private String loggedBy;
    private String assignedTo;
    private int status;
    private List<HistoryDto> history;

    public ItemResponseDto(Item item) {
        this.id = item.getId();
        this.refId = item.getRefId();
        this.summary = item.getSummary();
        this.detail = item.getDetail();
        this.loggedBy = item.getLoggedBy().getLoginName();
        this.assignedTo = item.getAssignedTo().getLoginName();
        this.status = item.getStatus();
        this.history = item.getHistory().stream().map(HistoryDto::new).collect(Collectors.toList());
    }

    // Getters and setters...
}
