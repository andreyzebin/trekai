package info.jtrac.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter
public class FieldsConverter implements AttributeConverter<Map<String, Field>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Field> attribute) {
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting map to JSON", e);
        }
    }

    @Override
    public Map<String, Field> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null) {
                return null;
            }
            return objectMapper.readValue(dbData, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Error converting JSON to map", e);
        }
    }
}
