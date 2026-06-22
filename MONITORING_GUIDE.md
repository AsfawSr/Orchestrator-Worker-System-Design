# Thread Pool Monitoring Guide

## 🎯 Overview

This guide shows you how to monitor and visualize your thread pools in real-time.

---

## 📺 What You'll See at Startup

When the application starts, you'll see a detailed configuration summary:

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

╔═══════════════════════════════════════════════════════════════╗
║                   MONITORING ENDPOINTS                        ║
╚═══════════════════════════════════════════════════════════════╝

  Pool Statistics:     GET /api/monitoring/pools
  Specific Pool:       GET /api/monitoring/pools/{taskType}
  System Overview:     GET /api/monitoring/overview
  Task Statistics:     GET /api/tasks/stats
  Health Check:        GET /api/tasks/health
```

---

## 📊 Automatic Logging (Every 30 seconds)

The system automatically logs pool statistics:

```
========== Thread Pool Statistics ==========
COMPUTE Pool → Active: 2/4, Queue: 5/100, Completed: 142, Total: 147
IO Pool → Active: 7/10, Queue: 12/100, Completed: 531, Total: 543
AI Pool → Active: 3/6, Queue: 8/100, Completed: 89, Total: 97
============================================
```

**Reading the logs:**
- `Active: 2/4` = 2 threads active out of 4 max
- `Queue: 5/100` = 5 tasks waiting in queue (capacity 100)
- `Completed: 142` = 142 tasks finished
- `Total: 147` = 147 total tasks submitted

---

## 🌐 REST API Endpoints

### 1. Get All Pool Statistics

**Request:**
```bash
curl http://localhost:8080/api/monitoring/pools
```

**Response:**
```json
{
  "compute": {
    "taskType": "COMPUTE",
    "configuration": {
      "corePoolSize": 2,
      "maxPoolSize": 4,
      "queueCapacity": 100
    },
    "current": {
      "poolSize": 3,
      "activeThreads": 2,
      "queueSize": 5,
      "largestPoolSize": 4
    },
    "metrics": {
      "completedTasks": 142,
      "totalTasks": 147,
      "threadUtilization": "50.00%",
      "queueUtilization": "5.00%"
    },
    "status": {
      "idle": false,
      "underPressure": false
    }
  },
  "io": {
    "taskType": "IO",
    "configuration": {
      "corePoolSize": 5,
      "maxPoolSize": 10,
      "queueCapacity": 100
    },
    "current": {
      "poolSize": 8,
      "activeThreads": 7,
      "queueSize": 12,
      "largestPoolSize": 10
    },
    "metrics": {
      "completedTasks": 531,
      "totalTasks": 543,
      "threadUtilization": "70.00%",
      "queueUtilization": "12.00%"
    },
    "status": {
      "idle": false,
      "underPressure": false
    }
  },
  "ai": {
    "taskType": "AI",
    "configuration": {
      "corePoolSize": 3,
      "maxPoolSize": 6,
      "queueCapacity": 100
    },
    "current": {
      "poolSize": 4,
      "activeThreads": 3,
      "queueSize": 8,
      "largestPoolSize": 5
    },
    "metrics": {
      "completedTasks": 89,
      "totalTasks": 97,
      "threadUtilization": "50.00%",
      "queueUtilization": "8.00%"
    },
    "status": {
      "idle": false,
      "underPressure": false
    }
  }
}
```

---

### 2. Get Specific Pool Statistics

**Request:**
```bash
curl http://localhost:8080/api/monitoring/pools/COMPUTE
```

**Response:**
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
    "queueSize": 5,
    "largestPoolSize": 4
  },
  "metrics": {
    "completedTasks": 142,
    "totalTasks": 147,
    "threadUtilization": "50.00%",
    "queueUtilization": "5.00%"
  },
  "status": {
    "idle": false,
    "underPressure": false
  }
}
```

---

### 3. Get System Overview

**Request:**
```bash
curl http://localhost:8080/api/monitoring/overview
```

**Response:**
```json
{
  "totalPools": 3,
  "threads": {
    "active": 12,
    "maximum": 20,
    "utilization": "60.00%"
  },
  "queues": {
    "totalPending": 25,
    "totalCapacity": 300,
    "utilization": "8.33%"
  },
  "tasks": {
    "completedTotal": 762
  },
  "health": {
    "poolsUnderPressure": 0,
    "status": "HEALTHY"
  }
}
```

---

## 🔍 Understanding the Metrics

### Configuration Metrics

| Metric | Description |
|--------|-------------|
| `corePoolSize` | Minimum threads always alive |
| `maxPoolSize` | Maximum threads under load |
| `queueCapacity` | Maximum pending tasks |

### Current State Metrics

| Metric | Description |
|--------|-------------|
| `poolSize` | Current number of threads |
| `activeThreads` | Threads executing tasks now |
| `queueSize` | Tasks waiting in queue |
| `largestPoolSize` | Peak thread count ever reached |

### Performance Metrics

| Metric | Description |
|--------|-------------|
| `completedTasks` | Total tasks finished |
| `totalTasks` | Total tasks submitted |
| `threadUtilization` | % of max threads in use |
| `queueUtilization` | % of queue capacity used |

### Health Indicators

| Metric | Description |
|--------|-------------|
| `idle` | No active threads or queued tasks |
| `underPressure` | Queue >80% full OR threads >90% utilized |

---

## 📈 Monitoring Scenarios

### Scenario 1: Normal Operation

```json
{
  "threadUtilization": "45.00%",
  "queueUtilization": "12.00%",
  "underPressure": false
}
```

✅ **Status**: Healthy  
📊 **Action**: No action needed

---

### Scenario 2: High Load

```json
{
  "threadUtilization": "95.00%",
  "queueUtilization": "65.00%",
  "underPressure": true
}
```

⚠️ **Status**: Under Pressure  
📊 **Action**: Consider increasing `max-size` or `queue-capacity`

---

### Scenario 3: Queue Overflow Risk

```json
{
  "threadUtilization": "100.00%",
  "queueUtilization": "92.00%",
  "underPressure": true
}
```

🚨 **Status**: Critical  
📊 **Action**: Immediately increase `max-size` and `queue-capacity`

---

### Scenario 4: Idle System

```json
{
  "threadUtilization": "0.00%",
  "queueUtilization": "0.00%",
  "idle": true
}
```

💤 **Status**: Idle  
📊 **Action**: System ready for work

---

## 🎯 Real-Time Monitoring Script

Create a script to monitor continuously:

**PowerShell:**
```powershell
while ($true) {
    Clear-Host
    Write-Host "=== Thread Pool Monitor ===" -ForegroundColor Cyan
    Write-Host ""
    
    $overview = Invoke-RestMethod -Uri "http://localhost:8080/api/monitoring/overview"
    
    Write-Host "System Health: $($overview.health.status)" -ForegroundColor $(if ($overview.health.status -eq "HEALTHY") { "Green" } else { "Yellow" })
    Write-Host "Active Threads: $($overview.threads.active)/$($overview.threads.maximum) ($($overview.threads.utilization))"
    Write-Host "Queue Size: $($overview.queues.totalPending)/$($overview.queues.totalCapacity) ($($overview.queues.utilization))"
    Write-Host "Completed Tasks: $($overview.tasks.completedTotal)"
    Write-Host ""
    
    $pools = Invoke-RestMethod -Uri "http://localhost:8080/api/monitoring/pools"
    
    foreach ($pool in $pools.PSObject.Properties) {
        $name = $pool.Name
        $stats = $pool.Value
        
        Write-Host "[$($name.ToUpper())] " -NoNewline
        Write-Host "Active: $($stats.current.activeThreads)/$($stats.configuration.maxPoolSize) " -NoNewline
        Write-Host "Queue: $($stats.current.queueSize)/$($stats.configuration.queueCapacity) " -NoNewline
        Write-Host "Util: $($stats.metrics.threadUtilization)"
    }
    
    Write-Host ""
    Write-Host "Press Ctrl+C to stop..." -ForegroundColor Gray
    
    Start-Sleep -Seconds 5
}
```

**Bash:**
```bash
#!/bin/bash
while true; do
    clear
    echo "=== Thread Pool Monitor ==="
    echo ""
    
    curl -s http://localhost:8080/api/monitoring/overview | jq '.'
    
    echo ""
    echo "Press Ctrl+C to stop..."
    sleep 5
done
```

---

## 🔔 Alert Conditions

Set up alerts when:

1. **Thread Utilization > 90%**
   ```bash
   if threadUtilization > 90: send_alert("High thread usage")
   ```

2. **Queue Utilization > 80%**
   ```bash
   if queueUtilization > 80: send_alert("Queue nearly full")
   ```

3. **Pool Under Pressure**
   ```bash
   if underPressure == true: send_alert("Pool under pressure")
   ```

4. **Tasks Failing**
   ```bash
   if failureRate > 10%: send_alert("High failure rate")
   ```

---

## 📊 Performance Tuning Based on Metrics

### If `threadUtilization` consistently high:

**Increase max threads:**
```yaml
worker:
  pools:
    compute:
      max-size: 8  # Increased from 4
```

### If `queueUtilization` consistently high:

**Increase queue capacity:**
```yaml
worker:
  pools:
    io:
      queue-capacity: 200  # Increased from 100
```

### If `completedTasks` low but `activeThreads` high:

Tasks are slow. Consider:
- Optimizing task logic
- Increasing timeout
- Adding more workers

---

## 🎉 Quick Reference

| Endpoint | Purpose |
|----------|---------|
| `/api/monitoring/pools` | All pool stats |
| `/api/monitoring/pools/{type}` | Specific pool |
| `/api/monitoring/overview` | System summary |
| `/api/tasks/stats` | Task statistics |

**Poll every**: 5-10 seconds for real-time  
**Alert on**: utilization > 80%, underPressure = true  
**Log check**: Every 30 seconds automatically

---

**Now you have full visibility into your thread pools!** 🚀
