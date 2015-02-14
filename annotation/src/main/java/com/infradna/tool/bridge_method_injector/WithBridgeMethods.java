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
 * <p>
 * In some cases, it's necessary to widen the return type of a method, but in a way that legacy
 * calls would still return instances of the original type. In this case, add
 * {@link #castRequired() castRequired=true} to the annotation. For example, if you have the
 * following code:
 * <pre>
 * &#64;WithBridgeMethods(value=FooSubType.class, castRequired=true)
 * public &lt;T extends Foo&gt; createFoo(Class&lt;T&gt; clazz) {
 *   return clazz.newInstance();
 * }
 * </pre>
 * <p>
 * The Maven mojo will insert the following bridge method:
 *
 * <pre>
 * public FooSubType createFoo(Class clazz) {
 *   return (FooSubType) createFoo(clazz); // invokeVirtual to createFoo that returns Foo
 * }
 * </pre>
 *
 * <p>
 * In extreme cases, this method can add a method whose return type has nothing to do
 * with the return type of the declared method. For example, if you have the following code:
 *
 * <pre>
 * &#64;WithBridgeMethods(value=String.class, adapterMethod="convert")
 * public URL getURL() {
 *   URL url = ....
 *   return url;
 * }
 *
 * private Object convert(URL url, Class targetType) { return url.toString(); }
 * </pre>
 *
 * <p>
 * The Maven mojo will insert the following bridge method:
 *
 * <pre>
 * public String getURL() {
 *   return (String)urlToString(getURL(),String.class);  // invokeVirtual to getURL that returns URL
 * }
 * </pre>
 *
 * <p>
 * The specified adapter method must be a method specified on the current class
 * or its ancestors. It cannot be a static method.
 *
 * @author Kohsuke Kawaguchi
 */
@Retention(CLASS)
@Target(METHOD)
@Documented
@Indexed
public @interface WithBridgeMethods {
    /**
     * Specifies the return types. These types must be assignable from the actual
     * method return type, or {@link #castRequired()} should be set to true.
     */
    Class<?>[] value();

    /**
     * Specifies whether the injected bridge methods should perform a cast prior to returning.  Only
     * set this to true when it is known that calls to the bridge methods will in fact return
     * objects assignable to {@linkplain #value() the bridge method return type}, even though
     * the declared method return type is not assignable to them.
     *
     * @since 1.4
     */
    boolean castRequired() default false;

    /**
     * Specifies the method to convert return value. This lets bridge methods to return
     * any types, even if it's unrelated to the return type of the declared method.
     *
     * @since 1.14
     */
    String adapterMethod() default "";
}
