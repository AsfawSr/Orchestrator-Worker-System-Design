# API Examples - Comprehensive Request Collection

This document contains ready-to-use cURL commands and JSON payloads for testing all API endpoints.

## 🎯 Quick Start

Start the application and run these commands to test the system.

---

## 1. Health Check

```bash
curl http://localhost:8080/api/tasks/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "service": "Orchestrator-Worker"
}
```

---

## 2. COMPUTE Worker Examples

### Calculate Prime Numbers

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "COMPUTE",
    "payload": {
      "operation": "prime",
      "limit": 10000
    },
    "priority": 8,
    "maxRetries": 3
  }'
```

### Calculate Fibonacci

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "COMPUTE",
    "payload": {
      "operation": "fibonacci",
      "position": 30
    },
    "priority": 5
  }'
```

### Calculate Factorial

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "COMPUTE",
    "payload": {
      "operation": "factorial",
      "number": 20
    }
  }'
```

### Calculate Sum of Numbers

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "COMPUTE",
    "payload": {
      "operation": "sum",
      "numbers": [10, 20, 30, 40, 50, 100, 200]
    },
    "priority": 4
  }'
```

### Generic Compute Task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "COMPUTE",
    "payload": {
      "operation": "calculate",
      "data": "some computational work"
    }
  }'
```

---

## 3. IO Worker Examples

### File Read Operation

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "IO",
    "payload": {
      "operation": "file_read",
      "fileName": "data.txt"
    },
    "priority": 6
  }'
```

### File Write Operation

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "IO",
    "payload": {
      "operation": "file_write",
      "fileName": "output.txt",
      "content": "Hello from Orchestrator-Worker system!"
    }
  }'
```

### External API Call

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "IO",
    "payload": {
      "operation": "api_call",
      "endpoint": "/api/users/123",
      "method": "GET"
    },
    "priority": 7
  }'
```

### Database Query

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "IO",
    "payload": {
      "operation": "database_query",
      "query": "SELECT * FROM users WHERE status = active"
    }
  }'
```

### Generic IO Task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "IO",
    "payload": {
      "operation": "fetch",
      "resource": "user-data"
    }
  }'
```

---

## 4. AI Worker Examples

### Sentiment Analysis

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "AI",
    "payload": {
      "operation": "sentiment",
      "text": "This product is absolutely amazing! I love how it works and the quality is excellent."
    },
    "priority": 9
  }'
```

### Text Classification

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "AI",
    "payload": {
      "operation": "classification",
      "text": "Machine learning algorithms can process vast amounts of data to identify patterns and make predictions."
    }
  }'
```

### Generate Recommendations

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "AI",
    "payload": {
      "operation": "recommendation",
      "userId": "user_12345",
      "count": 10
    },
    "priority": 7
  }'
```

### Make Prediction

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "AI",
    "payload": {
      "operation": "prediction",
      "features": {
        "age": 35,
        "income": 75000,
        "education": "bachelors",
        "experience": 10
      }
    }
  }'
```

### Generic AI Inference

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "AI",
    "payload": {
      "operation": "inference",
      "model": "custom-model-v1",
      "input": "data to process"
    }
  }'
```

---

## 5. Task Management Examples

### Get Task Status by ID

```bash
# Replace {taskId} with actual task ID from submission response
curl http://localhost:8080/api/tasks/{taskId}
```

### Get All Tasks

```bash
curl http://localhost:8080/api/tasks
```

### Get Tasks by Status

```bash
# Get all pending tasks
curl http://localhost:8080/api/tasks/status/PENDING

# Get all running tasks
curl http://localhost:8080/api/tasks/status/RUNNING

# Get all completed tasks
curl http://localhost:8080/api/tasks/status/COMPLETED

# Get all failed tasks
curl http://localhost:8080/api/tasks/status/FAILED
```

### Cancel a Task

```bash
# Replace {taskId} with actual task ID
curl -X DELETE http://localhost:8080/api/tasks/{taskId}
```

---

## 6. System Monitoring

### Get System Statistics

```bash
curl http://localhost:8080/api/tasks/stats
```

**Expected Response:**
```json
{
  "tasks": {
    "total": 50,
    "pending": 5,
    "running": 3,
    "completed": 40,
    "failed": 2,
    "cancelled": 0
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

## 7. Advanced Examples

### High Priority Task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "COMPUTE",
    "payload": {
      "operation": "prime",
      "limit": 50000
    },
    "priority": 10,
    "maxRetries": 5
  }'
```

### No Retry Task

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "IO",
    "payload": {
      "operation": "api_call",
      "endpoint": "/api/critical-data"
    },
    "maxRetries": 0
  }'
```

### Batch Task Submission

```bash
# Submit multiple tasks
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/tasks \
    -H "Content-Type: application/json" \
    -d '{
      "taskType": "COMPUTE",
      "payload": {
        "operation": "fibonacci",
        "position": '$i'
      }
    }'
  sleep 0.5
done
```

---

## 8. Full Workflow Example

```bash
# Step 1: Check health
curl http://localhost:8080/api/tasks/health

# Step 2: Submit a task
TASK_ID=$(curl -s -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "AI",
    "payload": {
      "operation": "sentiment",
      "text": "Great service!"
    }
  }' | grep -o '"taskId":"[^"]*' | cut -d'"' -f4)

echo "Task ID: $TASK_ID"

# Step 3: Wait a moment for processing
sleep 3

# Step 4: Check task status
curl http://localhost:8080/api/tasks/$TASK_ID

# Step 5: Check system stats
curl http://localhost:8080/api/tasks/stats
```

---

## 9. Error Handling Examples

### Invalid Task Type

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "INVALID",
    "payload": {}
  }'
```

### Missing Required Fields

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "payload": {
      "operation": "test"
    }
  }'
```

### Invalid Priority

```bash
curl -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{
    "taskType": "COMPUTE",
    "payload": {},
    "priority": 99
  }'
```

---

## 10. PowerShell Examples (Windows)

### Submit Task

```powershell
$body = @{
    taskType = "COMPUTE"
    payload = @{
        operation = "prime"
        limit = 1000
    }
    priority = 8
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/tasks" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

### Get Statistics

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/tasks/stats" -Method Get
```

---

## 📊 Response Time Expectations

| Task Type | Operation | Typical Duration |
|-----------|-----------|------------------|
| COMPUTE | prime (limit: 1000) | 500ms - 1s |
| COMPUTE | fibonacci (n: 30) | 10ms - 50ms |
| COMPUTE | factorial | 5ms - 20ms |
| IO | file_read | 500ms - 1.5s |
| IO | api_call | 1s - 3s |
| IO | database_query | 500ms - 2s |
| AI | sentiment | 1.5s - 3s |
| AI | classification | 2s - 4s |
| AI | recommendation | 2.5s - 5s |

---

## 🔍 Tips

1. **Save Task IDs**: Always save the taskId from submission response to check status later
2. **Priority Matters**: Tasks with priority 10 execute before priority 1
3. **Retry Logic**: Failed tasks auto-retry up to maxRetries times
4. **Queue Monitoring**: Use `/stats` endpoint to monitor queue depths
5. **Batch Testing**: Submit multiple tasks to test concurrent processing

---

Happy Testing! 🚀
