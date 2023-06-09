package com.fortify.cli.common.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.util.ResourceBundle;

import picocli.CommandLine.Model.ArgSpec;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Model.Messages;

public class PicocliSpecHelper {
    public static final <T extends Annotation> T findAnnotation(CommandSpec commandSpec, Class<T> annotationType) {
        T annotation = null;
        while ( commandSpec!=null && annotation==null ) {
            Object cmd = commandSpec.userObject();
            annotation = cmd==null ? null : cmd.getClass().getAnnotation(annotationType);
            commandSpec = commandSpec.parent();
        }
        return annotation;
    }
    
    public static final <T extends Annotation> T getAnnotation(CommandSpec cmdSpec, Class<T> annotationType) {
        var userObject = cmdSpec==null ? null : cmdSpec.userObject();
        if ( userObject!=null ) {
            return userObject.getClass().getAnnotation(annotationType);
        }
        return null;
    }
    
    public static final <T extends Annotation> T getAnnotation(ArgSpec argSpec, Class<T> annotationType) {
        var userObject = argSpec==null ? null : argSpec.userObject();
        if ( userObject!=null && userObject instanceof AccessibleObject ) {
            return ((AccessibleObject) userObject).getAnnotation(annotationType);
        }
        return null;
    }
    
    public static final String getMessageString(CommandSpec commandSpec, String keySuffix) {
        Messages messages = getMessages(commandSpec);
        String value = null;
        while ( commandSpec!=null && value==null ) {
            String key = commandSpec.qualifiedName(".")+"."+keySuffix;
            value = messages.getString(key, null);
            commandSpec = commandSpec.parent();
        }
        // If value is still null, try without any prefix
        return value!=null ? value : messages.getString(keySuffix, null);
    }
    
    public static final String getRequiredMessageString(CommandSpec commandSpec, String keySuffix) {
        String result = getMessageString(commandSpec, keySuffix);
        if ( StringUtils.isBlank(result) ) {
            throw new RuntimeException("No resource bundle entry found for required key suffix: "+keySuffix);
        }
        return result;
    }
    
    /**
     * @param commandSpec {@link CommandSpec} instance for looking up a {@link ResourceBundle}
     * @return {@link Messages} instance for the given {@link CommandSpec}, 
     *         or null if {@link CommandSpec} doesn't have a {@link ResourceBundle}
     */
    public static final Messages getMessages(CommandSpec commandSpec) {
        ResourceBundle resourceBundle = commandSpec.resourceBundle();
        return resourceBundle==null ? null : new Messages(commandSpec, resourceBundle);
    }
}
