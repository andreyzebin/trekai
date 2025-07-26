package info.jtrac.ui.item;

import lombok.Data;

@Data
public class ItemSearchForm {
    private String summary;
    private String detail;
    private String assignedToId;
    private String status;
    // Future fields can be added here
}