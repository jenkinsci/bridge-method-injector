package com.infradna.tool.synthetic_method_injector;

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
        File index = new File(classesDirectory, "META-INF/annotations/" + WithSyntheticMethods.class.getName());
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
            throw new MojoExecutionException("Failed to process @WithSyntheticMethods",e);
        } finally {
            try {
                if (r!=null)    r.close();
            } catch (IOException _) {
            }
        }
    }
}
