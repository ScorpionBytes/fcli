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
package com.fortify.cli.common.rest.cli.cmd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fortify.cli.common.output.cli.cmd.AbstractOutputCommand;
import com.fortify.cli.common.output.cli.cmd.IBaseRequestSupplier;
import com.fortify.cli.common.output.product.IProductHelperSupplier;
import com.fortify.cli.common.output.transform.IInputTransformer;
import com.fortify.cli.common.output.transform.IRecordTransformer;
import com.fortify.cli.common.rest.unirest.IUnirestInstanceSupplier;

import io.micronaut.core.util.StringUtils;
import kong.unirest.HttpRequest;
import kong.unirest.UnirestInstance;
import lombok.Getter;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

public abstract class AbstractRestCallCommand extends AbstractOutputCommand implements IBaseRequestSupplier, IProductHelperSupplier, IInputTransformer, IRecordTransformer {
    @Parameters(index = "0", arity = "1..1", descriptionKey = "api.uri") String uri;
    
    @Option(names = {"--request", "-X"}, required = false, defaultValue = "GET")
    @Getter private String httpMethod;
    
    @Option(names = {"--data", "-d"}, required = false)
    @Getter private String data; // TODO Add ability to read data from file
    
    @Option(names="--no-transform-input", negatable = true, defaultValue = "false") 
    private boolean noTransformInput;
    
    @Option(names="--no-transform-records", negatable = true, defaultValue = "false") 
    private boolean noTransformRecords;
    
    // TODO Add options for content-type, arbitrary headers, ...?
    
    @Override
    public HttpRequest<?> getBaseRequest() {
        if ( getProductHelper() instanceof IUnirestInstanceSupplier ) {
            UnirestInstance unirest = ((IUnirestInstanceSupplier)getProductHelper()).getUnirestInstance();
            return prepareRequest(unirest);
        }
        throw new RuntimeException("Class doesn't implement IUnirestInstanceSupplier: "+getProductHelper().getClass().getName());
    }
    
    @Override
    public boolean isSingular() {
        return false;
    }
    
    @Override
    public JsonNode transformInput(JsonNode input) {
        if ( !noTransformInput && getProductHelper() instanceof IInputTransformer ) {
            input = ((IInputTransformer)getProductHelper()).transformInput(input);
        }
        return input;
    }
    
    @Override
    public JsonNode transformRecord(JsonNode input) {
        if ( !noTransformRecords && getProductHelper() instanceof IRecordTransformer ) {
            input = ((IRecordTransformer)getProductHelper()).transformRecord(input);
        }
        return input;
    }
    
    protected final HttpRequest<?> prepareRequest(UnirestInstance unirest) {
        if ( StringUtils.isEmpty(uri) ) {
            throw new IllegalArgumentException("Uri must be specified");
        }
        var request = unirest.request(httpMethod, uri);
        // TODO Add Content-Type & accept headers
        return data==null ? request : request.body(data);
    }
    
}
