package info.jtrac.web.api.dto;

import info.jtrac.domain.History;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class HistoryDto {
    private Long id;
    private String loggedBy;
    private String assignedTo;
    private String comment;
    private Integer status;
    private Date timeStamp;

    private FieldUpdateDto change;

    public HistoryDto(History history) {
        this.id = history.getId();

        this.loggedBy = history.getLoggedBy() != null
                ? history.getLoggedBy().getLoginName()
                : null;

        this.assignedTo = history.getAssignedTo() != null
                ? history.getAssignedTo().getLoginName()
                : null;

        this.comment = history.getComment();
        this.status = history.getStatus();
        this.timeStamp = history.getTimeStamp();
        this.change = history.getChange();
    }

    // Getters and setters...
}
