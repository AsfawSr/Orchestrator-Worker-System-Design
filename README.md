# Orchestrator-Worker Pattern - Spring Boot Implementation

A production-ready Spring Boot application demonstrating the **Orchestrator-Worker Pattern** for distributed task processing with PostgreSQL persistence, RabbitMQ messaging, retry mechanisms, and specialized worker types.

## 🚀 Current Phase: Phase 1 Complete (PostgreSQL + RabbitMQ)

✅ **Persistent Storage** - PostgreSQL database for task persistence  
✅ **Reliable Messaging** - RabbitMQ for distributed task queuing  
✅ **Horizontal Scaling** - Multiple application instances supported  
✅ **Automatic Retries** - Built-in retry with dead-letter queue  
✅ **Complete Observability** - Rich monitoring and audit trails

📚 **Documentation:**
- [Phase 1 Guide](PHASE1_RABBITMQ_POSTGRESQL.md) - Complete implementation guide
- [Phase 1 Checklist](PHASE1_CHECKLIST.md) - Testing and verification
- [Quick Start](QUICKSTART.md) - Get started in 5 minutes
- [Architecture](ARCHITECTURE.md) - Deep dive into system design

---

## 🎯 Architecture Overview

The Orchestrator-Worker pattern separates task coordination (orchestrator) from task execution (workers), enabling:
- **Scalability**: Workers can be scaled horizontally across multiple instances
- **Resilience**: Failed tasks are automatically retried with persistent storage
- **Specialization**: Different worker types handle different task categories
- **Asynchronous Processing**: Non-blocking task execution using thread pools
- **Persistence**: Tasks and results survive application restarts
- **Distributed**: RabbitMQ enables multi-instance deployments

### Components

```
┌─────────────────────────────────────────────────────────────┐
│                      REST API Layer                         │
│                    (TaskController)                         │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                  Orchestrator Service                       │
│  • Task Submission    • Status Tracking   • Cancellation    │
└─────────────────────────┬───────────────────────────────────┘
                          │
                ┌─────────┼─────────┐
                │         │         │
        ┌───────▼──┐ ┌────▼────┐ ┌─▼────────┐
        │ TaskQueue│ │  Task   │ │  Task    │
        │ (In-Mem) │ │ Manager │ │Scheduler │
        └─────┬────┘ └─────────┘ └────┬─────┘
              │                        │
              │    ┌───────────────────┘
              │    │ (Polls & Dispatches)
        ┌─────▼────▼─────────────────────────┐
        │      Worker Thread Pools            │
        │  ┌────────┐ ┌────────┐ ┌────────┐  │
        │  │Compute │ │   IO   │ │   AI   │  │
        │  │Workers │ │Workers │ │Workers │  │
        │  └────────┘ └────────┘ └────────┘  │
        └─────────────────────────────────────┘
```

## 🚀 Features

- ✅ **Multiple Worker Types**: ComputeWorker, IoWorker, AiWorker
- ✅ **Priority-Based Queue**: High-priority tasks processed first
- ✅ **Automatic Retries**: Configurable retry mechanism with backoff
- ✅ **Thread Pool Management**: Separate pools for each worker type
- ✅ **REST API**: Submit tasks and check status via HTTP
- ✅ **Task Lifecycle Tracking**: PENDING → RUNNING → COMPLETED/FAILED
- ✅ **Comprehensive Logging**: Detailed execution logs
- ✅ **Production Ready**: Proper error handling and validation

## 📋 Prerequisites

- Java 21
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, VS Code)

## 🛠️ Installation & Setup

1. **Clone or navigate to the project directory**

2. **Build the project**
```bash
mvn clean install
```

3. **Run the application**
```bash
mvn spring-boot:run
```

Or run directly:
```bash
java -jar target/Orchestrator-Worker-0.0.1-SNAPSHOT.jar
```

The application will start on **http://localhost:8080**

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api/tasks
```

### Endpoints

#### 1. Submit a Task

**POST** `/api/tasks`

Submit a new task for processing.

**Request Body:**
```json
{
  "taskType": "COMPUTE",
  "payload": {
    "operation": "prime",
    "limit": 1000
  },
  "priority": 7,
  "maxRetries": 3
}
```

**Parameters:**
- `taskType`: `COMPUTE`, `IO`, or `AI`
- `payload`: Task-specific parameters (varies by operation)
- `priority`: 1-10 (higher = more important) - default: 5
- `maxRetries`: 0-10 - default: 3

**Response:** (201 Created)
```json
{
  "taskId": "abc123-def456-...",
  "taskType": "COMPUTE",
  "status": "PENDING",
  "priority": 7,
  "retryCount": 0,
  "maxRetries": 3,
  "createdAt": "2026-06-22T10:30:00",
  "payload": { ... }
}
```

---

#### 2. Get Task Status

**GET** `/api/tasks/{taskId}`

Retrieve current status and result of a task.

**Response:** (200 OK)
```json
{
  "taskId": "abc123-def456-...",
  "taskType": "COMPUTE",
  "status": "COMPLETED",
  "workerId": "COMPUTE-WORKER-1a2b3c4d",
  "createdAt": "2026-06-22T10:30:00",
  "startedAt": "2026-06-22T10:30:02",
  "completedAt": "2026-06-22T10:30:05",
  "result": {
    "success": true,
    "data": {
      "operation": "prime",
      "limit": 1000,
      "primeCount": 168,
      "message": "Calculated 168 primes up to 1000"
    },
    "executionTimeMs": 2843
  }
}
```

---

#### 3. Get All Tasks

**GET** `/api/tasks`

Retrieve all tasks in the system.

**Response:** (200 OK)
```json
[
  { ... task 1 ... },
  { ... task 2 ... }
]
```

---

#### 4. Get Tasks by Status

**GET** `/api/tasks/status/{status}`

Filter tasks by status: `PENDING`, `RUNNING`, `COMPLETED`, `FAILED`, `CANCELLED`

**Response:** (200 OK)
```json
[
  { ... tasks with specified status ... }
]
```

---

#### 5. Cancel a Task

**DELETE** `/api/tasks/{taskId}`

Cancel a pending task (only works for PENDING status).

**Response:** (200 OK)
```json
{
  "message": "Task cancelled successfully",
  "taskId": "abc123-def456-..."
}
```

---

#### 6. Get System Statistics

**GET** `/api/tasks/stats`

Get system-wide statistics and queue sizes.

**Response:** (200 OK)
```json
{
  "tasks": {
    "total": 150,
    "pending": 5,
    "running": 3,
    "completed": 135,
    "failed": 5,
    "cancelled": 2
  },
  "queue": {
    "compute": 2,
    "io": 1,
    "ai": 2,
    "total": 5
  }
}
```

---

#### 7. Health Check

**GET** `/api/tasks/health`

Check if the service is running.

**Response:** (200 OK)
```json
{
  "status": "UP",
  "service": "Orchestrator-Worker"
}
```

---

## 💡 Usage Examples

### Example 1: Compute Task - Calculate Primes

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "COMPUTE",
    "payload": {
      "operation": "prime",
      "limit": 5000
    },
    "priority": 8
  }'
```

### Example 2: Compute Task - Fibonacci

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "COMPUTE",
    "payload": {
      "operation": "fibonacci",
      "position": 25
    }
  }'
```

### Example 3: IO Task - API Call

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "IO",
    "payload": {
      "operation": "api_call",
      "endpoint": "/api/users",
      "method": "GET"
    },
    "priority": 6
  }'
```

### Example 4: AI Task - Sentiment Analysis

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "AI",
    "payload": {
      "operation": "sentiment",
      "text": "This is a great product! I love it."
    }
  }'
```

### Example 5: Check Task Status

```bash
curl http://localhost:8080/api/tasks/{taskId}
```

### Example 6: Get Statistics

```bash
curl http://localhost:8080/api/tasks/stats
```

---

## 🔧 Configuration

Edit `src/main/resources/application.yaml` to customize:

### Server Port
```yaml
server:
  port: 8080
```

### Worker Thread Pools
```yaml
worker:
  compute:
    pool:
      core-size: 2    # Core threads for compute tasks
      max-size: 4     # Maximum threads
  io:
    pool:
      core-size: 5
      max-size: 10
  ai:
    pool:
      core-size: 3
      max-size: 6
  pool:
    queue-capacity: 100  # Task queue capacity per pool
```

### Logging Levels
```yaml
logging:
  level:
    com.asfaw.Orchestrator_Worker: DEBUG
```

---

## 📦 Supported Operations

### COMPUTE Worker Operations
| Operation | Description | Payload Example |
|-----------|-------------|-----------------|
| `prime` | Calculate primes | `{"operation": "prime", "limit": 1000}` |
| `fibonacci` | Calculate Fibonacci | `{"operation": "fibonacci", "position": 20}` |
| `factorial` | Calculate factorial | `{"operation": "factorial", "number": 10}` |
| `sum` | Sum array of numbers | `{"operation": "sum", "numbers": [1,2,3,4,5]}` |

### IO Worker Operations
| Operation | Description | Payload Example |
|-----------|-------------|-----------------|
| `file_read` | Read file | `{"operation": "file_read", "fileName": "data.txt"}` |
| `file_write` | Write file | `{"operation": "file_write", "fileName": "output.txt", "content": "..."}` |
| `api_call` | External API call | `{"operation": "api_call", "endpoint": "/api/data", "method": "GET"}` |
| `database_query` | Database query | `{"operation": "database_query", "query": "SELECT * FROM users"}` |

### AI Worker Operations
| Operation | Description | Payload Example |
|-----------|-------------|-----------------|
| `sentiment` | Sentiment analysis | `{"operation": "sentiment", "text": "Great product!"}` |
| `classification` | Text classification | `{"operation": "classification", "text": "Article content..."}` |
| `recommendation` | Generate recommendations | `{"operation": "recommendation", "userId": "user123", "count": 5}` |
| `prediction` | Make prediction | `{"operation": "prediction", "features": {...}}` |

---

## 🏗️ Project Structure

```
src/main/java/com/asfaw/Orchestrator_Worker/
├── OrchestratorWorkerApplication.java   # Main application class
├── config/
│   ├── AppConfig.java                   # Application configuration
│   └── ThreadPoolConfig.java            # Thread pool beans
├── controller/
│   ├── TaskController.java              # REST API endpoints
│   └── dto/
│       ├── TaskRequest.java             # Request DTO
│       └── TaskResponse.java            # Response DTO
├── orchestrator/
│   ├── OrchestratorService.java         # Main orchestrator
│   ├── TaskManager.java                 # Task state management
│   └── TaskScheduler.java               # Task scheduling & dispatch
├── worker/
│   ├── BaseWorker.java                  # Abstract worker base
│   ├── ComputeWorker.java               # CPU-intensive tasks
│   ├── IoWorker.java                    # I/O-bound tasks
│   └── AiWorker.java                    # AI/ML tasks
├── task/
│   ├── Task.java                        # Task entity
│   ├── TaskType.java                    # Task type enum
│   ├── TaskStatus.java                  # Task status enum
│   └── TaskResult.java                  # Task result model
└── queue/
    ├── TaskQueue.java                   # Queue interface
    └── InMemoryTaskQueue.java           # In-memory implementation
```

---

## 🔄 Task Lifecycle

1. **Submit**: Task submitted via REST API → Status: `PENDING`
2. **Enqueue**: Task added to priority queue
3. **Schedule**: TaskScheduler polls queue every 500ms
4. **Dispatch**: Task assigned to appropriate worker pool
5. **Execute**: Worker processes task → Status: `RUNNING`
6. **Complete**: Task finishes → Status: `COMPLETED` or `FAILED`
7. **Retry**: If failed and retries available, re-enqueue

---

## 🧪 Testing

### Using cURL

See examples above in the Usage Examples section.

### Using Postman

Import the API endpoints:
- Base URL: `http://localhost:8080/api/tasks`
- Add requests for each endpoint
- Set Content-Type: `application/json`

---

## 🚀 Future Enhancements

- [ ] Add Redis/RabbitMQ integration for distributed queue
- [ ] Implement task dependencies (task chains)
- [ ] Add WebSocket for real-time status updates
- [ ] Implement dead letter queue for failed tasks
- [ ] Add metrics with Prometheus/Grafana
- [ ] Implement rate limiting per worker type
- [ ] Add authentication & authorization
- [ ] Database persistence for task history
- [ ] Docker containerization
- [ ] Kubernetes deployment configurations

---

## 📝 License

This project is open-source and available for educational purposes.

---

## 👥 Author

**TaskForge Team**

For questions or support, please open an issue in the repository.

---

## 🙏 Acknowledgments

Built with:
- Spring Boot 4.1.0
- Java 21
- Maven
- Lombok

---

Happy Task Processing! 🎉
