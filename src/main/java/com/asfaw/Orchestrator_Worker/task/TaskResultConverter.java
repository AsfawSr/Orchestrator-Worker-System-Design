package com.asfaw.Orchestrator_Worker.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

/**
 * JPA Converter to store TaskResult as JSON in database.
 * Handles serialization/deserialization for task results.
 * 
 * @author TaskForge Team
 */
@Slf4j
@Converter
public class TaskResultConverter implements AttributeConverter<TaskResult, String> {
    
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());
    
    @Override
    public String convertToDatabaseColumn(TaskResult attribute) {
        if (attribute == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert TaskResult to JSON", e);
            return null;
        }
    }
    
    @Override
    public TaskResult convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(dbData, TaskResult.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to convert JSON to TaskResult", e);
            return null;
        }
    }
}
