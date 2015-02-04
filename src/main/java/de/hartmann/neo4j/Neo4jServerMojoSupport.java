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

package de.hartmann.neo4j;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class Neo4jServerMojoSupport extends AbstractMojo {

    protected static final String ARTIFACT_NAME = "neo4j-community-";

    protected static final String BASE_URL = "http://dist.neo4j.org/" + ARTIFACT_NAME;

    @Parameter(required = true, property = "neo4j-server.downloadPrefix", defaultValue = "-unix.tar.gz")
    protected String urlSuffix;

    @Parameter(required = true, property = "neo4j-server.version")
    protected String version;

    @Parameter(required = true, property = "neo4j-server.directory",
            defaultValue = "${project.build.directory}/neo4j-server")
    protected String directory;

    @Parameter(required = true, property = "neo4j-server.port", defaultValue = "7575")
    protected String port;

    protected Path getServerLocation() {
        return Paths.get(directory, ARTIFACT_NAME + version);
    }
}
