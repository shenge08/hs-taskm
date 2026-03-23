package com.taskm.cli;

import com.taskm.cli.service.ApiClient;
import com.taskm.cli.service.OutputFormatter;
import com.taskm.entity.Strategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.List;

/**
 * Strategy query commands.
 */
@Component
@Command(
    name = "strategy",
    mixinStandardHelpOptions = true,
    description = "Query strategy information",
    subcommands = {
        StrategyListCommand.class,
        StrategyGetCommand.class
    }
)
public class StrategyCommand implements Runnable {

    @Autowired
    private ApiClient apiClient;

    @Override
    public void run() {
        CommandLine cmd = new CommandLine(this);
        cmd.usage(System.out);
    }
}

/**
 * List all strategies.
 */
@Component
@Command(
    name = "list",
    mixinStandardHelpOptions = true,
    description = "List all strategies"
)
class StrategyListCommand implements Runnable {

    @Autowired
    private ApiClient apiClient;

    @Option(
        names = {"--language"},
        description = "Filter by language (e.g., java, python, go)"
    )
    private String language;

    @Option(
        names = {"--json"},
        description = "Output in JSON format"
    )
    private boolean jsonOutput;

    @Override
    public void run() {
        try {
            List<Strategy> strategies = apiClient.getStrategies(language);

            if (jsonOutput) {
                OutputFormatter.printJson(strategies);
            } else {
                OutputFormatter.printStrategies(strategies);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}

/**
 * Get strategy details by ID.
 */
@Component
@Command(
    name = "get",
    mixinStandardHelpOptions = true,
    description = "Get strategy details"
)
class StrategyGetCommand implements Runnable {

    @Autowired
    private ApiClient apiClient;

    @Parameters(
        index = "0",
        description = "Strategy ID"
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
            Strategy strategy = apiClient.getStrategy(id);

            if (jsonOutput) {
                OutputFormatter.printJson(strategy);
            } else {
                OutputFormatter.printStrategy(strategy);
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}
