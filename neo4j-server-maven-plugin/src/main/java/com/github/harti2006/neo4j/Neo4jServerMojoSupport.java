/*
 * Copyright 2015 André Hartmann (github.com/harti2006) Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License
 * at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.github.harti2006.neo4j;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Parameter;

public abstract class Neo4jServerMojoSupport extends AbstractMojo
{

	protected static final String ARTIFACT_NAME = "neo4j-community-";

	protected static final String BASE_URL = "http://dist.neo4j.org/" + ARTIFACT_NAME;

	/**
	 * 
	 * The suffix for the file name, as it is found on the Neo4j donwload server.
	 * 
	 * {@code @auto} causes the plug-in to select between -unix.tar.gz or -windows.zip, depending on the platform where
	 * it's executed.
	 * 
	 */
	@Parameter ( required = false, property = "neo4j.server.downloadSuffix", defaultValue = "@auto" )
	protected String urlSuffix;

	/**
	 * Defined in the knetminer-common POM.
	 */
	@Parameter ( required = true, property = "neo4j.server.version" )
	protected String version;

	@Parameter ( required = true, property = "neo4j.server.directory", defaultValue = "${project.build.directory}/neo4j.server" )
	protected String directory;

	/**
	 * The web interface port, default is the same as Neo4J default.
	 */
	@Parameter ( required = true, property = "neo4j.server.port", defaultValue = "7474" )
	protected String port;

	/**
	 * The BOLT protocol port. This is what the programmatic clients use to access Neo4J. Default is the same as Neo4J
	 * default.
	 */
	@Parameter ( required = true, property = "neo4j.server.boltPort", defaultValue = "7687" )
	protected String boltPort;

	/**
	 * The new Neo4J password. Neo4j creates a default neo4j/neo4j account of each new database, which is set as expired
	 * and it can be used only to setup a new password. So the start procedure does that with this parameter.
	 */
	@Parameter ( required = true, property = "neo4j.server.password", defaultValue = "test" )
	protected String password;

	/**
	 * Whether the neo4j DB must be cleaned or not before starting a new server with the start goal. Typically, you want
	 * this to be false when you manually restart the test server, by invoking this plug-in via command line, and you
	 * want it to be true when you configure a POM.
	 */
	@Parameter ( required = true, property = "neo4j.server.deleteDb", defaultValue = "false" )
	protected boolean deleteDb;

	/**
	 * Makes this number of attempts to wait for server ready conditions when the Neo4j server is started in
	 * {@link StartNeo4jServerMojo}. After the start command, we check the server by connecting it via BOLT. If that
	 * doesn't work, we start making a connection attempt every second, until we connect it successfully or we reach a
	 * number of attempts equal to this parameter. The time spent this way can be configured and adapted to your
	 * particular situation, e.g., in some busy servers Neo4j can take over 1 min to start.
	 */
	@Parameter ( required = true, property = "neo4j.server.serverReadyAttempts", defaultValue = "10" )
	protected int serverReadyAttempts = 10;

	protected Path getServerLocation ()
	{
		return Paths.get ( directory, ARTIFACT_NAME + version );
	}
	
	protected void runNeo4jCommand ( String command, String... params ) throws MojoExecutionException
	{
		try
		{
			Log log = getLog ();
			
			String os = System.getProperty ( "os.name" );
			boolean isWin = os != null && os.toLowerCase ().contains ( "windows" );
						
			Path serverLocation = getServerLocation ();
			String cmdStr = serverLocation.resolve ( Paths.get ( "bin", command ) ).toString ();
			if ( params == null ) params = new String[ 0 ];
			
			List<String> cmdFull = new LinkedList<> ();
			cmdFull.add ( cmdStr );
			for ( String p: params ) cmdFull.add ( p );
			
			if ( isWin ) {
				cmdFull.add ( 0, "cmd" );
				cmdFull.add ( 1, "/c" );
			}
			
			final File workingDir = serverLocation.toFile ();

			// I can't use get Runtime.getRuntime ().exec(), because this hasn't any option to 
			// redirect the std error. This is exactly the same thing that exec() does
			// (apart from the redirect).
			//
			final Process cmdProcess = new ProcessBuilder ( cmdFull.toArray ( new String [ cmdFull.size () ] ) )
        .directory ( workingDir )
        .redirectErrorStream ( true )
        .start ();
			
			try ( BufferedReader br = new BufferedReader ( new InputStreamReader ( cmdProcess.getInputStream () ) ) )
			{
				for ( String line; ( line = br.readLine () ) != null; )
					log.info ( "NEO4J SERVER > " + line );
			}

			if ( cmdProcess.waitFor ( 5, SECONDS ) && cmdProcess.exitValue () == 0 )
				log.info ( "Command '" + command + "' finished" );
			else
				throw new MojoExecutionException ( "Command '" + command + "' didn't work" );
		}
		catch ( IOException | InterruptedException ex ) {
			throw new MojoExecutionException ( 
				"Error while running the Neo4j command '" + command + "':" + ex.getMessage (), ex
			);
		}
	}	
}
