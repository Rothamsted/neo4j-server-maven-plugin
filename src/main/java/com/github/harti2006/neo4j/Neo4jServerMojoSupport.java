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

    /**
     * The web interface port, default is the same as Neo4J default.
     */
    @Parameter(required = true, property = "neo4j-server.port", defaultValue = "7474")
    protected String port;

    /**
     * The BOLT protocol port. This is what the programmatic clients use to access Neo4J. Default is
     * the same as Neo4J default.
     */
    @Parameter(required = true, property = "neo4j-server.boltPort", defaultValue = "7687")
    protected String boltPort;

    /**
     * The new Neo4J password. Neo4j creates a default neo4j/neo4j account of each new database, which is
     * set as expired and it can be used only to setup a new password. So the start procedure does that
     * with this parameter.
     */
    @Parameter(required = true, property = "neo4j-server.password", defaultValue = "test")
    protected String password;

    /**
     * Whether the neo4j DB must be cleaned or not before starting a new server with the start goal.
     * Typically, you want this to be false when you manually restart the test server, by invoking
     * this plug-in via command line, and you want it to be true when you configure a POM.
     */
    @Parameter(required = true, property = "neo4j-server.deleteDb", defaultValue = "false")
    protected boolean deleteDb;

    protected Path getServerLocation() {
        return Paths.get(directory, ARTIFACT_NAME + version);
    }
}
