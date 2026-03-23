package com.taskm.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import picocli.CommandLine;

/**
 * Spring Boot application for HS-TASKM CLI tool.
 */
@SpringBootApplication
public class HsTaskmCliApplication {

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(HsTaskmCliApplication.class, args);

        // Get main command from Spring context
        HsTaskmCommand command = context.getBean(HsTaskmCommand.class);

        // Execute command
        CommandLine cmd = new CommandLine(command);
        int exitCode = cmd.execute(args);

        System.exit(exitCode);
    }
}
