package info.jtrac.ui.item;

// Using Lombok to reduce boilerplate code for getters and setters
import lombok.Data;

@Data
public class ItemSearchForm {
    private String summary;
    private String detail;
}
