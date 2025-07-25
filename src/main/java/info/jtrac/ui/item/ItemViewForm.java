package info.jtrac.ui.item;

import lombok.Data;

@Data
public class ItemViewForm {
    private String comment;
    private Integer status;
    private Long assignedToId;
    // Any other fields that can be updated from the view page
}
