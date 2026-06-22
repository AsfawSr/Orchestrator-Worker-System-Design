# Example: Adding a New Task Type in 3 Simple Steps

This guide shows how easy it is to add a new task type with the flexible thread pool configuration.

---

## 🎯 Scenario: Add VIDEO Processing Task Type

Let's add support for video processing tasks.

---

## Step 1: Update TaskType Enum

**File**: `src/main/java/com/asfaw/Orchestrator_Worker/task/TaskType.java`

```java
public enum TaskType {
    /**
     * CPU-intensive computational tasks
     */
    COMPUTE,
    
    /**
     * I/O-bound tasks
     */
    IO,
    
    /**
     * AI/ML related tasks
     */
    AI,
    
    /**
     * Video processing tasks (NEW!)
     */
    VIDEO
}
```

---

## Step 2: Add Configuration

**File**: `src/main/resources/application.yaml`

```yaml
worker:
  pools:
    compute:
      core-size: 2
      max-size: 4
      queue-capacity: 100
      await-termination-seconds: 60
    
    io:
      core-size: 5
      max-size: 10
      queue-capacity: 100
      await-termination-seconds: 60
    
    ai:
      core-size: 3
      max-size: 6
      queue-capacity: 100
      await-termination-seconds: 60
    
    # NEW: Video processing pool
    video:
      core-size: 2           # Video encoding is CPU-intensive
      max-size: 4
      queue-capacity: 50     # Smaller queue, videos are large
      await-termination-seconds: 300  # Wait longer for video processing
```

---

## Step 3: Create VideoWorker

**File**: `src/main/java/com/asfaw/Orchestrator_Worker/worker/VideoWorker.java`

```java
package com.asfaw.Orchestrator_Worker.worker;

import com.asfaw.Orchestrator_Worker.task.Task;
import com.asfaw.Orchestrator_Worker.task.TaskResult;
import com.asfaw.Orchestrator_Worker.task.TaskType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Worker specialized for video processing tasks.
 * Handles operations like transcoding, thumbnail generation, compression.
 */
@Slf4j
@Component
public class VideoWorker extends BaseWorker {
    
    public VideoWorker() {
        super(TaskType.VIDEO);
    }
    
    @Override
    protected TaskResult processTask(Task task) throws Exception {
        log.debug("VideoWorker {} processing video task {}", 
                getWorkerId(), task.getTaskId());
        
        Map<String, Object> payload = task.getPayload();
        String operation = (String) payload.getOrDefault("operation", "transcode");
        
        Map<String, Object> resultData = switch (operation.toLowerCase()) {
            case "transcode" -> transcodeVideo(payload);
            case "thumbnail" -> generateThumbnail(payload);
            case "compress" -> compressVideo(payload);
            default -> processGenericVideo(payload);
        };
        
        return TaskResult.success(resultData, 0);
    }
    
    private Map<String, Object> transcodeVideo(Map<String, Object> payload) {
        String inputFile = (String) payload.getOrDefault("inputFile", "video.mp4");
        String format = (String) payload.getOrDefault("format", "webm");
        
        log.debug("Transcoding {} to {}", inputFile, format);
        
        // Simulate video transcoding (CPU-intensive)
        simulateVideoProcessing(3000, 8000);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "transcode");
        result.put("inputFile", inputFile);
        result.put("outputFile", inputFile.replace(".mp4", "." + format));
        result.put("format", format);
        result.put("status", "completed");
        result.put("message", "Video transcoded successfully");
        
        return result;
    }
    
    private Map<String, Object> generateThumbnail(Map<String, Object> payload) {
        String videoFile = (String) payload.getOrDefault("videoFile", "video.mp4");
        int timestamp = ((Number) payload.getOrDefault("timestamp", 5)).intValue();
        
        log.debug("Generating thumbnail from {} at {}s", videoFile, timestamp);
        
        // Simulate thumbnail generation
        simulateVideoProcessing(500, 1500);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "thumbnail");
        result.put("videoFile", videoFile);
        result.put("thumbnailFile", videoFile.replace(".mp4", "_thumb.jpg"));
        result.put("timestamp", timestamp);
        result.put("message", "Thumbnail generated successfully");
        
        return result;
    }
    
    private Map<String, Object> compressVideo(Map<String, Object> payload) {
        String inputFile = (String) payload.getOrDefault("inputFile", "video.mp4");
        int quality = ((Number) payload.getOrDefault("quality", 80)).intValue();
        
        log.debug("Compressing {} with quality {}", inputFile, quality);
        
        // Simulate video compression
        simulateVideoProcessing(4000, 10000);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "compress");
        result.put("inputFile", inputFile);
        result.put("outputFile", inputFile.replace(".mp4", "_compressed.mp4"));
        result.put("quality", quality);
        result.put("originalSize", "100MB");
        result.put("compressedSize", "30MB");
        result.put("compressionRatio", "70%");
        result.put("message", "Video compressed successfully");
        
        return result;
    }
    
    private Map<String, Object> processGenericVideo(Map<String, Object> payload) {
        log.debug("Processing generic video operation");
        
        simulateVideoProcessing(2000, 5000);
        
        Map<String, Object> result = new HashMap<>();
        result.put("operation", "generic_video");
        result.put("status", "completed");
        result.put("message", "Video processed successfully");
        
        return result;
    }
    
    private void simulateVideoProcessing(int minMs, int maxMs) {
        try {
            long delay = minMs + (long)(Math.random() * (maxMs - minMs));
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Video processing interrupted");
        }
    }
}
```

---

## ✅ That's It!

The thread pool for VIDEO tasks is **automatically created** by the flexible configuration system.

**What happens at startup:**

```
INFO: VIDEO TaskExecutor initialized: core=2, max=4, queue=50, termination=300s
```

---

## 🧪 Test the New Task Type

### Submit a Video Transcoding Task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "VIDEO",
    "payload": {
      "operation": "transcode",
      "inputFile": "movie.mp4",
      "format": "webm"
    },
    "priority": 8
  }'
```

### Submit a Thumbnail Generation Task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "VIDEO",
    "payload": {
      "operation": "thumbnail",
      "videoFile": "movie.mp4",
      "timestamp": 10
    }
  }'
```

### Submit a Video Compression Task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "VIDEO",
    "payload": {
      "operation": "compress",
      "inputFile": "large_video.mp4",
      "quality": 75
    },
    "priority": 7
  }'
```

---

## 📊 What You Get Automatically

### 1. Dedicated Thread Pool
- 2 core threads always active
- Scale up to 4 threads under load
- Queue capacity of 50 tasks
- 5-minute wait during shutdown

### 2. Worker Metrics
```java
VideoWorker worker = ...;
WorkerStats stats = worker.getStats();
System.out.println("Total processed: " + stats.getTotalProcessed());
System.out.println("Success rate: " + stats.getTotalSucceeded());
```

### 3. Queue Statistics
```bash
curl http://localhost:8080/api/tasks/stats
```

Response includes VIDEO queue:
```json
{
  "queue": {
    "compute": 2,
    "io": 1,
    "ai": 3,
    "video": 5
  }
}
```

---

## 🎉 Benefits of This Approach

1. **No Code Changes to Core System** - ThreadPoolConfig still works
2. **Automatic Integration** - TaskScheduler picks up new type automatically
3. **Independent Configuration** - Video pool doesn't affect other pools
4. **Easy Rollback** - Remove enum + config = feature removed
5. **Production Ready** - Full retry, error handling, metrics included

---

## 🔄 Removing a Task Type

If you decide video processing isn't needed:

1. Remove `VIDEO` from `TaskType` enum
2. Remove `video:` from `application.yaml`
3. Delete `VideoWorker.java`

**Done!** No orphaned code or broken references.

---

## 💡 Pro Tips

### Tip 1: Environment-Specific Video Config

**application-dev.yaml**
```yaml
worker:
  pools:
    video:
      core-size: 1  # Limited resources in dev
      max-size: 2
```

**application-prod.yaml**
```yaml
worker:
  pools:
    video:
      core-size: 4  # More powerful prod servers
      max-size: 8
```

### Tip 2: Monitor Video Pool Performance

```java
@Component
public class VideoPoolMonitor {
    
    @Autowired
    private Map<TaskType, ThreadPoolTaskExecutor> executors;
    
    @Scheduled(fixedDelay = 60000)
    public void checkVideoPool() {
        ThreadPoolTaskExecutor videoExecutor = executors.get(TaskType.VIDEO);
        if (videoExecutor.getThreadPoolExecutor().getQueue().size() > 40) {
            log.warn("Video queue near capacity! Consider scaling up.");
        }
    }
}
```

### Tip 3: Adjust Based on Server Specs

**8-core server:**
```yaml
video:
  core-size: 4   # 50% of cores for video
  max-size: 6
```

**16-core server:**
```yaml
video:
  core-size: 8   # 50% of cores for video
  max-size: 12
```

---

## 🎓 Summary

Adding a new task type requires:
- ✅ 1 enum value
- ✅ 1 config section (5 lines)
- ✅ 1 worker class

**Total time**: ~10 minutes

**Code changes**: Minimal, isolated, clean

**Result**: Fully functional, monitored, configurable task processing for new type!

---

**This is the power of flexible configuration!** 🚀
