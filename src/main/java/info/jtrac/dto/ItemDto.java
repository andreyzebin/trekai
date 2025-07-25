package info.jtrac.dto;

import info.jtrac.domain.Item;
import lombok.Data;

@Data
public class ItemDto {

    private long id;
    private String summary;
    private String detail;
    private String loggedBy;
    private String assignedTo;
    private String status;

    public ItemDto(Item item) {
        this.id = item.getId();
        this.summary = item.getSummary();
        this.detail = item.getDetail();
        this.loggedBy = item.getLoggedBy().getName();
        this.assignedTo = item.getAssignedTo().getName();
        this.status = item.getStatusValue();
    }
}
