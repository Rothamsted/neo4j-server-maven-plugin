# Neo4j Server Maven Plugin

It may get hard to update the existing [neo4j-maven-plugin](https://github.com/rherschke/neo4j-maven-plugin)
and make it use the latest versions of Neo4j, because it depends on deprecated classes, which are very
likely to be moved to some internal packages in the future.

So I decided to write another small Maven plugin, that simply downloads the complete Neo4j server artifact,
and runs it, as a user would do it, using `./neo4j start`

Furthermore it provides an idiomatic way to configure the server using the plugin `<configuration>` section.

**WARNING**: Since version 2.0, this works with Neo4j version >= 4.x and Java 11.

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
        <version>SEE-THE-POM-OR-RELEASE</version>
        <configuration>
            <port>${neo4j.server.port}</port>
            <version>${neo4j.server.version}</version>
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

### Parameters

Have a look at the [`Neo4jServerMojoSupport`](src/main/java/com/github/harti2006/neo4j/Neo4jServerMojoSupport.java) class for details.
 
### Code Formatting

File formatting is verified by [EditorConfig](http://editorconfig.org/) 
during `mvn verify` step. Most errors can be fixed with
`mvn editorconfig:format` task.
 
### Releases

The release process is copied frome this [blog post](https://dracoblue.net/dev/uploading-snapshots-and-releases-to-maven-central-with-travis/):

* Snapshot releases to Maven Central are performed automatically by Travis CI on every push to `master`.
* Final releases to Maven Central are performed automatically by Travis CI after creating and pushing a git tag.


## Troubleshooting

### Windows
While we don't officially support Windows, we have done some tests with it and debugged the code to see 
the plug-in running at least once.  

Based on that, we understand you might need [(re)install the Neo4j service][10], before the Neo4j plug-in is able to start/stop the Neo4j server. The best way to do so should be:

1. Run a Maven build against a project that starts the plug-in (`mvn verify` or `mvn install`).
   Likely, the first time you'll get an error, but you'll also get the working Neo4j server under
   `<your-project>\target\neo4j.server\neo4j-community-4.4.1`.
1. `cd` under that directory and issue: `bin\neo4j install-service`
1. Then, try to start the Neo4j manually (`bin\neo4j start`). If you can reach the server via the browser    
   (eg, `http://localhost/7474`), now you should be able to build the project and get the Neo4j plug-in 
   working.
* If `install-service` doesn't work, try `update-service`, or the more brutal: `uninstall-service` + 
  `install-service`.
* Notice that Windows keeps popping up dialogs to confirm of all the service-related operations above, even
  after service installation and regular use of the plug-in. We have no idea if this can be disabled for 
  the Neo4j service only (it can for all the apps, but highly non recommended).
  
  
   
   
 

[10]: https://github.com/Rothamsted/neo4j-server-maven-plugin


