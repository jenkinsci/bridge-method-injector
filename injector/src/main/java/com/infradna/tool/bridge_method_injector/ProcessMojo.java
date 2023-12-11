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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.jvnet.hudson.annotation_indexer.Index;

/**
 * @author Kohsuke Kawaguchi
 */
@Mojo(
        name = "process",
        requiresDependencyResolution = ResolutionScope.RUNTIME,
        defaultPhase = LifecyclePhase.PROCESS_CLASSES,
        threadSafe = true)
public class ProcessMojo extends AbstractMojo {
    /**
     * The directory containing generated classes.
     */
    @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
    private File classesDirectory;

    @Override
    @SuppressFBWarnings(
            value = {"DP_CREATE_CLASSLOADER_INSIDE_DO_PRIVILEGED", "PATH_TRAVERSAL_IN"},
            justification = "irrelevant without SecurityManager; user-provided value for running the program")
    public void execute() throws MojoExecutionException {
        try {
            for (String line : Index.listClassNames(
                    WithBridgeMethods.class,
                    new URLClassLoader(
                            new URL[] {classesDirectory.toURI().toURL()},
                            ClassLoader.getSystemClassLoader().getParent()))) {
                File classFile = new File(classesDirectory, line.replace('.', '/') + ".class");
                getLog().debug("Processing " + line);
                new MethodInjector().handle(classFile);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to process @WithBridgeMethods", e);
        }
    }
}
