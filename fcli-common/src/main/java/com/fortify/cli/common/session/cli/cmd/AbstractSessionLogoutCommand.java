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
package com.fortify.cli.common.session.cli.cmd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fortify.cli.common.output.transform.IActionCommandResultSupplier;
import com.fortify.cli.common.session.cli.mixin.SessionNameMixin;
import com.fortify.cli.common.session.helper.ISessionDescriptor;

import lombok.Getter;
import picocli.CommandLine.Mixin;

public abstract class AbstractSessionLogoutCommand<D extends ISessionDescriptor> extends AbstractSessionCommand<D> implements IActionCommandResultSupplier {
    @Getter @Mixin private SessionNameMixin.OptionalParameter sessionNameMixin;
    
    @Override
    public JsonNode getJsonNode() {
        String sessionName = sessionNameMixin.getSessionName();
        JsonNode result = null;
        var sessionHelper = getSessionHelper();
        if ( sessionHelper.exists(sessionName) ) {
        	result = sessionHelper.sessionSummaryAsObjectNode(sessionName);
            logout(sessionName, sessionHelper.get(sessionName, true));
            // TODO Optionally delete all variables
            getSessionHelper().destroy(sessionName);
        }
        return result;
    }
    
    @Override
    public String getActionCommandResult() {
    	return "TERMINATED";
    }
    
    @Override
    public boolean isSingular() {
    	return false;
    }
    
    protected abstract void logout(String sessionName, D sessionDescriptor);
}
