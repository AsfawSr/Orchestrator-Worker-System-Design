package com.asfaw.Orchestrator_Worker.worker;

import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskResult;
import com.asfaw.Orchestrator_Worker.task.TaskType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Worker specialized for I/O-bound tasks.
 * Handles operations like file I/O, database queries, external API calls.
 * 
 * Example use cases:
 * - File read/write operations
 * - Database queries
 * - HTTP requests to external services
 * - Data validation and transformation
 * 
 * @author TaskForge Team
 */
@Slf4j
@Component
public class IoWorker extends BaseWorker {
    
    public IoWorker() {
        super(TaskType.IO);
    }
    
    @Override
    protected TaskResult processTask(Task task) throws Exception {
        log.debug("IoWorker {} processing IO task {}", getWorkerId(), task.getTaskId());
        
        Map<String, Object> payload = task.getPayload();
        String operation = (String) payload.getOrDefault("operation", "fetch");
        
        Map<String, Object> resultData = new HashMap<>();
        
        // Route to appropriate I/O operation
        switch (operation.toLowerCase()) {
            case "file_read":
                resultData = readFile(payload);
                break;
            case "file_write":
                resultData = writeFile(payload);
                break;
            case "api_call":
                resultData = simulateApiCall(payload);
                break;
            case "database_query":
                resultData = simulateDatabaseQuery(payload);
                break;
            default:
                // Generic I/O simulation
                resultData = performGenericIo(payload);
                break;
        }
        
        return TaskResult.success(resultData, 0);
    }
    
    /**
     * Read file content (simulated)
     */
    private Map<String, Object> readFile(Map<String, Object> payload) {
        String fileName = (String) payload.getOrDefault("fileName", "sample.txt");
        
        log.debug("Simulating file read for {}", fileName);
        
        // Simulate I/O latency
        simulateIoDelay(500, 1500);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "file_read");
        result.put("fileName", fileName);
        result.put("status", "success");
        result.put("size", "1024 bytes");
        result.put("message", "File " + fileName + " read successfully");
        
        return result;
    }
    
    /**
     * Write file content (simulated)
     */
    private Map<String, Object> writeFile(Map<String, Object> payload) {
        String fileName = (String) payload.getOrDefault("fileName", "output.txt");
        String content = (String) payload.getOrDefault("content", "sample content");
        
        log.debug("Simulating file write for {}", fileName);
        
        // Simulate I/O latency
        simulateIoDelay(700, 2000);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "file_write");
        result.put("fileName", fileName);
        result.put("status", "success");
        result.put("bytesWritten", content.length());
        result.put("message", "File " + fileName + " written successfully");
        
        return result;
    }
    
    /**
     * Simulate external API call
     */
    private Map<String, Object> simulateApiCall(Map<String, Object> payload) {
        String endpoint = (String) payload.getOrDefault("endpoint", "/api/data");
        String method = (String) payload.getOrDefault("method", "GET");
        
        log.debug("Simulating API call: {} {}", method, endpoint);
        
        // Simulate network latency
        simulateIoDelay(1000, 3000);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "api_call");
        result.put("endpoint", endpoint);
        result.put("method", method);
        result.put("statusCode", 200);
        result.put("responseTime", "1250ms");
        result.put("message", "API call completed successfully");
        
        // Simulate response data
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("id", "12345");
        responseData.put("status", "active");
        responseData.put("timestamp", System.currentTimeMillis());
        result.put("data", responseData);
        
        return result;
    }
    
    /**
     * Simulate database query
     */
    private Map<String, Object> simulateDatabaseQuery(Map<String, Object> payload) {
        String query = (String) payload.getOrDefault("query", "SELECT * FROM users");
        
        log.debug("Simulating database query: {}", query);
        
        // Simulate database latency
        simulateIoDelay(500, 2000);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "database_query");
        result.put("query", query);
        result.put("rowsAffected", 42);
        result.put("executionTime", "850ms");
        result.put("message", "Database query executed successfully");
        
        return result;
    }
    
    /**
     * Generic I/O operation simulation
     */
    private Map<String, Object> performGenericIo(Map<String, Object> payload) {
        log.debug("Performing generic I/O operation");
        
        // Simulate I/O work
        simulateIoDelay(1000, 3000);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "generic_io");
        result.put("status", "completed");
        result.put("message", "Generic I/O operation completed successfully");
        result.put("inputSize", payload.size());
        
        return result;
    }
    
    /**
     * Simulate I/O delay (network, disk, etc.)
     */
    private void simulateIoDelay(int minMs, int maxMs) {
        try {
            long delay = minMs + (long)(Math.random() * (maxMs - minMs));
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("I/O simulation interrupted");
        }
    }
}
