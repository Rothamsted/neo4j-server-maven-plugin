# Neo4j Server Maven Plugin

It may get hard to update the existing [neo4j-maven-plugin](https://github.com/rherschke/neo4j-maven-plugin)
and make it use the latest versions of Neo4j, because it depends on deprecated classes, which are very
likely to be moved to some internal packages in the future.

So I decided to write another small Maven plugin, that simply downloads the complete Neo4j server artifact,
and runs it, as a user would do it, using `./neo4j start`

Furthermore it provides an idiomatic way to configure the server using the plugin `<configuration>` section.

**This is a fork** made in the the [kNetMiner project](http://knetminer.rothamsted.ac.uk/), to upgrade the 
plug-in to the last Neo4J version and deploy on our [organisation repository](http://ondex.rothamsted.ac.uk/nexus/index.html).

## Building the project

    mvn clean install

## Usage

### start

    mvn neo4j-server:start

### stop

    mvn neo4j-server:stop

### Integration Testing

The start/stop goals bind by default to lifecycle phases pre- and post-integration-test:

    <plugin>
        <groupId>com.github.harti2006</groupId>
        <artifactId>neo4j-server-maven-plugin</artifactId>
        <version>1.0-SNAPSHOT</version>
        <configuration>
            <port>${neo4j-server.port}</port>
            <version>${neo4j-server.version}</version>
        </configuration>
        <executions>
            <execution>
                <id>start-neo4j-server</id>
                <goals>
                    <goal>start</goal>
                </goals>
            </execution>
            <execution>
                <id>stop-neo4j-server</id>
                <goals>
                    <goal>stop</goal>
                </goals>
            </execution>
        </executions>
    </plugin>

For an example, run

    cd integration-tests
    mvn clean verify

The server might take a while before going on line, so your tests might get connection refused errors. This
ant-based pause might help (must go after the neo4j plug-in declaration above):

```
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-antrun-plugin</artifactId>
	<version>1.8</version>
	<executions>
		<execution>
			<id>wait</id>
			<phase>pre-integration-test</phase>
			<configuration>
				<target>
					<echo message = "Waiting for Neo4j to boot" />
					<sleep seconds="3" />
				</target>
			</configuration>
			<goals>
				<goal>run</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```


### Releases

This project uses the [maven-release-plugin](http://maven.apache.org/maven-release/maven-release-plugin/) to
create new release versions. This happens in two steps:

    mvn clean release:prepare -Prelease

    mvn release:perform -Prelease
