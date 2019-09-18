Configuration
=============
The Top4J Java Agent is configured via a standard [Java properties file](https://docs.oracle.com/javase/tutorial/essential/environment/properties.html).

The Top4J Java Agent ships with a set of default properties configured via a file called `default-top4j.properties` which can be found within the Top4J source code [here](../top4j-javaagent/src/main/resources/default-top4j.properties).

The default Top4J properties can be overridden in one of two ways:

1. By providing a custom Top4J properties file called `top4j.properties` within the current working directory of the target JVM.
1. By adding additional arguments to the `java -javaagent` JVM command-line argument, for example....

```bash
java -javaagent:<path-to-top4j-jar>/top4j-javaagent-0.0.8.jar=stats.logger.enabled=true,top.thread.count=10 <java-class-name>
```

**NOTE:** The default location and name of the override `top4j.properties` file can be modified via the `-javaagent` command line argument `config.file`, for example....

```bash
java -javaagent:<path-to-top4j-jar>/top4j-javaagent-0.0.8.jar=config.file=/custom/config/location/top4j.properties <java-class-name>
```

The full set of configurable properties, along with a brief description of each property, can be accessed via the [default-top4j.properties file](../top4j-javaagent/src/main/resources/default-top4j.properties).

