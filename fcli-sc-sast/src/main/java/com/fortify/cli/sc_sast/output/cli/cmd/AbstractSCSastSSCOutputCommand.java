package com.fortify.cli.sc_sast.output.cli.cmd;

import com.fortify.cli.common.output.cli.cmd.unirest.AbstractUnirestOutputCommand;
import com.fortify.cli.sc_sast.rest.cli.mixin.SCSastControllerUnirestRunnerMixin;

import lombok.Getter;
import picocli.CommandLine.Mixin;

public abstract class AbstractSCSastSSCOutputCommand extends AbstractUnirestOutputCommand {
    @Getter @Mixin SCSastControllerUnirestRunnerMixin unirestRunner;

    /*
    public final <R> R runOnSSC(Function<UnirestInstance, R> f) {
        return runOnSSC((u,d)->f.apply(u));
    }
    
    public final <R> R runOnController(Function<UnirestInstance, R> f) {
        return runOnController((u,d)->f.apply(u));
    }
    
    public final <R> R runOnSSC(BiFunction<UnirestInstance, SCSastSessionData, R> f) {
        return unirestRunner.runOnSSC(f);
    }
    
    public final <R> R runOnController(BiFunction<UnirestInstance, SCSastSessionData, R> f) {
        return unirestRunner.runOnController(f);
    }
    */
}
