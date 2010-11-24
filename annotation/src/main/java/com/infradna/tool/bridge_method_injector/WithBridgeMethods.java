/*
 * The MIT License
 *
 * Copyright (c) 2010, InfraDNA, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
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
     * method return type, or {@link #castRequired()} should be set to true.
     */
    Class<?>[] value();

    /**
     * Specifies whether the injected bridge methods should perform a cast prior to returning.  Only
     * set this to true when it is known that calls to the bridge methods will in fact return
     * types assignable to the actual method return type, even though declared return types are
     * not assignable to the actual method return type.
     */
    boolean castRequired() default false;
}
