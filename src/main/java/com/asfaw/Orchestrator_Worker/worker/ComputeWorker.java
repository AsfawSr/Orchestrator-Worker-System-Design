package com.asfaw.Orchestrator_Worker.worker;

import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskResult;
import com.asfaw.Orchestrator_Worker.task.TaskType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Worker specialized for CPU-intensive computational tasks.
 * Handles operations like mathematical calculations, data processing, and algorithms.
 * 
 * Example use cases:
 * - Prime number calculation
 * - Fibonacci sequence generation
 * - Matrix operations
 * - Data transformations
 * 
 * @author TaskForge Team
 */
@Slf4j
@Component
public class ComputeWorker extends BaseWorker {
    
    public ComputeWorker() {
        super(TaskType.COMPUTE);
    }
    
    @Override
    protected TaskResult processTask(Task task) throws Exception {
        log.debug("ComputeWorker {} processing compute task {}", getWorkerId(), task.getTaskId());
        
        Map<String, Object> payload = task.getPayload();
        String operation = (String) payload.getOrDefault("operation", "calculate");
        
        Map<String, Object> resultData = new HashMap<>();
        
        // Route to appropriate computation based on operation type
        switch (operation.toLowerCase()) {
            case "prime":
                resultData = calculatePrimes(payload);
                break;
            case "fibonacci":
                resultData = calculateFibonacci(payload);
                break;
            case "factorial":
                resultData = calculateFactorial(payload);
                break;
            case "sum":
                resultData = calculateSum(payload);
                break;
            default:
                // Generic computation simulation
                resultData = performGenericComputation(payload);
                break;
        }
        
        return TaskResult.success(resultData, 0);
    }
    
    /**
     * Calculate prime numbers up to a given limit
     */
    private Map<String, Object> calculatePrimes(Map<String, Object> payload) {
        int limit = ((Number) payload.getOrDefault("limit", 100)).intValue();
        
        log.debug("Calculating primes up to {}", limit);
        
        // Sieve of Eratosthenes algorithm
        boolean[] isPrime = new boolean[limit + 1];
        for (int i = 2; i <= limit; i++) {
            isPrime[i] = true;
        }
        
        for (int i = 2; i * i <= limit; i++) {
            if (isPrime[i]) {
                for (int j = i * i; j <= limit; j += i) {
                    isPrime[j] = false;
                }
            }
        }
        
        int count = 0;
        for (int i = 2; i <= limit; i++) {
            if (isPrime[i]) count++;
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "prime");
        result.put("limit", limit);
        result.put("primeCount", count);
        result.put("message", "Calculated " + count + " primes up to " + limit);
        
        return result;
    }
    
    /**
     * Calculate Fibonacci number at given position
     */
    private Map<String, Object> calculateFibonacci(Map<String, Object> payload) {
        int position = ((Number) payload.getOrDefault("position", 10)).intValue();
        
        log.debug("Calculating Fibonacci at position {}", position);
        
        long result = fibonacci(position);
        
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("operation", "fibonacci");
        resultMap.put("position", position);
        resultMap.put("value", result);
        resultMap.put("message", "Fibonacci(" + position + ") = " + result);
        
        return resultMap;
    }
    
    /**
     * Recursive Fibonacci with memoization
     */
    private long fibonacci(int n) {
        if (n <= 1) return n;
        
        long[] fib = new long[n + 1];
        fib[0] = 0;
        fib[1] = 1;
        
        for (int i = 2; i <= n; i++) {
            fib[i] = fib[i - 1] + fib[i - 2];
        }
        
        return fib[n];
    }
    
    /**
     * Calculate factorial of a number
     */
    private Map<String, Object> calculateFactorial(Map<String, Object> payload) {
        int number = ((Number) payload.getOrDefault("number", 5)).intValue();
        
        if (number < 0) {
            throw new IllegalArgumentException("Factorial not defined for negative numbers");
        }
        
        log.debug("Calculating factorial of {}", number);
        
        long result = 1;
        for (int i = 2; i <= number; i++) {
            result *= i;
        }
        
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("operation", "factorial");
        resultMap.put("number", number);
        resultMap.put("value", result);
        resultMap.put("message", number + "! = " + result);
        
        return resultMap;
    }
    
    /**
     * Calculate sum of an array of numbers
     */
    private Map<String, Object> calculateSum(Map<String, Object> payload) {
        @SuppressWarnings("unchecked")
        var numbers = (java.util.List<Number>) payload.get("numbers");
        
        if (numbers == null || numbers.isEmpty()) {
            throw new IllegalArgumentException("Numbers array is required for sum operation");
        }
        
        log.debug("Calculating sum of {} numbers", numbers.size());
        
        double sum = numbers.stream()
                .mapToDouble(Number::doubleValue)
                .sum();
        
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("operation", "sum");
        resultMap.put("count", numbers.size());
        resultMap.put("sum", sum);
        resultMap.put("message", "Sum of " + numbers.size() + " numbers = " + sum);
        
        return resultMap;
    }
    
    /**
     * Generic computation simulation
     */
    private Map<String, Object> performGenericComputation(Map<String, Object> payload) {
        log.debug("Performing generic computation");
        
        // Simulate computational work
        try {
            Thread.sleep(1000 + (long)(Math.random() * 2000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "generic_compute");
        result.put("status", "completed");
        result.put("message", "Generic computation completed successfully");
        result.put("inputSize", payload.size());
        
        return result;
    }
}
