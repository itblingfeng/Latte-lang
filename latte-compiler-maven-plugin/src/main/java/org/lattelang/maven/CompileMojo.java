/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 KuiGang Wang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.lattelang.maven;

import lt.lang.Utils;
import lt.repl.Compiler;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * compile latte source files.
 */
@Mojo(name = "compile")
@Execute(phase = LifecyclePhase.COMPILE, goal = "compile")
@SuppressWarnings("unused")
public class CompileMojo extends AbstractMojo {
        /**
         * The source directory
         */
        @Parameter(defaultValue = "${project.build.sourceDirectory}", required = true)
        private File sourceDirectory;
        /**
         * The output directory.
         */
        @Parameter(defaultValue = "${project.build.outputDirectory}", required = true)
        private File outputDirectory;

        /**
         * The project.
         */
        @Parameter(defaultValue = "${project}", required = true)
        private MavenProject mavenProject;

        @SuppressWarnings("unchecked")
        @Override
        public void execute() throws MojoExecutionException, MojoFailureException {
                // already run
                if (getPluginContext().containsKey("org::lattelang::maven::CompileMojo")) {
                        if (Boolean.TRUE.equals(getPluginContext().get("org::lattelang::maven::CompileMojo"))) {
                                return;
                        }
                }

                getPluginContext().put("org::lattelang::maven::CompileMojo", Boolean.TRUE);

                Set<Artifact> artifacts = mavenProject.getDependencyArtifacts();
                List<File> files = artifacts.stream().filter(a ->
                        !a.getScope().equals(Artifact.SCOPE_TEST)
                ).map(Artifact::getFile).collect(Collectors.toList());

                File latteSourceDirectory = new File(sourceDirectory.getParent() + "/latte");

                getLog().info("Compiling latte source files to " + outputDirectory);
                Compiler compiler = new Compiler(LoaderUtil.loadClassesIn(files, outputDirectory));

                compiler.config.fastFail = false;
                compiler.config.result.outputDir = outputDirectory;

                ClassLoader sourceLoader;
                try {
                        compiler.compile(Utils.filesInDirectory(latteSourceDirectory, ".*\\.lt", true));
                } catch (Exception e) {
                        getLog().info("Compilation failed!");
                        throw new MojoFailureException("Compilation failed!", e);
                }

                getLog().info("Compilation succeeded!");
        }
}
