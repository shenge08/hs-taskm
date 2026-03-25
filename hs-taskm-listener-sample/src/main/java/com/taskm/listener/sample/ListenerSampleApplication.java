package com.taskm.listener.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Sample Listener Application for TaskM.
 *
 * <p>This is a sample Spring Boot application that demonstrates how to create a listener
 * that receives task lifecycle events from strategies. The application exposes REST endpoints
 * that strategies can call using the TaskM SDK's ListenerClient.</p>
 *
 * <h3>Features:</h3>
 * <ul>
 *   <li>Receives task-started events</li>
 *   <li>Receives task-completed events with results</li>
 *   <li>Receives task-failed events with error messages</li>
 *   <li>Automatic logging using TaskM SDK Logger</li>
 *   <li>Health check endpoint</li>
 *   <li>OpenAPI/Swagger documentation</li>
 * </ul>
 *
 * <h3>Environment Variables:</h3>
 * <ul>
 *   <li>SERVER_PORT: Server port (default: 8080)</li>
 *   <li>LOG_LEVEL: Log level (DEBUG, INFO, WARNING, ERROR, default: INFO)</li>
 *   <li>LOG_FORMAT: Log format (text, json, default: text)</li>
 * </ul>
 *
 * <h3>Usage:</h3>
 * <pre>{@code
 * # Build the application
 * mvn clean package
 *
 * # Run the application
 * java -jar target/hs-taskm-listener-sample-1.0.0-SNAPSHOT.jar
 *
 * # Or with custom configuration
 * SERVER_PORT=9090 LOG_LEVEL=DEBUG java -jar target/hs-taskm-listener-sample-1.0.0-SNAPSHOT.jar
 * }</pre>
 *
 * @see com.taskm.listener.sample.controller.ListenerController
 * @since 1.0.0
 */
@SpringBootApplication
public class ListenerSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ListenerSampleApplication.class, args);
    }
}
