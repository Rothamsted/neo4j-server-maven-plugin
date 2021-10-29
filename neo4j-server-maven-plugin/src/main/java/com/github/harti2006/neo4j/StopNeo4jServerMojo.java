/*
 * Copyright 2015 André Hartmann (github.com/harti2006)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.harti2006.neo4j;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.maven.plugins.annotations.LifecyclePhase.POST_INTEGRATION_TEST;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "stop", defaultPhase = POST_INTEGRATION_TEST)
public class StopNeo4jServerMojo extends Neo4jServerMojoSupport {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final Log log = getLog();
        final Path serverLocation = getServerLocation();
        final String[] stopServerCmd = {
            serverLocation.resolve(Paths.get("bin", "neo4j")).toString(), "stop"};
        final File workingDir = serverLocation.toFile();

        try {
            final Process neo4jServerStopProcess = Runtime.getRuntime().exec(stopServerCmd, null, workingDir);
            try (final BufferedReader br = new BufferedReader(
                new InputStreamReader(neo4jServerStopProcess.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    log.info("NEO4J-SERVER > " + line);
                }

                if (neo4jServerStopProcess.waitFor(5, SECONDS) && neo4jServerStopProcess.exitValue() == 0) {
                    log.info("Stopped Neo4j server");
                } else {
                    throw new MojoExecutionException("Neo4j server did not stop properly");
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new MojoExecutionException("Could not stop Neo4j server", e);
        }
    }
}
