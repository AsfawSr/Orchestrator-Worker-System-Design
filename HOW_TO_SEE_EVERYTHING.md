# How to See Everything Working - Quick Start

## 🚀 Start the Application

```bash
mvn spring-boot:run
```

---

## 👀 What You'll See Immediately

### 1. Startup Banner (Automatic)

```
╔═══════════════════════════════════════════════════════════════╗
║        THREAD POOL CONFIGURATION - ORCHESTRATOR-WORKER        ║
╚═══════════════════════════════════════════════════════════════╝

Total Thread Pools Configured: 3

┌─ COMPUTE Pool ─────────────────────────
│  Core Threads:      2
│  Max Threads:       4
│  Queue Capacity:    100
│  Await Termination: 60s
│  Thread Prefix:     compute-worker-
└──────────────────────────────────────────

┌─ IO Pool ─────────────────────────
│  Core Threads:      5
│  Max Threads:       10
│  Queue Capacity:    100
│  Await Termination: 60s
│  Thread Prefix:     io-worker-
└──────────────────────────────────────────

┌─ AI Pool ─────────────────────────
│  Core Threads:      3
│  Max Threads:       6
│  Queue Capacity:    100
│  Await Termination: 60s
│  Thread Prefix:     ai-worker-
└──────────────────────────────────────────
```

**This shows**: Your thread pool configuration is loaded and active!

---

### 2. Periodic Logs (Every 30 seconds)

```
========== Thread Pool Statistics ==========
COMPUTE Pool → Active: 2/4, Queue: 5/100, Completed: 142, Total: 147
IO Pool → Active: 7/10, Queue: 12/100, Completed: 531, Total: 543
AI Pool → Active: 3/6, Queue: 8/100, Completed: 89, Total: 97
============================================
```

**This shows**: Real-time thread and queue activity!

---

## 🧪 Test It in 5 Minutes

### Step 1: Submit Some Tasks

```bash
# Submit 5 COMPUTE tasks
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/tasks \
    -H "Content-Type: application/json" \
    -d '{"taskType":"COMPUTE","payload":{"operation":"prime","limit":1000}}'
  sleep 0.5
done

# Submit 5 IO tasks
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/tasks \
    -H "Content-Type: application/json" \
    -d '{"taskType":"IO","payload":{"operation":"api_call","endpoint":"/api/data"}}'
  sleep 0.5
done

# Submit 5 AI tasks
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/tasks \
    -H "Content-Type: application/json" \
    -d '{"taskType":"AI","payload":{"operation":"sentiment","text":"Great system!"}}'
  sleep 0.5
done
```

---

### Step 2: Watch the Pools in Action

**Check System Overview:**
```bash
curl http://localhost:8080/api/monitoring/overview
```

**Output:**
```json
{
  "totalPools": 3,
  "threads": {
    "active": 10,
    "maximum": 20,
    "utilization": "50.00%"
  },
  "queues": {
    "totalPending": 5,
    "totalCapacity": 300,
    "utilization": "1.67%"
  },
  "tasks": {
    "completedTotal": 10
  },
  "health": {
    "poolsUnderPressure": 0,
    "status": "HEALTHY"
  }
}
```

**You see**: System is processing tasks, threads are active!

---

### Step 3: Drill Down to Specific Pools

**Check COMPUTE Pool:**
```bash
curl http://localhost:8080/api/monitoring/pools/COMPUTE
```

**Output:**
```json
{
  "taskType": "COMPUTE",
  "configuration": {
    "corePoolSize": 2,
    "maxPoolSize": 4,
    "queueCapacity": 100
  },
  "current": {
    "poolSize": 3,
    "activeThreads": 2,
    "queueSize": 1,
    "largestPoolSize": 3
  },
  "metrics": {
    "completedTasks": 4,
    "totalTasks": 5,
    "threadUtilization": "50.00%",
    "queueUtilization": "1.00%"
  },
  "status": {
    "idle": false,
    "underPressure": false
  }
}
```

**You see**: 
- ✅ 2 threads actively working
- ✅ 1 task waiting in queue
- ✅ 4 tasks already completed
- ✅ Pool at 50% utilization

---

### Step 4: Check All Pools at Once

```bash
curl http://localhost:8080/api/monitoring/pools | jq
```

**You see**: Complete stats for COMPUTE, IO, and AI pools!

---

### Step 5: Watch Task Progression

```bash
# Check task statistics
curl http://localhost:8080/api/tasks/stats
```

**Output:**
```json
{
  "tasks": {
    "total": 15,
    "pending": 3,
    "running": 5,
    "completed": 7,
    "failed": 0,
    "cancelled": 0
  },
  "queue": {
    "compute": 1,
    "io": 1,
    "ai": 1,
    "total": 3
  }
}
```

**You see**: Tasks moving through the pipeline!

---

## 🎬 Live Monitoring (Continuous)

### Option 1: Watch Logs

```bash
mvn spring-boot:run | grep "Thread Pool Statistics" -A 5
```

**You'll see updates every 30 seconds:**
```
========== Thread Pool Statistics ==========
COMPUTE Pool → Active: 2/4, Queue: 1/100, Completed: 10, Total: 11
IO Pool → Active: 3/10, Queue: 2/100, Completed: 8, Total: 10
AI Pool → Active: 1/6, Queue: 1/100, Completed: 5, Total: 6
============================================
```

---

### Option 2: Poll API Continuously

**PowerShell:**
```powershell
while ($true) {
    Clear-Host
    Write-Host "=== LIVE MONITOR ===" -ForegroundColor Cyan
    $stats = Invoke-RestMethod http://localhost:8080/api/monitoring/overview
    $stats | ConvertTo-Json
    Start-Sleep -Seconds 2
}
```

**Bash:**
```bash
watch -n 2 'curl -s http://localhost:8080/api/monitoring/overview | jq'
```

---

## 📊 Visual Indicators

### Thread Utilization Colors

- 🟢 **0-50%**: Healthy (plenty of capacity)
- 🟡 **50-80%**: Moderate (system working well)
- 🟠 **80-90%**: High (consider scaling)
- 🔴 **90-100%**: Critical (scale immediately)

### Queue Utilization Colors

- 🟢 **0-50%**: Healthy
- 🟡 **50-80%**: Moderate
- 🔴 **80-100%**: Critical (tasks may be rejected)

---

## 🎯 Quick Verification Checklist

After starting the app, verify:

- [x] **Startup banner shows all pools** → Configuration loaded ✅
- [x] **Periodic logs appear** → Monitoring active ✅
- [x] **Submit task returns task ID** → System accepting work ✅
- [x] **Pool stats show active threads** → Workers processing ✅
- [x] **Completed count increases** → Tasks finishing ✅
- [x] **Queue size changes** → Queue active ✅

---

## 🐛 Troubleshooting: "I don't see anything!"

### Issue 1: No startup banner

**Check:** Are you using the right main class?
```bash
java -jar target/Orchestrator-Worker-0.0.1-SNAPSHOT.jar
```

### Issue 2: No periodic logs

**Check:** Logging level in `application.yaml`
```yaml
logging:
  level:
    com.asfaw.Orchestrator_Worker: DEBUG
```

### Issue 3: API returns 404

**Check:** Server started on port 8080?
```bash
curl http://localhost:8080/api/tasks/health
```

Should return: `{"status":"UP"}`

---

## 💡 Pro Tips

### Tip 1: Open Multiple Terminals

**Terminal 1:** Run the app
```bash
mvn spring-boot:run
```

**Terminal 2:** Submit tasks
```bash
for i in {1..20}; do curl -X POST ...; done
```

**Terminal 3:** Watch stats
```bash
watch -n 1 'curl -s http://localhost:8080/api/monitoring/overview | jq'
```

### Tip 2: Use Browser Developer Tools

Open: `http://localhost:8080/api/monitoring/pools`

Hit F12 → Network tab → See live JSON responses

### Tip 3: Compare Before/After

**Before submitting tasks:**
```bash
curl http://localhost:8080/api/monitoring/overview > before.json
```

**After submitting tasks:**
```bash
curl http://localhost:8080/api/monitoring/overview > after.json
diff before.json after.json
```

---

## 🎉 Summary: What You Can See

| What | Where | When |
|------|-------|------|
| Pool Configuration | Startup logs | App starts |
| Pool Statistics | API `/monitoring/pools` | Anytime |
| System Overview | API `/monitoring/overview` | Anytime |
| Task Progress | API `/tasks/stats` | Anytime |
| Periodic Updates | Console logs | Every 30s |
| Individual Pool | API `/monitoring/pools/{type}` | Anytime |

---

**You now have COMPLETE VISIBILITY into your thread pools!** 🚀

No more guessing - you can see:
- ✅ How many threads are active
- ✅ How many tasks are queued
- ✅ Which pools are under pressure
- ✅ How many tasks completed
- ✅ Real-time utilization metrics

**Happy Monitoring!** 📊
