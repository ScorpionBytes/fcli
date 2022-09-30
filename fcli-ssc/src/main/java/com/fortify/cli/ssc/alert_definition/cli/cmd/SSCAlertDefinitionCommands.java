package com.fortify.cli.ssc.alert_definition.cli.cmd;

import picocli.CommandLine.Command;

@Command(
        name = "alert-definition",
        subcommands = {
                SSCAlertDefinitionGetCommand.class,
                SSCAlertDefinitionListCommand.class
        }
)
public class SSCAlertDefinitionCommands {
}