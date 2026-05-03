package com.os.workshop.features.service.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

@Converter(autoApply = false)
public class ServiceStatusConverter implements AttributeConverter<List<Status>, String> {

    private static final Logger logger = LoggerFactory.getLogger(ServiceStatusConverter.class);

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Override
    public String convertToDatabaseColumn(List<Status> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }

        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            logger.error("Erro ao converter List<Status> para JSON", e);
            throw new IllegalArgumentException("Erro ao converter serviceStatus para JSON", e);
        }
    }

    @Override
    public List<Status> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank() || "[]".equals(dbData)) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(dbData, new TypeReference<List<Status>>() {});
        } catch (JsonProcessingException e) {
            logger.error("Erro ao converter JSON para List<Status>. Valor recebido: {}", dbData, e);
            throw new IllegalArgumentException("Erro ao converter JSON para serviceStatus", e);
        }
    }
}
