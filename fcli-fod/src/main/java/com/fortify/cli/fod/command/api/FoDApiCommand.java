/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
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
package com.fortify.cli.fod.command.api;

import com.fortify.cli.common.command.api.APICommandMixin;
import com.fortify.cli.common.command.api.RootApiCommand;
import com.fortify.cli.common.command.util.annotation.RequiresProduct;
import com.fortify.cli.common.command.util.annotation.SubcommandOf;
import com.fortify.cli.common.config.product.Product;
import com.fortify.cli.common.config.product.Product.ProductIdentifiers;

import io.micronaut.core.annotation.ReflectiveAccess;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@ReflectiveAccess
@SubcommandOf(RootApiCommand.class)
@Command(name = ProductIdentifiers.FOD, description = "Invoke FoD REST API")
@RequiresProduct(Product.FOD)
public final class FoDApiCommand /*extends AbstractFoDUnirestRunnerCommand*/ {
	@Mixin private APICommandMixin apiCommand;
	
	/*
	@Override
	protected Void runWithUnirest(UnirestInstance unirest) {
		System.out.println(apiCommand.prepareRequest(unirest).asObject(ObjectNode.class).getBody().toPrettyString());
		return null;
	}
	*/
    
}