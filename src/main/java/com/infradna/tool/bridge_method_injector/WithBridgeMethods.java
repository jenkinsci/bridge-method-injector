package com.infradna.tool.bridge_method_injector;

import org.jvnet.hudson.annotation_indexer.Indexed;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Request that bridge methods of the same name and same arguments be generated
 * with each specified type as the return type. This helps you maintain binary compatibility
 * as you evolve your classes.
 *
 * <p>
 * For example, if you have the following code:
 *
 * <pre>
 * &#64;WithBridgeMethods(Foo.class)
 * public FooSubType getFoo() { ... }
 * </pre>
 *
 * <p>
 * The Maven mojo will insert the following bridge method:
 *
 * <pre>
 * public Foo getFoo() {
 *     return getFoo(); // invokevirtual to getFoo() that returns FooSubType
 * }
 * </pre>
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(CLASS)
@Target(METHOD)
@Documented
@Indexed
public @interface WithBridgeMethods {
    /**
     * Specifies the return types. These types must be assignable to the actual
     * method return type.
     */
    Class[] value();
}
