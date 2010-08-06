package com.infradna.tool.synthetic_method_injector;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;

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
        // TODO:
    }
}
