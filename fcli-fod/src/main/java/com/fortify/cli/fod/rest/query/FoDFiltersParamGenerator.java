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
package com.fortify.cli.fod.rest.query;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelNode;
import org.springframework.expression.spel.ast.OpAnd;
import org.springframework.expression.spel.ast.OpEQ;
import org.springframework.expression.spel.ast.OpOr;

import com.fortify.cli.common.rest.query.IServerSideQueryParamValueGenerator;
import com.fortify.cli.common.spring.expression.AbstractSpelTreeVisitor;
import com.fortify.cli.common.spring.expression.SpelNodeHelper;
import com.fortify.cli.common.util.JavaHelper;

public final class FoDFiltersParamGenerator implements IServerSideQueryParamValueGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(FoDFiltersParamGenerator.class);
    private final Map<String, String> filterNamesByPropertyPaths = new HashMap<>();
    
    public FoDFiltersParamGenerator add(String propertyPath, String filterName) {
        filterNamesByPropertyPaths.put(propertyPath, filterName);
        return this;
    }

    public FoDFiltersParamGenerator add(String propertyPath) {
        return add(propertyPath, propertyPath);
    }

    @Override
    public final String getServerSideQueryParamValue(Expression expression) {
        return new FoDFilterParamSpELTreeVisitor(expression).getQParamValue();
    }
    
    private final class FoDFilterParamSpELTreeVisitor extends AbstractSpelTreeVisitor {
        private StringBuffer filterParamValue = new StringBuffer();
        
        public FoDFilterParamSpELTreeVisitor(Expression expression) {
            process(expression);
        }

        public String getQParamValue() {
            return filterParamValue.isEmpty() ? null : filterParamValue.toString();
        }
        
        @Override
        protected void visit(SpelNode node) {
            if ( node instanceof OpAnd ) {
                LOG.trace("Processing OpAnd children: {}", node.toStringAST());
                visitChildren(node);
            } else if ( node instanceof OpEQ ) {
                LOG.trace("Processing OpEQ: {}", node.toStringAST());
                visitEQ((OpEQ)node);
            } else if ( node instanceof OpOr ) {
                visitOr((OpOr)node);
            }
        }
        
        private void visitOr(OpOr node) {
            String previousPropertyName = null;
            StringBuffer queryValue = new StringBuffer();
            for ( int i=0 ; i < node.getChildCount() ; i++ ) {
                // We only process OR expressions containing equals operators
                var child = JavaHelper.getAs(node.getChild(i), OpEQ.class);
                var propertyName = SpelNodeHelper.getQualifiedPropertyNameFromOperator(child);
                var literalString = SpelNodeHelper.getLiteralStringFromOperator(child);
                // We only support equals operators comparing simple property names with literals 
                if ( propertyName==null || literalString==null ) { return; }
                // We only support OR expressions for a single property name
                if ( previousPropertyName!=null && !previousPropertyName.equals(propertyName)) { return; }
                previousPropertyName = propertyName;
                queryValue.append(queryValue.isEmpty() ? literalString : "|"+literalString);
            }
            if ( !queryValue.isEmpty() ) {
                addEQ(previousPropertyName, queryValue.toString());
            }
        }

        private void visitEQ(OpEQ node) {
            var propertyName = SpelNodeHelper.getQualifiedPropertyNameFromOperator(node);
            var literalString = SpelNodeHelper.getLiteralStringFromOperator(node);
            LOG.trace("OpEQ property: {}, literal: {}", propertyName, literalString);
            if ( propertyName!=null && literalString!=null ) {
                addEQ(propertyName, literalString);
            }
        }
        
        private void addEQ(String propertyName, String value) {
            String qName = filterNamesByPropertyPaths.get(propertyName);
            if ( qName!=null ) {
                addQuery(String.format("%s:%s", qName, value));
            }
        }

        private void addQuery(String query) {
            filterParamValue.append(
                filterParamValue.isEmpty()
                    ? query
                    : ("+"+query)
            );
        }
    }
}