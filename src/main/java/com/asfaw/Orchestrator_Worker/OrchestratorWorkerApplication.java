package com.asfaw.Orchestrator_Worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Orchestrator-Worker Pattern implementation.
 * 
 * This application demonstrates a scalable task processing system where:
 * - Orchestrator receives and manages task submissions
 * - Tasks are queued and dispatched to specialized workers
 * - Workers execute tasks asynchronously using thread pools
 * - System supports retries, priorities, and status tracking
 * 
 * Architecture Components:
 * - Orchestrator: Coordinates task submission and status
 * - Task Queue: Priority-based in-memory queue (upgradable to Redis/RabbitMQ)
 * - Workers: Specialized processors (Compute, IO, AI)
 * - Task Scheduler: Continuously polls queue and dispatches tasks
 * 
 * @author TaskForge Team
 * @version 1.0.0
 */
@SpringBootApplication
public class OrchestratorWorkerApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrchestratorWorkerApplication.class, args);
		System.out.println("""
				
				╔═══════════════════════════════════════════════════════╗
				║   Orchestrator-Worker Pattern Application Started    ║
				║                                                       ║
				║   Server: http://localhost:8080                       ║
				║   API:    http://localhost:8080/api/tasks             ║
				║   Health: http://localhost:8080/api/tasks/health      ║
				║   Stats:  http://localhost:8080/api/tasks/stats       ║
				║                                                       ║
				║   Ready to process tasks! 🚀                          ║
				╚═══════════════════════════════════════════════════════╝
				""");
	}

}

