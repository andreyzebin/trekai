package info.jtrac.web.api.dto;

import info.jtrac.domain.History;

import java.util.Date;

public class HistoryDto {
    private long id;
    private String loggedBy;
    private String assignedTo;
    private String comment;
    private int status;
    private Date timeStamp;

    public HistoryDto(History history) {
        this.id = history.getId();
        this.loggedBy = history.getLoggedBy().getLoginName();
        if (history.getAssignedTo() != null) {
            this.assignedTo = history.getAssignedTo().getLoginName();
        }
        this.comment = history.getComment();
        this.status = history.getStatus();
        this.timeStamp = history.getTimeStamp();
    }

    // Getters and setters...
}
