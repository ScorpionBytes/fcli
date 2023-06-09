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
package com.fortify.cli.common.session.helper;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fortify.cli.common.json.JsonHelper;
import com.fortify.cli.common.util.FcliHomeHelper;

import lombok.SneakyThrows;

public abstract class AbstractSessionHelper<T extends ISessionDescriptor> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractSessionHelper.class);
    private ObjectMapper objectMapper = JsonHelper.getObjectMapper();
    
    @SneakyThrows // TODO Do we want to use SneakyThrows?
    public final T get(String sessionName, boolean failIfUnavailable) {
        Path sessionDescriptorPath = getSessionDescriptorPath(sessionName);
        checkSessionExists(sessionName, failIfUnavailable);
        try {
            String sessionDescriptorJson = FcliHomeHelper.readSecuredFile(sessionDescriptorPath, failIfUnavailable);
            T sessionDescriptor = sessionDescriptorJson==null ? null : objectMapper.readValue(sessionDescriptorJson, getSessionDescriptorType());
            checkNonExpiredSessionAvailable(sessionName, failIfUnavailable, sessionDescriptor);
            return sessionDescriptor;
        } catch ( Exception e ) {
            FcliHomeHelper.deleteFile(sessionDescriptorPath, false);
            conditionalThrow(failIfUnavailable, ()->new IllegalStateException("Error reading session descriptor, please try logging in again", e));
            LOG.warn("Error reading session descriptor from {}; session descriptor has been deleted", sessionDescriptorPath);
            LOG.warn("Exception details: ", e);
            return null;
        }
    }

    @SneakyThrows // TODO Do we want to use SneakyThrows? 
    public final void save(String sessionName, T sessionDescriptor) {
        String sessionDescriptorJson = objectMapper.writeValueAsString(sessionDescriptor);
        FcliHomeHelper.saveSecuredFile(getSessionDescriptorPath(sessionName), sessionDescriptorJson, true);
    }
    
    @SneakyThrows // TODO Do we want to use SneakyThrows?
    public final void destroy(String sessionName) {
        FcliHomeHelper.deleteFile(getSessionDescriptorPath(sessionName), true);
    }
    
    public final boolean exists(String sessionName) {
        return FcliHomeHelper.isReadable(getSessionDescriptorPath(sessionName));
    }
    
    @SneakyThrows // TODO Do we want to use SneakyThrows?
    public final List<String> sessionNames() {
        Path path = getSessionsDataPath();
        if ( !FcliHomeHelper.exists(path) ) {
            return Collections.emptyList();
        }
        return FcliHomeHelper.listFilesInDir(path, true)
                .map(Path::getFileName)
                .map(Path::toString)
                .collect(Collectors.toList());
    }
    
    public final Collection<SessionSummary> sessionSummaries() {
        return sessionNames().stream()
                .map(this::getSessionSummary)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    public final ArrayNode sessionSummariesAsArrayNode() {
        return sessionSummaries().stream()
                .map(objectMapper::valueToTree)
                .map(JsonNode.class::cast) // TODO Not sure why this is necessary
                .collect(JsonHelper.arrayNodeCollector());
    }
    
    public ObjectNode sessionSummaryAsObjectNode(String sessionName) {
    	return objectMapper.valueToTree(getSessionSummary(sessionName));
    }
    
    private final Path getSessionsDataPath() {
        return FcliHomeHelper.getFcliStatePath().resolve("sessions").resolve(getType());
    }
    
    private final Path getSessionDescriptorPath(String sessionName) {
        return getSessionsDataPath().resolve(sessionName);
    }
    
    private SessionSummary getSessionSummary(String sessionName) {
        T sessionDescriptor = get(sessionName, false);
        return sessionDescriptor==null ? null : 
            SessionSummary.builder()
                .name(sessionName)
                .type(getType())
                .url(sessionDescriptor.getUrlDescriptor())
                .created(sessionDescriptor.getCreatedDate())
                .expires(sessionDescriptor.getExpiryDate())
                .build();
    }
    
    private void conditionalThrow(boolean throwException, Supplier<RuntimeException> exceptionSupplier) {
        if ( throwException ) { throw exceptionSupplier.get(); }
    }

    private void checkNonExpiredSessionAvailable(String sessionName, boolean failIfUnavailable, T sessionDescriptor) {
        if ( failIfUnavailable && isExpiredOrUnavailable(sessionDescriptor) )
        {
            throw new IllegalStateException(getType()+" session '"+sessionName+"' cannot be retrieved or has expired, please login again");
        }
    }

    private void checkSessionExists(String sessionName, boolean failIfUnavailable) {
        if ( failIfUnavailable && !exists(sessionName) ) {
            throw new IllegalStateException(getType()+" session '"+sessionName+"' not found, please login first");
        }
    }
    
    private boolean isExpiredOrUnavailable(T sessionDescriptor) {
        return sessionDescriptor==null ||
                (sessionDescriptor.getExpiryDate()!=null // We don't know whether session is expired if expiryDate is null
                && sessionDescriptor.getExpiryDate().before(new Date()));
    }
    
    public abstract String getType();
    
    protected abstract Class<T> getSessionDescriptorType();
}
