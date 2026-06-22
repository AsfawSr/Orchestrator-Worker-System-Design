package com.asfaw.Orchestrator_Worker.worker;

import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskResult;
import com.asfaw.Orchestrator_Worker.task.TaskType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Worker specialized for AI/ML-related tasks.
 * Handles operations like model inference, data analysis, predictions.
 * 
 * Example use cases:
 * - Text classification
 * - Sentiment analysis
 * - Image recognition
 * - Recommendation generation
 * 
 * @author TaskForge Team
 */
@Slf4j
@Component
public class AiWorker extends BaseWorker {
    
    public AiWorker() {
        super(TaskType.AI);
    }
    
    @Override
    protected TaskResult processTask(Task task) throws Exception {
        log.debug("AiWorker {} processing AI task {}", getWorkerId(), task.getTaskId());
        
        Map<String, Object> payload = task.getPayload();
        String operation = (String) payload.getOrDefault("operation", "inference");
        
        Map<String, Object> resultData = new HashMap<>();
        
        // Route to appropriate AI operation
        switch (operation.toLowerCase()) {
            case "sentiment":
                resultData = analyzeSentiment(payload);
                break;
            case "classification":
                resultData = classifyText(payload);
                break;
            case "recommendation":
                resultData = generateRecommendations(payload);
                break;
            case "prediction":
                resultData = makePrediction(payload);
                break;
            default:
                // Generic AI inference simulation
                resultData = performGenericInference(payload);
                break;
        }
        
        return TaskResult.success(resultData, 0);
    }
    
    /**
     * Analyze sentiment of text
     */
    private Map<String, Object> analyzeSentiment(Map<String, Object> payload) {
        String text = (String) payload.getOrDefault("text", "");
        
        log.debug("Analyzing sentiment for text of length {}", text.length());
        
        // Simulate model inference time
        simulateModelInference(1500, 3000);
        
        // Simple sentiment analysis simulation
        String sentiment;
        double confidence;
        
        // Basic keyword-based simulation
        String lowerText = text.toLowerCase();
        if (lowerText.contains("good") || lowerText.contains("great") || lowerText.contains("excellent")) {
            sentiment = "POSITIVE";
            confidence = 0.85 + (Math.random() * 0.15);
        } else if (lowerText.contains("bad") || lowerText.contains("poor") || lowerText.contains("terrible")) {
            sentiment = "NEGATIVE";
            confidence = 0.80 + (Math.random() * 0.15);
        } else {
            sentiment = "NEUTRAL";
            confidence = 0.70 + (Math.random() * 0.20);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "sentiment");
        result.put("sentiment", sentiment);
        result.put("confidence", String.format("%.2f", confidence));
        result.put("textLength", text.length());
        result.put("message", "Sentiment analysis completed: " + sentiment);
        
        return result;
    }
    
    /**
     * Classify text into categories
     */
    private Map<String, Object> classifyText(Map<String, Object> payload) {
        String text = (String) payload.getOrDefault("text", "");
        
        log.debug("Classifying text of length {}", text.length());
        
        // Simulate model inference
        simulateModelInference(2000, 4000);
        
        // Simulated categories
        List<String> categories = Arrays.asList(
                "Technology", "Business", "Sports", "Entertainment", "Science"
        );
        
        // Random classification simulation
        String category = categories.get(new Random().nextInt(categories.size()));
        double confidence = 0.75 + (Math.random() * 0.25);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "classification");
        result.put("category", category);
        result.put("confidence", String.format("%.2f", confidence));
        result.put("textLength", text.length());
        result.put("message", "Text classified as: " + category);
        
        return result;
    }
    
    /**
     * Generate recommendations
     */
    private Map<String, Object> generateRecommendations(Map<String, Object> payload) {
        String userId = (String) payload.getOrDefault("userId", "user123");
        int count = ((Number) payload.getOrDefault("count", 5)).intValue();
        
        log.debug("Generating {} recommendations for user {}", count, userId);
        
        // Simulate model inference
        simulateModelInference(2500, 5000);
        
        List<Map<String, Object>> recommendations = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", "item-" + (1000 + i));
            item.put("score", String.format("%.2f", 0.70 + (Math.random() * 0.30)));
            item.put("reason", "Based on your previous interests");
            recommendations.add(item);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "recommendation");
        result.put("userId", userId);
        result.put("recommendationCount", count);
        result.put("recommendations", recommendations);
        result.put("message", "Generated " + count + " recommendations");
        
        return result;
    }
    
    /**
     * Make prediction based on input features
     */
    private Map<String, Object> makePrediction(Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        var features = (Map<String, Object>) payload.getOrDefault("features", new HashMap<>());
        
        log.debug("Making prediction with {} features", features.size());
        
        // Simulate model inference
        simulateModelInference(1800, 3500);
        
        // Simulated prediction
        double predictionValue = 50.0 + (Math.random() * 100.0);
        double confidence = 0.80 + (Math.random() * 0.20);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "prediction");
        result.put("predictionValue", String.format("%.2f", predictionValue));
        result.put("confidence", String.format("%.2f", confidence));
        result.put("featureCount", features.size());
        result.put("message", "Prediction completed successfully");
        
        return result;
    }
    
    /**
     * Generic AI inference simulation
     */
    private Map<String, Object> performGenericInference(Map<String, Object> payload) {
        log.debug("Performing generic AI inference");
        
        // Simulate model inference
        simulateModelInference(2000, 4000);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "generic_ai");
        result.put("status", "completed");
        result.put("message", "Generic AI inference completed successfully");
        result.put("inputSize", payload.size());
        result.put("modelVersion", "v1.0.0");
        
        return result;
    }
    
    /**
     * Simulate model inference time
     */
    private void simulateModelInference(int minMs, int maxMs) {
        try {
            long delay = minMs + (long)(Math.random() * (maxMs - minMs));
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Model inference interrupted");
        }
    }
}
