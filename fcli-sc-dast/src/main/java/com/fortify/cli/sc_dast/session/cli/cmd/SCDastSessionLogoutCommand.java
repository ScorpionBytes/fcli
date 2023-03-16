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
package com.fortify.cli.sc_dast.session.cli.cmd;

import com.fortify.cli.common.output.cli.mixin.BasicOutputHelperMixins;
import com.fortify.cli.common.session.cli.cmd.AbstractSessionLogoutCommand;
import com.fortify.cli.common.util.FixInjection;
import com.fortify.cli.sc_dast.session.cli.mixin.SCDastSessionLogoutOptions;
import com.fortify.cli.sc_dast.session.manager.SCDastSessionData;
import com.fortify.cli.sc_dast.session.manager.SCDastSessionDataManager;
import com.fortify.cli.ssc.token.helper.SSCTokenHelper;

import io.micronaut.core.annotation.ReflectiveAccess;
import jakarta.inject.Inject;
import lombok.Getter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = BasicOutputHelperMixins.Logout.CMD_NAME, sortOptions = false)
@FixInjection
public class SCDastSessionLogoutCommand extends AbstractSessionLogoutCommand<SCDastSessionData> {
    @Mixin @Getter private BasicOutputHelperMixins.Logout outputHelper;
    @Inject @ReflectiveAccess @Getter private SCDastSessionDataManager sessionDataManager;
    @Inject @ReflectiveAccess private SSCTokenHelper tokenHelper;
    @Mixin private SCDastSessionLogoutOptions logoutOptions;
    
    @Override
    protected void logout(String sessionName, SCDastSessionData sessionData) {
        sessionData.logout(tokenHelper, logoutOptions.getUserCredentialOptions());
    }
}
