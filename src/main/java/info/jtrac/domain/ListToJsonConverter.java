package info.jtrac.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.List;

@Converter
public class ListToJsonConverter implements AttributeConverter<List<?>, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<?> attribute) {
        if (attribute == null) return null;
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Unable to serialize list to JSON", e);
        }
    }

    @Override
    public List<?> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) return null;
        try {
            // Default to List<Object>; if you want concrete types, use subclassing (см. ниже)
            return objectMapper.readValue(dbData, new TypeReference<List<Object>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to deserialize JSON to list", e);
        }
    }
}
