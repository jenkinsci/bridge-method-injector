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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author Kohsuke Kawaguchi
 * @goal process
 * @phase process-classes
 * @requiresDependencyResolution runtime
 */
public class ProcessMojo extends AbstractMojo {
    /**
     * The directory containing generated classes.
     *
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     */
    private File classesDirectory;

    public void execute() throws MojoExecutionException, MojoFailureException {
        File index = new File(classesDirectory, "META-INF/annotations/" + WithBridgeMethods.class.getName());
        if (!index.exists()) {
            getLog().debug("Skipping because there's no "+index);
            return;
        }

        BufferedReader r = null;
        try {
            r = new BufferedReader(new InputStreamReader(new FileInputStream(index),"UTF-8"));
            String line;
            while ((line=r.readLine())!=null) {
                File classFile = new File(classesDirectory,line.replace('.','/')+".class");
                getLog().debug("Processing "+line);
                new MethodInjector().handle(classFile);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to process @WithBridgeMethods",e);
        } finally {
            try {
                if (r!=null)    r.close();
            } catch (IOException _) {
            }
        }
    }
}
