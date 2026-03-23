package com.taskm.cli.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskm.entity.Strategy;
import com.taskm.entity.Task;

/**
 * Output formatter for CLI commands.
 */
public class OutputFormatter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Print object as JSON.
     */
    public static void printJson(Object obj) {
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
            System.out.println(json);
        } catch (Exception e) {
            System.err.println("Error formatting JSON: " + e.getMessage());
        }
    }

    /**
     * Print list of strategies as table.
     */
    public static void printStrategies(java.util.List<Strategy> strategies) {
        if (strategies == null || strategies.isEmpty()) {
            System.out.println("No strategies found.");
            return;
        }

        System.out.println("\nStrategies:");
        System.out.println("┌────┬─────────────┬──────────┬────────────────────────────────┬─────────────────────────────┐");
        System.out.println("│ ID │ Name        │ Language │ Description                    │ Created At                   │");
        System.out.println("├────┼─────────────┼──────────┼────────────────────────────────┼─────────────────────────────┤");

        for (Strategy s : strategies) {
            String name = truncate(s.getName(), 11);
            String lang = truncate(s.getLanguage(), 8);
            String desc = truncate(s.getDescription() != null ? s.getDescription() : "", 30);
            String created = s.getCreatedAt() != null ? s.getCreatedAt().toString().substring(0, 19) : "N/A";

            System.out.printf("│ %2d │ %-11s │ %-8s │ %-30s │ %-27s │%n",
                s.getId(), name, lang, desc, created);
        }

        System.out.println("└────┴─────────────┴──────────┴────────────────────────────────┴─────────────────────────────┘");
        System.out.println("Total: " + strategies.size() + " strategies");
    }

    /**
     * Print single strategy details.
     */
    public static void printStrategy(Strategy strategy) {
        System.out.println("\nStrategy Details:");
        System.out.println("────────────────────────────────────────────────────────────────────────────────");
        System.out.printf("ID:          %d%n", strategy.getId());
        System.out.printf("Name:        %s%n", strategy.getName());
        System.out.printf("Language:    %s%n", strategy.getLanguage());
        System.out.printf("Description: %s%n",
            strategy.getDescription() != null ? strategy.getDescription() : "N/A");
        System.out.printf("Code:        %s%n",
            strategy.getCode() != null ?
                (strategy.getCode().length() > 50 ?
                    strategy.getCode().substring(0, 50) + "..." : strategy.getCode())
                : "N/A");
        System.out.printf("Created At:  %s%n",
            strategy.getCreatedAt() != null ? strategy.getCreatedAt() : "N/A");
        System.out.printf("Updated At:  %s%n",
            strategy.getUpdatedAt() != null ? strategy.getUpdatedAt() : "N/A");
        System.out.println("────────────────────────────────────────────────────────────────────────────────");
    }

    /**
     * Print list of tasks as table.
     */
    public static void printTasks(java.util.List<Task> tasks) {
        if (tasks == null || tasks.isEmpty()) {
            System.out.println("No tasks found.");
            return;
        }

        System.out.println("\nTasks:");
        System.out.println("┌────┬────────┬─────────┬────────────┬─────────────┬─────────────────────────────┐");
        System.out.println("│ ID │ Status │ Strategy│ Started At  │ Completed At│ Created At                   │");
        System.out.println("├────┼────────┼─────────┼────────────┼─────────────┼─────────────────────────────┤");

        for (Task t : tasks) {
            String status = truncate(t.getStatus(), 6);
            String started = t.getStartedAt() != null ?
                t.getStartedAt().toString().substring(0, 16) : "N/A";
            String completed = t.getCompletedAt() != null ?
                t.getCompletedAt().toString().substring(0, 16) : "N/A";
            String created = t.getCreatedAt() != null ?
                t.getCreatedAt().toString().substring(0, 19) : "N/A";

            System.out.printf("│ %2d │ %-6s │ %7d │ %-10s │ %-11s │ %-27s │%n",
                t.getId(), status, t.getStrategyId(), started, completed, created);
        }

        System.out.println("└────┴────────┴─────────┴────────────┴─────────────┴─────────────────────────────┘");
        System.out.println("Total: " + tasks.size() + " tasks");
    }

    /**
     * Print single task details.
     */
    public static void printTask(Task task) {
        System.out.println("\nTask Details:");
        System.out.println("────────────────────────────────────────────────────────────────────────────────");
        System.out.printf("ID:           %d%n", task.getId());
        System.out.printf("Status:       %s%n", task.getStatus());
        System.out.printf("Strategy ID:  %d%n", task.getStrategyId());
        System.out.printf("Container ID: %s%n",
            task.getContainerId() != null ? task.getContainerId() : "N/A");
        System.out.printf("Created At:   %s%n",
            task.getCreatedAt() != null ? task.getCreatedAt() : "N/A");
        System.out.printf("Started At:   %s%n",
            task.getStartedAt() != null ? task.getStartedAt() : "N/A");
        System.out.printf("Completed At: %s%n",
            task.getCompletedAt() != null ? task.getCompletedAt() : "N/A");
        System.out.printf("Parameters:   %s%n",
            task.getParameters() != null ? task.getParameters().toString() : "N/A");
        System.out.println("────────────────────────────────────────────────────────────────────────────────");
    }

    /**
     * Truncate string to max length.
     */
    private static String truncate(String str, int maxLength) {
        if (str == null) {
            return "";
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 1) + "…";
    }
}
