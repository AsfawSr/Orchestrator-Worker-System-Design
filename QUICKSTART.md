# Quick Start Guide

Get up and running with the Orchestrator-Worker system in 5 minutes!

## 🚀 Step-by-Step Guide

### Step 1: Verify Prerequisites

```bash
# Check Java version (must be 21+)
java -version

# Check Maven
mvn -version
```

### Step 2: Build the Project

```bash
cd "c:\Users\pc\Desktop\SpringBoot\Spring Boot projects\System Deisgn\Orchestrator-Worker"
mvn clean package
```

### Step 3: Run the Application

```bash
mvn spring-boot:run
```

Or run the JAR directly:
```bash
java -jar target/Orchestrator-Worker-0.0.1-SNAPSHOT.jar
```

### Step 4: Verify It's Running

Open your browser or use curl:
```bash
curl http://localhost:8080/api/tasks/health
```

Expected response:
```json
{
  "status": "UP",
  "service": "Orchestrator-Worker"
}
```

### Step 5: Submit Your First Task

**Example 1: Calculate Prime Numbers**

```bash
curl -X POST http://localhost:8080/api/tasks ^
  -H "Content-Type: application/json" ^
  -d "{\"taskType\":\"COMPUTE\",\"payload\":{\"operation\":\"prime\",\"limit\":1000},\"priority\":8}"
```

Save the `taskId` from the response!

**Example 2: AI Sentiment Analysis**

```bash
curl -X POST http://localhost:8080/api/tasks ^
  -H "Content-Type: application/json" ^
  -d "{\"taskType\":\"AI\",\"payload\":{\"operation\":\"sentiment\",\"text\":\"This is amazing!\"}}"
```

### Step 6: Check Task Status

Replace `{taskId}` with the ID from step 5:

```bash
curl http://localhost:8080/api/tasks/{taskId}
```

### Step 7: View System Statistics

```bash
curl http://localhost:8080/api/tasks/stats
```

---

## 🎯 Common Tasks

### Submit Different Task Types

#### Compute Task - Fibonacci
```bash
curl -X POST http://localhost:8080/api/tasks ^
  -H "Content-Type: application/json" ^
  -d "{\"taskType\":\"COMPUTE\",\"payload\":{\"operation\":\"fibonacci\",\"position\":25}}"
```

#### IO Task - API Call
```bash
curl -X POST http://localhost:8080/api/tasks ^
  -H "Content-Type: application/json" ^
  -d "{\"taskType\":\"IO\",\"payload\":{\"operation\":\"api_call\",\"endpoint\":\"/api/users\"}}"
```

#### AI Task - Text Classification
```bash
curl -X POST http://localhost:8080/api/tasks ^
  -H "Content-Type: application/json" ^
  -d "{\"taskType\":\"AI\",\"payload\":{\"operation\":\"classification\",\"text\":\"Machine learning is transforming industries.\"}}"
```

---

## 📊 Monitoring

### Watch Logs in Real-Time

The application logs show:
- Task submissions
- Worker assignments
- Task completions
- Queue statistics (every 10 seconds)

Look for log messages like:
```
Task abc123 submitted successfully. Type: COMPUTE, Priority: 8
Worker COMPUTE-WORKER-1a2b3c4d started processing task abc123
Worker COMPUTE-WORKER-1a2b3c4d successfully completed task abc123 in 1234ms
Queue Status - COMPUTE: 2, IO: 1, AI: 3, Total: 6
```

### Check Statistics Dashboard

```bash
curl http://localhost:8080/api/tasks/stats
```

Shows:
- Total tasks by status
- Queue depths by type
- System health

---

## 🛠️ Troubleshooting

### Port Already in Use

If port 8080 is occupied, change it in `application.yaml`:

```yaml
server:
  port: 8081  # Change to any available port
```

### Application Won't Start

1. Check Java version: `java -version` (must be 21+)
2. Check for compilation errors: `mvn clean compile`
3. Review logs for error messages

### Task Stays in PENDING

- Check worker thread pool configuration
- Verify TaskScheduler is running (check logs)
- Ensure queue is not full (check stats)

---

## 📖 Next Steps

1. **Read the full documentation**: See `README.md`
2. **Explore API examples**: See `API_EXAMPLES.md`
3. **Understand architecture**: See `ARCHITECTURE.md`
4. **Customize configuration**: Edit `src/main/resources/application.yaml`

---

## 🎉 You're Ready!

Your Orchestrator-Worker system is now running and processing tasks!

Try submitting multiple tasks and watch them being processed concurrently by different workers.

Happy coding! 🚀
