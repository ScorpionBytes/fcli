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
package com.fortify.cli.fod.rest.cli.cmd;

import com.fortify.cli.common.rest.runner.IUnirestRunner;
import com.fortify.cli.common.util.FixInjection;
import com.fortify.cli.fod.rest.cli.mixin.FoDUnirestRunnerMixin;

import io.micronaut.core.annotation.ReflectiveAccess;
import kong.unirest.UnirestInstance;
import lombok.Getter;
import lombok.SneakyThrows;
import picocli.CommandLine.Mixin;

import java.util.List;

@ReflectiveAccess
public abstract class AbstractFoDUnirestRunnerCommand implements Runnable {
    @Getter @Mixin private FoDUnirestRunnerMixin unirestRunnerMixin;

    @Override @SneakyThrows
    public final void run() {
        // TODO Do we want to do anything with the results, like formatting it based on output options?
        //      Or do we let the actual implementation handle this?
        unirestRunnerMixin.run(this::run);
    }

    protected abstract Void run(UnirestInstance unirest);
    protected boolean missing(List<?> list) {
        return list == null || list.isEmpty();
    }

}