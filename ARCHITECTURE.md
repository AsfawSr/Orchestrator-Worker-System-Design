# System Architecture Documentation

## 🏛️ Orchestrator-Worker Pattern

This document provides an in-depth look at the architecture, design decisions, and implementation details.

---

## Table of Contents

1. [Pattern Overview](#pattern-overview)
2. [Component Architecture](#component-architecture)
3. [Data Flow](#data-flow)
4. [Thread Management](#thread-management)
5. [Retry Mechanism](#retry-mechanism)
6. [Scalability Considerations](#scalability-considerations)
7. [Extension Points](#extension-points)

---

## Pattern Overview

### What is the Orchestrator-Worker Pattern?

The Orchestrator-Worker pattern is a distributed computing pattern that separates:
- **Orchestration**: Coordination, routing, and state management
- **Execution**: Actual task processing

### Benefits

✅ **Scalability**: Workers can scale independently
✅ **Resilience**: Failed tasks can be retried automatically
✅ **Specialization**: Different workers for different task types
✅ **Loose Coupling**: Orchestrator doesn't know worker implementation details
✅ **Load Balancing**: Queue naturally distributes work
✅ **Observability**: Centralized tracking of all tasks

---

## Component Architecture

### 1. REST API Layer (`controller/`)

**Purpose**: External interface for clients

```java
TaskController
├── submitTask()       // POST /api/tasks
├── getTaskStatus()    // GET /api/tasks/{id}
├── getAllTasks()      // GET /api/tasks
├── cancelTask()       // DELETE /api/tasks/{id}
└── getStatistics()    // GET /api/tasks/stats
```

**Responsibilities**:
- Request validation using Jakarta Validation
- DTO conversion (TaskRequest ↔ Task ↔ TaskResponse)
- HTTP status code handling
- Error responses

---

### 2. Orchestrator Layer (`orchestrator/`)

#### OrchestratorService

**Role**: Main facade for task operations

```java
public class OrchestratorService {
    - TaskQueue taskQueue
    - TaskManager taskManager
    
    + submitTask(Task): Task
    + getTaskStatus(String): Optional<Task>
    + cancelTask(String): boolean
    + getStatistics(): TaskStatistics
}
```

**Key Operations**:
1. Validate task input
2. Assign unique task ID
3. Store in TaskManager
4. Enqueue to TaskQueue
5. Return task to client

---

#### TaskManager

**Role**: Centralized task state storage

```java
public class TaskManager {
    - ConcurrentHashMap<String, Task> tasks
    
    + saveTask(Task)
    + getTask(String): Optional<Task>
    + updateTask(Task)
    + getTasksByStatus(TaskStatus): Map<String, Task>
}
```

**Design Decision**: In-memory storage
- ✅ Fast access
- ✅ No database dependency
- ❌ Not persistent across restarts
- 🔄 **Future**: Add database layer for persistence

---

#### TaskScheduler

**Role**: Continuous task dispatching

```java
@Scheduled(fixedDelay = 500)
public void scheduleTasks() {
    for (TaskType type : TaskType.values()) {
        processPendingTasks(type);
    }
}
```

**Workflow**:
1. Poll queue every 500ms
2. Dequeue up to 10 tasks per cycle
3. Match task type to worker
4. Submit to appropriate thread pool
5. Handle results asynchronously

**Design Decision**: Pull-based scheduling
- Workers pull from queue vs. queue pushing to workers
- Prevents worker overload
- Natural backpressure mechanism

---

### 3. Queue Layer (`queue/`)

#### TaskQueue Interface

```java
public interface TaskQueue {
    boolean enqueue(Task task);
    Optional<Task> dequeue(TaskType taskType);
    int getQueueSize(TaskType taskType);
}
```

**Why an Interface?**
- Allows multiple implementations
- Current: InMemoryTaskQueue
- Future: RedisTaskQueue, RabbitMQTaskQueue

---

#### InMemoryTaskQueue

**Implementation**: `PriorityBlockingQueue` per task type

```java
Map<TaskType, PriorityBlockingQueue<Task>> queues;
```

**Features**:
- Thread-safe operations
- Priority-based ordering (high priority first)
- Separate queue per task type
- Capacity: configurable (default 100)

**Design Decision**: Separate queues per type
- ✅ Prevents one type from starving others
- ✅ Enables type-specific monitoring
- ✅ Allows independent queue tuning

---

### 4. Worker Layer (`worker/`)

#### BaseWorker (Abstract)

**Role**: Common worker functionality

```java
public abstract class BaseWorker {
    - String workerId
    - TaskType taskType
    - AtomicInteger tasksProcessed
    - AtomicInteger tasksSucceeded
    - AtomicInteger tasksFailed
    
    # abstract TaskResult processTask(Task): TaskResult
    + execute(Task): TaskResult
}
```

**Template Method Pattern**:
- `execute()`: Common execution logic (final)
- `processTask()`: Task-specific logic (abstract)

**Metrics Tracking**:
- Total tasks processed
- Success count
- Failure count
- Current busy status

---

#### Worker Implementations

##### ComputeWorker
- **Purpose**: CPU-intensive tasks
- **Examples**: Prime calculation, Fibonacci, factorial
- **Thread Pool**: Smaller (core=2, max=4)
- **Characteristics**: CPU-bound, long-running

##### IoWorker
- **Purpose**: I/O-bound tasks
- **Examples**: File operations, API calls, database queries
- **Thread Pool**: Larger (core=5, max=10)
- **Characteristics**: I/O-bound, blocking operations

##### AiWorker
- **Purpose**: AI/ML tasks
- **Examples**: Sentiment analysis, classification, predictions
- **Thread Pool**: Moderate (core=3, max=6)
- **Characteristics**: Mixed (CPU + I/O), model inference

---

### 5. Task Layer (`task/`)

#### Task Entity

```java
public class Task {
    - String taskId              // Unique identifier
    - TaskType taskType          // COMPUTE, IO, AI
    - TaskStatus status          // Lifecycle state
    - Map<String, Object> payload  // Input data
    - TaskResult result          // Output data
    - int retryCount            // Current retry attempt
    - int maxRetries            // Max allowed retries
    - int priority              // 1-10 (higher first)
    - LocalDateTime timestamps  // Created, started, completed
}
```

**Lifecycle Methods**:
```java
task.markStarted(workerId);
task.markCompleted(result);
task.markFailed(errorMessage);
```

---

#### TaskStatus Enum

```
PENDING → RUNNING → COMPLETED
                 ↓
              FAILED (retry if possible)
                 ↓
              PENDING (retry)
```

Special states:
- `CANCELLED`: User-initiated cancellation (only for PENDING)

---

#### TaskResult

```java
public class TaskResult {
    - boolean success
    - Map<String, Object> data     // Output data
    - String errorMessage          // If failed
    - long executionTimeMs         // Performance metric
    - LocalDateTime timestamp
}
```

---

### 6. Configuration Layer (`config/`)

#### ThreadPoolConfig

**Purpose**: Configure worker thread pools

```yaml
worker:
  compute:
    pool:
      core-size: 2
      max-size: 4
  io:
    pool:
      core-size: 5
      max-size: 10
  ai:
    pool:
      core-size: 3
      max-size: 6
```

**Thread Pool Sizing Guidelines**:

| Worker Type | CPU-bound? | Pool Size Formula |
|-------------|------------|-------------------|
| Compute     | Yes        | # CPU cores       |
| IO          | No         | Much larger       |
| AI          | Mixed      | Moderate          |

**Rejection Policy**: `CallerRunsPolicy`
- If pool + queue full → execute in calling thread
- Provides backpressure
- Prevents task loss

---

## Data Flow

### Task Submission Flow

```
Client Request
    ↓
[TaskController] ─── validate ───→ TaskRequest DTO
    ↓
[OrchestratorService] ─── create ───→ Task Entity
    ↓
[TaskManager] ─── save ───→ In-Memory Store
    ↓
[TaskQueue] ─── enqueue ───→ Priority Queue
    ↓
[TaskScheduler] ─── poll (500ms) ───→ Dequeue Task
    ↓
[ThreadPoolExecutor] ─── dispatch ───→ Worker Thread
    ↓
[Worker.execute()] ─── process ───→ TaskResult
    ↓
[TaskManager] ─── update ───→ Update Task State
    ↓
[Client polls] ─── GET /tasks/{id} ───→ TaskResponse
```

---

### Retry Flow

```
[Worker] ─── task failed ───→ Check retryCount < maxRetries
    ↓ YES
[Task] ─── incrementRetry() ───→ retryCount++
    ↓
[TaskQueue] ─── re-enqueue ───→ Back to queue
    ↓
[TaskScheduler] ─── poll ───→ Retry execution
    ↓ NO
[Task] ─── markFailed() ───→ Status = FAILED
```

**Retry Strategy**:
- Immediate re-queue (no backoff in current implementation)
- Configurable per task (maxRetries)
- 🔄 **Future**: Add exponential backoff

---

## Thread Management

### Thread Pool Hierarchy

```
Application
    ├── computeTaskExecutor (ThreadPoolTaskExecutor)
    │   ├── Core threads: 2
    │   ├── Max threads: 4
    │   ├── Queue: 100
    │   └── Workers: ComputeWorker instances
    │
    ├── ioTaskExecutor (ThreadPoolTaskExecutor)
    │   ├── Core threads: 5
    │   ├── Max threads: 10
    │   ├── Queue: 100
    │   └── Workers: IoWorker instances
    │
    └── aiTaskExecutor (ThreadPoolTaskExecutor)
        ├── Core threads: 3
        ├── Max threads: 6
        ├── Queue: 100
        └── Workers: AiWorker instances
```

### Thread Lifecycle

1. **Core threads**: Always alive (up to core-size)
2. **Extra threads**: Created on demand (up to max-size)
3. **Idle timeout**: Extra threads die after inactivity
4. **Shutdown**: Graceful (wait for tasks to complete)

---

## Retry Mechanism

### Current Implementation

```java
if (!result.isSuccess() && task.canRetry()) {
    task.incrementRetry();
    taskQueue.enqueue(task);  // Re-queue immediately
}
```

### Retry Configuration

**Per Task**:
```json
{
  "maxRetries": 3
}
```

**Global Defaults**: 3 retries

### Failure Handling

After exhausting retries:
1. Mark task as `FAILED`
2. Store error message
3. Keep in TaskManager for audit
4. No further processing

---

## Scalability Considerations

### Current Design (Single Instance)

✅ **Works For**:
- Development
- Small deployments
- Single server

❌ **Limitations**:
- Single point of failure
- Memory-based (non-persistent)
- Vertical scaling only

---

### Scaling to Distributed System

#### 1. Replace In-Memory Queue

**Option A: Redis**
```java
@Component
public class RedisTaskQueue implements TaskQueue {
    @Autowired
    private RedisTemplate<String, Task> redis;
    
    public boolean enqueue(Task task) {
        redis.opsForList().rightPush("queue:" + task.getType(), task);
    }
}
```

**Option B: RabbitMQ**
```java
@Component
public class RabbitMQTaskQueue implements TaskQueue {
    @Autowired
    private RabbitTemplate rabbit;
    
    public boolean enqueue(Task task) {
        rabbit.convertAndSend("task.exchange", 
                              task.getType().name(), 
                              task);
    }
}
```

#### 2. Replace In-Memory TaskManager

**Database Persistence**:
```java
@Entity
public class Task {
    @Id
    private String taskId;
    // ... other fields
}

@Repository
public interface TaskRepository extends JpaRepository<Task, String> {
}
```

#### 3. Add Service Discovery

**Multiple Worker Instances**:
- Use Kubernetes/Docker Swarm
- Register workers with service registry
- Load balance across instances

---

### Horizontal Scaling Architecture

```
                    ┌─────────────┐
                    │ Load Balancer│
                    └──────┬──────┘
                           │
         ┌─────────────────┼─────────────────┐
         │                 │                 │
    ┌────▼────┐      ┌────▼────┐      ┌────▼────┐
    │Instance1│      │Instance2│      │Instance3│
    └────┬────┘      └────┬────┘      └────┬────┘
         │                │                 │
         └────────────────┼─────────────────┘
                          │
              ┌───────────▼───────────┐
              │   Redis/RabbitMQ      │
              │   (Shared Queue)      │
              └───────────┬───────────┘
                          │
              ┌───────────▼───────────┐
              │   PostgreSQL/MongoDB  │
              │   (Task Storage)      │
              └───────────────────────┘
```

---

## Extension Points

### 1. Add New Worker Type

```java
@Component
public class VideoWorker extends BaseWorker {
    
    public VideoWorker() {
        super(TaskType.VIDEO);
    }
    
    @Override
    protected TaskResult processTask(Task task) {
        // Video processing logic
    }
}
```

### 2. Custom Queue Implementation

```java
@Component
@Primary  // Override default
public class CustomTaskQueue implements TaskQueue {
    // Custom implementation
}
```

### 3. Task Middleware

```java
@Component
public class TaskInterceptor {
    
    @Before("execution(* OrchestratorService.submitTask(..))")
    public void beforeSubmit(Task task) {
        // Log, validate, enrich task
    }
}
```

### 4. Metrics and Monitoring

```java
@Component
public class MetricsCollector {
    
    @Scheduled(fixedDelay = 60000)
    public void collectMetrics() {
        // Send to Prometheus, Grafana, etc.
    }
}
```

---

## Design Patterns Used

1. **Template Method**: BaseWorker
2. **Strategy**: TaskQueue interface
3. **Factory**: Worker creation
4. **Singleton**: Spring beans
5. **Observer**: Event-driven scheduling
6. **Repository**: TaskManager

---

## Performance Characteristics

### Throughput

**Current Configuration**:
- Compute: ~4 concurrent tasks
- IO: ~10 concurrent tasks
- AI: ~6 concurrent tasks
- **Total**: ~20 concurrent tasks

### Latency

**Task Processing Time** = 
Queue Wait Time + Execution Time + Overhead

**Typical Overhead**: < 10ms per task

### Memory Usage

**Per Task**: ~1-2 KB
**10,000 tasks**: ~10-20 MB

---

## Security Considerations

### Current State

⚠️ **No Authentication**: Open API
⚠️ **No Authorization**: Any client can submit tasks
⚠️ **No Rate Limiting**: Potential DoS vector

### Recommendations

1. **Add Spring Security**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

2. **Implement Rate Limiting**
```java
@RateLimiter(name = "taskSubmission")
public Task submitTask(Task task) { ... }
```

3. **Input Validation**
- Already implemented via Jakarta Validation
- Add payload content validation

---

## Testing Strategy

### Unit Tests
- Test individual workers
- Mock TaskQueue and TaskManager
- Verify retry logic

### Integration Tests
- Test full task lifecycle
- Verify queue operations
- Check thread pool behavior

### Load Tests
- Submit 1000+ tasks
- Monitor queue depth
- Measure throughput

---

## Conclusion

This architecture provides a solid foundation for task processing with:
- Clean separation of concerns
- Extensibility through interfaces
- Scalability path clearly defined
- Production-ready error handling

**Next Steps**: See README.md for deployment and usage instructions.

---

**Last Updated**: 2026-06-22
**Version**: 1.0.0
