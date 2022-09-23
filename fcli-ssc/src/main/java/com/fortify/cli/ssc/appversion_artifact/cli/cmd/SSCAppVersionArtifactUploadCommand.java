/*******************************************************************************
 * (c) Copyright 2021 Micro Focus or one of its affiliates
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.cli.ssc.appversion_artifact.cli.cmd;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;
import com.fortify.cli.common.json.JsonHelper;
import com.fortify.cli.common.output.cli.mixin.OutputConfig;
import com.fortify.cli.ssc.appversion.cli.mixin.SSCAppVersionResolverMixin;
import com.fortify.cli.ssc.appversion.helper.SSCAppVersionDescriptor;
import com.fortify.cli.ssc.appversion_artifact.helper.SSCAppVersionArtifactHelper;
import com.fortify.cli.ssc.rest.SSCUrls;
import com.fortify.cli.ssc.rest.cli.cmd.AbstractSSCTableOutputCommand;

import io.micronaut.core.annotation.ReflectiveAccess;
import kong.unirest.GetRequest;
import kong.unirest.HttpRequestWithBody;
import kong.unirest.UnirestInstance;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@ReflectiveAccess
@Command(name = "upload")
public class SSCAppVersionArtifactUploadCommand extends AbstractSSCTableOutputCommand {
    private static final int POLL_INTERVAL_SECONDS = SSCAppVersionArtifactHelper.DEFAULT_POLL_INTERVAL_SECONDS;
    
    @Mixin private SSCAppVersionResolverMixin.To parentResolver;
    @Parameters(arity="1") private String filePath;
    @ArgGroup(exclusive=false) private SSCAppVersionArtifactAutoApproveOptions autoApproveOptions = new SSCAppVersionArtifactAutoApproveOptions();

    @Option(names = {"-w", "--wait"}, defaultValue = "false", description = "Will wait for the artifact to finish processing and auto approve if needed.")
    private Boolean wait;
    
    @Option(names = {"-e", "--engine-type"}, description = "Engine type for the artifact being uploaded")
    private String engineType;

    @Option(names = {"-t", "--time-out"}, defaultValue="300")
    private int processingTimeOutSeconds;
    
    private static final class SSCAppVersionArtifactAutoApproveOptions {
        @Option(names = {"-a", "--auto-approve"}, defaultValue = "false", description = "Auto approves any uploaded artifact that needs approval.")
        private Boolean autoApprove;
        
        @Option(names = {"-m", "--message"}, defaultValue = "Auto-approved by fcli")
        private String message;
    }
    
    @Override
    protected GetRequest generateRequest(UnirestInstance unirest) {
        SSCAppVersionDescriptor av = parentResolver.getAppVersionDescriptor(unirest);
        HttpRequestWithBody request = unirest.post(SSCUrls.PROJECT_VERSION_ARTIFACTS(av.getVersionId()));
        if ( engineType!=null && !engineType.isBlank() ) {
            request.queryString("engineType", engineType);
        }
        JsonNode uploadResponse = request.multiPartContent()
                .field("file", new File(filePath))
                .asObject(JsonNode.class).getBody();
        
        String artifactId = JsonHelper.evaluateJsonPath(uploadResponse, "$.data.id", String.class);
        
        if (autoApproveOptions.autoApprove) {
            SSCAppVersionArtifactHelper.waitAndApprove(unirest, artifactId, autoApproveOptions.message, POLL_INTERVAL_SECONDS, processingTimeOutSeconds);
        } else if (wait ) {
            SSCAppVersionArtifactHelper.waitForNonProcessingState(unirest, artifactId, POLL_INTERVAL_SECONDS, processingTimeOutSeconds);
        }

        return unirest.get(SSCUrls.ARTIFACT(artifactId)).queryString("embed","scans");
    }
    
    @Override
    public OutputConfig getOutputOptionsWriterConfig() {
        return super.getOutputOptionsWriterConfig()
                .defaultColumns("id#$[*].scans[*].type:type#lastScanDate#uploadDate#status");
    }
}