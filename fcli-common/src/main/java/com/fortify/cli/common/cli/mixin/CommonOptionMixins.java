package com.fortify.cli.common.cli.mixin;

import com.fortify.cli.common.util.PicocliSpecHelper;
import com.fortify.cli.common.util.StringUtils;

import lombok.Getter;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

public class CommonOptionMixins {
    private CommonOptionMixins() {}
    
    public static class OptionalDestinationFile {
        @Option(names = {"-f", "--dest"}, descriptionKey = "fcli.destination-file")
        @Getter private String destination;
    }
    
    public static class RequireConfirmation {
        @Mixin private CommandHelperMixin commandHelper;
        @Option(names = {"-y", "--confirm"}, defaultValue = "false")
        private boolean confirmed;
        
        public void checkConfirmed() {
            if (!confirmed) {
                CommandSpec spec = commandHelper.getCommandSpec();
                if ( System.console()==null ) {
                    throw new ParameterException(spec.commandLine(), "Missing option: Confirm operation with -y / --confirm (interactive prompt not available)");
                } else {
                    String expectedResponse = PicocliSpecHelper.getRequiredMessageString(spec, "expectedConfirmPromptResponse");
                    String response = System.console().readLine(getPrompt());
                    if ( response.equalsIgnoreCase(expectedResponse) ) {
                        return;
                    } else {
                        throw new IllegalStateException("Aborting: operation aborted by user");
                    }
                }
            }
        }
        
        private String getPrompt() {
            CommandSpec spec = commandHelper.getCommandSpec();
            String prompt = PicocliSpecHelper.getMessageString(spec, "confirmPrompt");
            if ( StringUtils.isBlank(prompt) ) {
                String[] descriptionLines = spec.optionsMap().get("-y").description();
                if ( descriptionLines==null || descriptionLines.length<1 ) {
                    throw new RuntimeException("No proper description found for generating prompt for --confirm option");
                }
                prompt = spec.optionsMap().get("-y").description()[0]+"?";
            }
            String promptOptions = PicocliSpecHelper.getRequiredMessageString(spec, "confirmPromptOptions");
            return String.format("%s (%s) ", prompt, promptOptions);
        }
    }
}
