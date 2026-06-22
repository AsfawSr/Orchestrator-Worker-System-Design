# Flexible Thread Pool Configuration Guide

## 🎯 Overview

The thread pool configuration has been refactored to be **fully flexible and extensible**. You can now:
- Configure thread pools for any task type via `application.yaml`
- Add new task types without modifying code
- Tune each pool independently
- Override defaults easily

---

## 📝 How It Works

### Architecture

```
application.yaml
      ↓
ThreadPoolProperties (loads config)
      ↓
ThreadPoolConfig (creates executors)
      ↓
Map<TaskType, ThreadPoolTaskExecutor>
```

### Key Components

1. **ThreadPoolProperties** - Configuration holder with `@ConfigurationProperties`
2. **ThreadPoolConfig** - Creates executors dynamically based on configuration
3. **application.yaml** - Your configuration source

---

## ⚙️ Configuration Format

### Basic Structure

```yaml
worker:
  pools:
    <task-type-name>:
      core-size: <number>              # Core threads (always alive)
      max-size: <number>               # Maximum threads under load
      queue-capacity: <number>         # Task queue capacity
      await-termination-seconds: <number>  # Shutdown wait time
```

### Current Configuration

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
```

---

## 🚀 Usage Examples

### Example 1: Add a New Task Type

Say you want to add a `VIDEO` task type for video processing:

**Step 1: Add to TaskType enum**

```java
public enum TaskType {
    COMPUTE,
    IO,
    AI,
    VIDEO  // Add new type
}
```

**Step 2: Add configuration to application.yaml**

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
    
    # New video task type
    video:
      core-size: 2
      max-size: 4
      queue-capacity: 50
      await-termination-seconds: 120
```

**Step 3: Create VideoWorker**

```java
@Component
public class VideoWorker extends BaseWorker {
    public VideoWorker() {
        super(TaskType.VIDEO);
    }
    
    @Override
    protected TaskResult processTask(Task task) {
        // Video processing logic
        return TaskResult.success(data, executionTime);
    }
}
```

**That's it!** The thread pool is automatically created and configured. ✅

---

### Example 2: Tune Existing Pools

Increase IO worker pool for high-traffic API:

```yaml
worker:
  pools:
    io:
      core-size: 10      # Increased from 5
      max-size: 20       # Increased from 10
      queue-capacity: 200 # Increased from 100
      await-termination-seconds: 60
```

---

### Example 3: Environment-Specific Configuration

**application-dev.yaml** (Development)
```yaml
worker:
  pools:
    compute:
      core-size: 1
      max-size: 2
      queue-capacity: 10
```

**application-prod.yaml** (Production)
```yaml
worker:
  pools:
    compute:
      core-size: 4
      max-size: 8
      queue-capacity: 500
```

Run with: `java -jar app.jar --spring.profiles.active=prod`

---

## 💡 Accessing Thread Pools

### Method 1: Inject by Name (Backward Compatible)

```java
@Autowired
@Qualifier("computeTaskExecutor")
private ThreadPoolTaskExecutor computeExecutor;

@Autowired
@Qualifier("ioTaskExecutor")
private ThreadPoolTaskExecutor ioExecutor;
```

### Method 2: Inject the Map (Flexible)

```java
@Autowired
private Map<TaskType, ThreadPoolTaskExecutor> taskExecutors;

public void submitTask(Task task) {
    ThreadPoolTaskExecutor executor = taskExecutors.get(task.getTaskType());
    executor.submit(() -> processTask(task));
}
```

### Method 3: In TaskScheduler

```java
@Component
public class TaskScheduler {
    private final Map<TaskType, ThreadPoolTaskExecutor> taskExecutors;
    
    public TaskScheduler(Map<TaskType, ThreadPoolTaskExecutor> taskExecutors) {
        this.taskExecutors = taskExecutors;
    }
    
    private ThreadPoolTaskExecutor getExecutorForTaskType(TaskType taskType) {
        return taskExecutors.get(taskType);
    }
}
```

---

## 📊 Thread Pool Sizing Guidelines

### CPU-Bound Tasks (COMPUTE)

```yaml
compute:
  core-size: <number-of-cpu-cores>
  max-size: <number-of-cpu-cores + 1>
  queue-capacity: 100
```

**Rationale**: CPU-bound tasks don't benefit from more threads than CPUs.

### I/O-Bound Tasks (IO, AI with external calls)

```yaml
io:
  core-size: <cpu-cores * 2>
  max-size: <cpu-cores * 4>
  queue-capacity: 200
```

**Rationale**: I/O-bound tasks spend time waiting, so more threads = better throughput.

### Mixed Workload

```yaml
ai:
  core-size: <cpu-cores * 1.5>
  max-size: <cpu-cores * 3>
  queue-capacity: 150
```

**Rationale**: Balance between CPU and I/O characteristics.

---

## 🔍 Monitoring

### Check Pool Configuration at Startup

Look for logs:
```
COMPUTE TaskExecutor initialized: core=2, max=4, queue=100, termination=60s
IO TaskExecutor initialized: core=5, max=10, queue=100, termination=60s
AI TaskExecutor initialized: core=3, max=6, queue=100, termination=60s
```

### Runtime Monitoring

```java
@Component
public class ThreadPoolMonitor {
    
    @Autowired
    private Map<TaskType, ThreadPoolTaskExecutor> taskExecutors;
    
    @Scheduled(fixedDelay = 30000) // Every 30 seconds
    public void logPoolStats() {
        taskExecutors.forEach((type, executor) -> {
            ThreadPoolExecutor threadPool = executor.getThreadPoolExecutor();
            log.info("{} Pool - Active: {}, Queue: {}, Completed: {}", 
                type,
                threadPool.getActiveCount(),
                threadPool.getQueue().size(),
                threadPool.getCompletedTaskCount()
            );
        });
    }
}
```

---

## ⚠️ Common Pitfalls

### 1. TaskType Name Mismatch

❌ **Wrong**:
```yaml
worker:
  pools:
    COMPUTE:  # Uppercase won't match
      core-size: 2
```

✅ **Correct**:
```yaml
worker:
  pools:
    compute:  # Lowercase matches TaskType.COMPUTE
      core-size: 2
```

### 2. Missing Configuration

If a task type has no configuration, **defaults are used** (core=2, max=4, queue=100).

Check logs for:
```
WARN: No thread pool configuration found for VIDEO, using defaults
```

### 3. Pool Exhaustion

If `queue-capacity` is too small and all threads are busy:
- Tasks execute in the calling thread (CallerRunsPolicy)
- This provides backpressure but slows down task submission

**Solution**: Increase `queue-capacity` or `max-size`.

---

## 🎓 Advanced: Custom Rejection Policy

Modify `ThreadPoolConfig.createExecutor()`:

```java
// Instead of CallerRunsPolicy
executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());

// Or custom logic
executor.setRejectedExecutionHandler((runnable, executor) -> {
    log.error("Task rejected! Queue full, threads maxed out");
    // Handle rejection (e.g., store in database, notify admin)
});
```

---

## 📦 Default Values

If configuration is missing, these defaults apply:

| Property | Default Value |
|----------|---------------|
| core-size | 2 |
| max-size | 4 |
| queue-capacity | 100 |
| await-termination-seconds | 60 |

---

## 🔄 Migration from Old Configuration

### Old Structure (Hardcoded)

```java
@Value("${worker.compute.pool.core-size:2}")
private int computePoolCoreSize;

@Bean(name = "computeTaskExecutor")
public ThreadPoolTaskExecutor computeTaskExecutor() {
    // Hardcoded logic for each type
}
```

### New Structure (Flexible)

```java
@Bean
public Map<TaskType, ThreadPoolTaskExecutor> taskExecutors(
        ThreadPoolProperties properties) {
    // Dynamic creation for all types
}
```

**Benefits**:
- ✅ Add task types without code changes
- ✅ Centralized configuration
- ✅ Easier to maintain
- ✅ Environment-specific overrides

---

## 🎉 Summary

The flexible thread pool configuration provides:

1. **Zero Code Changes** - Add task types via config only
2. **Independent Tuning** - Each pool has its own settings
3. **Environment Flexibility** - Different configs per environment
4. **Backward Compatible** - Existing code still works
5. **Easy Monitoring** - Access all executors via map

**Next Steps**: 
- Add new task types by updating enum + yaml
- Tune pools based on actual workload
- Monitor pool metrics to optimize settings

---

**Last Updated**: 2026-06-22  
**Version**: 2.0 (Flexible Configuration)
