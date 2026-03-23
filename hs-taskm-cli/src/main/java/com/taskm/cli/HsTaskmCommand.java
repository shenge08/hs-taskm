package com.taskm.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import org.springframework.stereotype.Component;

/**
 * Main command for HS-TASKM CLI tool.
 */
@Component
@Command(
    name = "hs-taskm",
    mixinStandardHelpOptions = true,
    version = "hs-taskm CLI 1.0.0-SNAPSHOT",
    description = "Command-line interface for HS-TASKM multi-language strategy execution platform",
    subcommands = {
        StrategyCommand.class,
        TaskCommand.class
    }
)
public class HsTaskmCommand implements Runnable {

    @Option(
        names = {"-v", "--verbose"},
        description = "Verbose output"
    )
    private boolean verbose;

    @Option(
        names = {"--json"},
        description = "Output in JSON format"
    )
    private boolean jsonOutput;

    @Override
    public void run() {
        // Show help by default
        CommandLine cmd = new CommandLine(this);
        cmd.usage(System.out);
    }

    public boolean isVerbose() {
        return verbose;
    }

    public boolean isJsonOutput() {
        return jsonOutput;
    }
}
