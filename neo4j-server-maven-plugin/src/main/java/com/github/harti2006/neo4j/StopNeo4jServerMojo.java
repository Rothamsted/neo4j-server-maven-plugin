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

import static org.apache.maven.plugins.annotations.LifecyclePhase.POST_INTEGRATION_TEST;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo ( name = "stop", defaultPhase = POST_INTEGRATION_TEST )
public class StopNeo4jServerMojo extends Neo4jServerMojoSupport
{

	@Override
	public void execute () throws MojoExecutionException, MojoFailureException
	{
		this.runNeo4jCommand ( "neo4j", "stop" );
	}
}
