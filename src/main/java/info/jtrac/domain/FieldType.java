package info.jtrac.domain;

import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

/**
 * the names that are used for the custom fields in the outside
 * world - e.g. the XML representation of the metadata that is
 * persisted to the database
 */
public enum FieldType {
    SELECT(3, "cusInt01", null),
    DECIMAL(4, "cusDbl01", "integer"),
    STRING(5, "cusStr01", "string"),
    DATE(6, "cusTim01", "date"),
    TEXT(7, "cusText01", "text");

    private final int type;
    private final String text;
    private final String text2;

    FieldType(int type, String text, String text2) {
        this.type = type;
        this.text = text;
        this.text2 = text2;
    }

    public boolean isDatePickerType() {
        return type == 6;
    }

    public boolean isTextArea() {
        return type == 7;
    }

    public boolean isDecimalNumberType() {
        return type == 4;
    }

    public int getType() {
        return type;
    }

    @JsonValue
    public String getText() {
        return text2;
    }

    public static FieldType ofInt(int val) {
        return switch (val) {
            case 1, 3 -> FieldType.SELECT;
            case 2, 5 -> FieldType.STRING;
            case 4 -> FieldType.DECIMAL;
            case 6 -> FieldType.DATE;
            case 7 -> FieldType.TEXT;
            default -> throw new IllegalArgumentException("Unknown type  " + val);
        };
    }

    public static FieldType ofJsonSchemaType(String val) {
        return Arrays.stream(values()).filter(cv -> val.equals(cv.getText()))
                .findAny().orElseThrow(() -> new IllegalArgumentException("Unknown type  " + val));
    }


    public boolean isDropDownType() {
        return type == 3;
    }


    public String getDescription() {
        switch (type) {
            case 3:
                return "Drop Down List";
            case 4:
                return "Decimal Number";
            case 5:
                return "String Field";
            case 6:
                return "Date Field";
            case 7:
                return "Text Field";
            default:
                throw new RuntimeException("Unknown type " + type);
        }
    }
}
