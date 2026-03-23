package com.taskm.cli;

import com.taskm.cli.service.ApiClient;
import com.taskm.cli.service.OutputFormatter;
import com.taskm.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;

/**
 * Task query commands.
 */
@Component
@Command(
    name = "task",
    mixinStandardHelpOptions = true,
    description = "Query task information",
    subcommands = {
        TaskListCommand.class,
        TaskStatusCommand.class
    }
)
public class TaskCommand implements Runnable {

    @Autowired
    private ApiClient apiClient;

    @Override
    public void run() {
        picocli.CommandLine cmd = new picocli.CommandLine(this);
        cmd.usage(System.out);
    }
}

/**
 * List all tasks.
 */
@Component
@Command(
    name = "list",
    mixinStandardHelpOptions = true,
    description = "List all tasks"
)
class TaskListCommand implements Runnable {

    @Autowired
    private ApiClient apiClient;

    @Option(
        names = {"--status"},
        description = "Filter by status (e.g., PENDING, RUNNING, STOPPED, FAILED)"
    )
    private String status;

    @Option(
        names = {"--page"},
        description = "Page number (default: 0)",
        defaultValue = "0"
    )
    private int page;

    @Option(
        names = {"--size"},
        description = "Page size (default: 20, max: 100)",
        defaultValue = "20"
    )
    private int size;

    @Option(
        names = {"--json"},
        description = "Output in JSON format"
    )
    private boolean jsonOutput;

    @Override
    public void run() {
        try {
            List<Task> tasks = apiClient.getTasks(status, page, size);

            if (jsonOutput) {
                OutputFormatter.printJson(tasks);
            } else {
                OutputFormatter.printTasks(tasks);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}

/**
 * Get task status by ID.
 */
@Component
@Command(
    name = "status",
    mixinStandardHelpOptions = true,
    description = "Get task status and details"
)
class TaskStatusCommand implements Runnable {

    @Autowired
    private ApiClient apiClient;

    @Parameters(
        index = "0",
        description = "Task ID"
    )
    private Long id;

    @Option(
        names = {"--json"},
        description = "Output in JSON format"
    )
    private boolean jsonOutput;

    @Override
    public void run() {
        try {
            Task task = apiClient.getTask(id);

            if (jsonOutput) {
                OutputFormatter.printJson(task);
            } else {
                OutputFormatter.printTask(task);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
