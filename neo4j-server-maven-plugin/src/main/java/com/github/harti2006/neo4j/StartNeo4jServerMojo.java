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

import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.apache.commons.io.FileUtils.copyURLToFile;
import static org.apache.maven.plugins.annotations.LifecyclePhase.PRE_INTEGRATION_TEST;
import static org.rauschig.jarchivelib.ArchiverFactory.createArchiver;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.PropertyUtils;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.exceptions.ServiceUnavailableException;
import org.rauschig.jarchivelib.Archiver;

@Mojo ( name = "start", defaultPhase = PRE_INTEGRATION_TEST )
public class StartNeo4jServerMojo extends Neo4jServerMojoSupport
{

	private static final int INITIAL_WAIT_MILLIS = 1500;
	private static final int WAIT_MILLIS = 5000;

	public void execute () throws MojoExecutionException
	{
		boolean isNewDB = installNeo4jServer ();
		configureNeo4jServer ();
		startNeo4jServer ( isNewDB );
	}

	/**
	 * 
	 * @return true if {@link #getServerLocation()} didn't already exist, a new installation was created
	 * from scratch and thus it requires to change the initial default provisional password.
	 * 
	 */
	private boolean installNeo4jServer () throws MojoExecutionException
	{
		final Path serverLocation = getServerLocation ();
		if ( exists ( serverLocation ) ) return false;
		
		final Log log = getLog ();
				
		if ( "@auto".equals (  urlSuffix ) )
		{
			String os = System.getProperty ( "os.name" );
			
			urlSuffix = os != null && os.toLowerCase ().contains ( "windows" )
				? "windows.zip"
				: "unix.tar.gz"; // Let's hope for the best instead
			
			urlSuffix = "-" + urlSuffix;
		}
		
		final Path downloadDestination = Paths.get ( 
			System.getProperty ( "java.io.tmpdir" ), "neo4j.server-maven-plugin",
			"downloads", "server", version, "neo4j.server" + urlSuffix 
		);

		if ( !exists ( downloadDestination ) )
		{
			try
			{
				final URL source = new URL ( BASE_URL + version + urlSuffix );
				createDirectories ( downloadDestination.getParent () );

				log.info ( format ( "Downloading Neo4j Server from %s", source ) );
				log.debug ( format ( "...and saving it to '%s'", downloadDestination ) );

				copyURLToFile ( source, downloadDestination.toFile () );
			}
			catch ( IOException e ) {
				throw new MojoExecutionException ( "Error downloading server artifact", e );
			}
		}

		try
		{
			getLog ().info ( format ( "Extracting %s", downloadDestination ) );
			
			// It comes in this two different flavours
			Archiver archiver = 
			urlSuffix.toLowerCase ().contains ( "unix" )
			  ? createArchiver ( "tar", "gz" )
			  : createArchiver ( "zip" );
			
			archiver.extract ( downloadDestination.toFile (), serverLocation.getParent ().toFile () );
		}
		catch ( IOException e ) {
			throw new MojoExecutionException ( "Error extracting server archive", e );
		}
		
		return true;
		
	} // installNeo4jServer

	
	private void configureNeo4jServer () throws MojoExecutionException
	{
		final Log log = getLog ();
		log.info ( "Server configuration" );
		
		final Path serverLocation = getServerLocation ();
		final Path serverPropertiesPath = serverLocation.resolve ( Paths.get ( "conf", "neo4j.conf" ) );

		try
		{
			Properties serverProperties = PropertyUtils.loadProperties ( serverPropertiesPath.toFile () );

			serverProperties.setProperty ( "dbms.connector.http.listen_address", "localhost:" + port );
			serverProperties.setProperty ( "dbms.connector.bolt.listen_address", "localhost:" + boltPort );
			serverProperties.setProperty ( "dbms.connector.https.enabled", "false" );

			serverProperties.store ( 
				newBufferedWriter ( serverPropertiesPath, TRUNCATE_EXISTING, WRITE ),
				"Generated by Neo4j Server Maven Plugin"
			);
		}
		catch ( IOException e ) {
			throw new MojoExecutionException ( "Could not configure Neo4j server", e );
		}
	}
	

	private void startNeo4jServer ( boolean isNewDB ) throws MojoExecutionException
	{

		final Log log = getLog ();
		final Path serverLocation = getServerLocation ();

		// Delete existing DB if required
		if ( deleteDb )
		{
			Path dataDir = serverLocation.resolve ( "data" );
			log.info ( "Deleting Database directory: '" + dataDir.toAbsolutePath ().toString () + "'" );
			FileUtils.deleteQuietly ( dataDir.toFile () );
		}
		
		if ( isNewDB || deleteDb ) {
			log.info ( "Changing the server initial password" );
			this.runNeo4jCommand ( "neo4j-admin", "set-initial-password", password );
		}
		
		log.info ( "Server start" );
		this.runNeo4jCommand ( "neo4j", "start" );
		checkServerReady ();
	}

	/**
	 * @see Neo4jServerMojoSupport#serverReadyAttempts
	 */
	private void checkServerReady () throws MojoExecutionException
	{
		try
		{
			Thread.sleep ( INITIAL_WAIT_MILLIS ); // It takes some time anyway

			for ( int attempts = 1; attempts <= serverReadyAttempts; attempts++ )
			{
				getLog ().debug ( "Trying to connect Neo4j, attempt " + attempts );

				try ( Driver driver = GraphDatabase.driver (
					"bolt://127.0.0.1:" + boltPort, AuthTokens.basic ( "neo4j", password ) 
				))
				{
					driver.verifyConnectivity ();
					getLog ().info ( "The server is running" );
					return;
				}
				catch ( ServiceUnavailableException ignored ) {
					Thread.sleep ( WAIT_MILLIS );
				}
			}
			throw new MojoExecutionException ( format ( 
				"Server doesn't result started after waiting %sms for its boot",
				INITIAL_WAIT_MILLIS + serverReadyAttempts * WAIT_MILLIS 
			));
		}
		catch ( InterruptedException ex ) {
			throw new MojoExecutionException ( "Error while connecting to the Neo4j server: " + ex.getMessage (), ex );
		}
	}

}
