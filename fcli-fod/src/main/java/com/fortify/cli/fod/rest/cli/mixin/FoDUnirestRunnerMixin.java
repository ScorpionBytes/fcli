package com.fortify.cli.fod.rest.cli.mixin;

import com.fortify.cli.common.http.proxy.helper.ProxyHelper;
import com.fortify.cli.common.rest.cli.mixin.AbstractUnirestRunnerWithSessionDataMixin;
import com.fortify.cli.common.rest.runner.config.UnirestJsonHeaderConfigurer;
import com.fortify.cli.common.rest.runner.config.UnirestUnexpectedHttpResponseConfigurer;
import com.fortify.cli.common.rest.runner.config.UnirestUrlConfigConfigurer;
import com.fortify.cli.common.util.FixInjection;
import com.fortify.cli.fod.oauth.helper.FoDOAuthHelper;
import com.fortify.cli.fod.session.manager.FoDSessionData;
import com.fortify.cli.fod.session.manager.FoDSessionDataManager;

import jakarta.inject.Inject;
import kong.unirest.UnirestInstance;
import lombok.Setter;

@FixInjection
public class FoDUnirestRunnerMixin extends AbstractUnirestRunnerWithSessionDataMixin<FoDSessionData> {
    @Setter(onMethod=@__({@Inject})) private FoDSessionDataManager sessionDataManager;
    @Setter(onMethod=@__({@Inject})) private FoDOAuthHelper oauthHelper;
    
    @Override
    protected final FoDSessionData getSessionData(String sessionName) {
        return sessionDataManager.get(sessionName, true);
    }

    @Override
    protected final void configure(UnirestInstance unirest, FoDSessionData sessionData) {
        UnirestUnexpectedHttpResponseConfigurer.configure(unirest);
        UnirestJsonHeaderConfigurer.configure(unirest);
        UnirestUrlConfigConfigurer.configure(unirest, sessionData.getUrlConfig());
        ProxyHelper.configureProxy(unirest, "fod", sessionData.getUrlConfig().getUrl());
        final String authHeader = String.format("Bearer %s", sessionData.getActiveBearerToken());
        unirest.config().addDefaultHeader("Authorization", authHeader);
    }
}
