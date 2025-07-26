package info.jtrac.domain;

/**
 * the names that are used for the custom fields in the outside
 * world - e.g. the XML representation of the metadata that is
 * persisted to the database
 */
public enum FieldType {
    SELECT(3, "cusInt01"),
    DECIMAL(4, "cusDbl01"),
    STRING(5, "cusStr01"),
    DATE(6, "cusTim01"),
    TEXT(7, "cusText01");

    private final int type;
    private final String text;

    FieldType(int type, String text) {
        this.type = type;
        this.text = text;
    }

    public int getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    public boolean isDropDownType() {
        return type < 4;
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

    @Override
    public String toString() {
        return text;
    }
}
