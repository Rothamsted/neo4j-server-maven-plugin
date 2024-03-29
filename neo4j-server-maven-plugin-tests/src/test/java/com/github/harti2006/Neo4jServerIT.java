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

package com.github.harti2006;

import org.junit.Test;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Neo4jServerIT
{		
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Test
	public void testNeo4jServerIsRunning() throws Exception
	{
		// We test the bolt server here, the web interface is not crucial for Maven builds.
		String boltPort = System.getProperty ( "neo4j.server.boltPort" );
		String pwd = System.getProperty ( "neo4j.server.password" );
		try (
			Driver driver = GraphDatabase.driver( "bolt://127.0.0.1:" + boltPort, AuthTokens.basic ( "neo4j", pwd ) );
			Session session = driver.session ();
		)
		{
			session.run ( "MATCH (n) RETURN COUNT(n)" );
			log.info ( "Neo4j Connected to {}", driver.toString () );
		}
	}
}
