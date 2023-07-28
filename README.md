# Dependency Tracker Maven Plugin
A maven plugin that tracks project dependencies **excluding** transitive dependencies and then writes them to a file as an 
artifact in the build directory.

By default, the plugin executes at the `compile` phase and is the earliest phase at which it can be configured to run 
to discover dependencies, for packaging types where compile phase might not apply, usually `prepare-package` or 
`packaging` should be good to use otherwise you will have to determine the appropriate one.

The generated dependency report artifact can be found in the build directory(`target`) and will have a name ending with 
`-dependencies.txt` i.e. `${project.build.finalName}-dependencies.txt`

The contents of the generated artifact file are interpreted as, each line is a key value pair separated by the equals 
sign for each dependency where the key is of the form `groupId:artifactId:type:version` or 
`groupId:artifactId:type:classifier:version` for dependencies with a classifier, then the value is the SHA-1 hash for 
the actual dependency file e.g. a jar file, the entries are always sorted by their keys alphabetically.

Below is an example of the artifact file's contents for a project with only 3 dependencies
```
org.slf4j:slf4j-api:jar:2.0.6=88c40d8b4f33326f19a7d3c0aaf2c7e8721d4953
org.slf4j:slf4j-api:test-jar:tests:2.0.6=88c40d8b4f33326f19a7d3c0aaf2c7e8721d4953
org.slf4j:slf4j-nop:jar:1.0.0-SNAPSHOT=bd0a88459dd8c99bb9d2474965c1aa36c7d66fb
```

**ATTENTION!!**

It's highly recommended to run your builds with `update-snapshots` flag set to true, that way the latest snapshot 
dependency builds are downloaded and used to generate the dependency report artifact. For builds that run in a 'clean' 
environment everytime e.g. on a CI server where a new container is used for every build then this might not be necessary.

# Usage

## In A Maven Project

Add the configuration below to your project POM file
```
<plugin>
    <groupId>net.mekomsolutions.maven.plugin</groupId>
    <artifactId>dependency-tracker-maven-plugin</artifactId>
    <version>${pluginVersion}</version>
    <executions>
        <execution>
            <goals>
                <goal>track</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

Replace `${pluginVersion}` with an actual plugin version, and then build your project.

## Command Line

Example command to run from the root of your project
```
mvn clean compile net.mekomsolutions.maven.plugin:dependency-tracker-maven-plugin:track -U
```

Note that we set the `U` flag which forces snapshot updates, you can replace the `compile` goal with any other 
appropriate goal depending on your packaging type.
