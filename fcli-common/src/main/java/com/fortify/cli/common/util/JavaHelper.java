package com.fortify.cli.common.util;

/**
 * This class contains various Java utility methods that don't fit
 * in any of the other Helper classes.
 * 
 * @author rsenden
 *
 */
public final class JavaHelper {
    private JavaHelper() {}

    /**
     * Utility method for getting the given object as the given type,
     * returning null if the given object is not an instance of the
     * given type.
     * 
     * @param <T>
     * @param obj
     * @param asType
     * @return
     */
    @SuppressWarnings("unchecked")
    public static final <T> T getAs(Object obj, Class<T> asType) {
        if ( obj!=null && asType.isAssignableFrom(obj.getClass()) ) {
            return (T)obj;
        }
        return null;
    }
}
