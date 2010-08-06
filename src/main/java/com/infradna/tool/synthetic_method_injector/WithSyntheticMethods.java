package com.infradna.tool.synthetic_method_injector;

import org.jvnet.hudson.annotation_indexer.Indexed;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Request that synthetic methods of the same name and same arguments be generated
 * with each specified type as the return type.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(CLASS)
@Target(METHOD)
@Documented
@Indexed
public @interface WithSyntheticMethods {
    /**
     * Specifies the return types. These types must be assignable to the actual
     * method return type.
     */
    Class[] value();
}
