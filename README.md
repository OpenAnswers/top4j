![Top4J Logo](/images/top4j-icon-panther-style.png)

Description
===========

Top4J is a lightweight, low overhead, production-ready performance analysis tool for the Java Runtime Environment. As the name suggests, it works a bit like the UNIX/Linux top command but at the JVM-level exposing key performance metrics from a Java runtime perspective.

Top4J has **two** modes of operation: **Remote Attach** command-line interface (CLI) and **Java Agent**.

The [Top4J Remote Attach](/README.md#run-top4j-via-cli-remote-attach) function is executed via the command line and attaches to a remote JVM process via the JDK tools [VirtualMachine API](https://docs.oracle.com/javase/8/docs/jdk/api/attach/spec/com/sun/tools/attach/VirtualMachine.html).

The [Top4J Java Agent](/README.md#run-top4j-as-a-java-agent) is invoked via the standard `java -javaagent` command line argument and runs as a background thread inside the target JVM.

See [Getting Started](/README.md#getting-started) section for more details.

Top4J is an open sourced component of the Open Answers [Panther](https://www.openanswers.co.uk/products/panther) suite of monitoring applications.

Getting Started
===============

Prerequisites
-------------

The Top4J CLI jar **must** be run using a Java JDK install not a JRE.

**Supported JDKs:**

[Oracle JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html) 7, 8, 11

[OpenJDK](https://openjdk.java.net/) 7, 8, 11

There are no prerequisites for running the Top4J Java Agent (aside from downloading and installing the Top4J Java Agent jar file).

Run Top4J via CLI Remote Attach
-------------------------------

1. Download and install the [Top4J CLI jar](https://github.com/OpenAnswers/top4j/releases/download/1.1.0/top4j-cli-1.1.0.jar) within an appropriate location on the local file system.
1. Run Top4J CLI jar as follows....

```bash
java -jar top4j-cli-1.1.0.jar
```

**NOTE:** The Top4J CLI jar **must** be run as the target JVM process owner.

The Top4J CLI will auto-detect running, attachable JVMs and present a list of JVM processes to choose from....

```bash
java -jar top4j-cli-1.1.0.jar

0) org.tanukisoftware.wrapper.WrapperSimpleApp org.sonar.application.App [PID=8800]
1) org.sonar.server.app.WebServer C:\Users\ryan\AppData\Local\Temp\sq-process6076250228712711580properties [PID=9840]
2) io.top4j.javaagent.test.MultiThreadedTest 1000 100 1000 [PID=28756]
3) org.sonar.search.SearchServer C:\Users\ryan\AppData\Local\Temp\sq-process647990075843669775properties [PID=10876]
4) top4j-cli-1.1.0.jar [PID=7884]

Please select a JVM number between 0 and 4:
```

....simply select the JVM number you want to profile and Top4J will do the rest.

Here's a screenshot of Top4J in action....

![Top4J Top Threads Screenshot](/images/top4j-top-threads-screenshot.png)

Alternatively, you can specify the target JVM PID on the command line as follows....

```bash
java -jar top4j-cli-1.1.0.jar -p 12345
```

See the [Top4J Command Line Interface](/docs/COMMAND_LINE_INTERFACE.md) documentation for the full set of command line options supported or use the "-h" command line option, e.g. `java -jar top4j-cli-1.1.0.jar -h`.

Some additional CLI screenshots can be found [here](/docs/SCREENSHOTS.md).

Run Top4J as a Java Agent
-------------------------

1. Download and install the [Top4J Java Agent jar](https://github.com/OpenAnswers/top4j/releases/download/1.1.0/top4j-javaagent-1.1.0.jar) within an appropriate location on the local file system.
1. Add `-javaagent` command line argument to JVM startup args, e.g.

```bash
java -javaagent:<path-to-top4j-jar>/top4j-javaagent-1.1.0.jar <java-class-name>
```

The performance metrics gathered by the Top4J Java Agent are exposed via standard JMX MBean attributes. See the Top4J [JMX Interface](/docs/JMX_INTERFACE.md) for more details.

The Top4J Java Agent can also be configured to log performance metrics to the local file system. See the Top4J [Logging](/docs/LOGGING.md) documentation for more details.

The Top4J Java Agent is configured via a standard [Java properties file](https://docs.oracle.com/javase/tutorial/essential/environment/properties.html). See the Top4J [Configuration](/docs/CONFIGURATION.md) documentation for more details.

Build Top4J (via Maven)
-----------------------
Make sure JDK 8+ and Maven 3.x are installed on your machine, then....
```bash
git clone https://github.com/OpenAnswers/top4j.git
cd top4j
mvn clean package
```

Contributing
============

Please read [CONTRIBUTING.md](/CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

For general queries, please email the project team at <top4j@openanswers.co.uk>. If you think you've found a bug or have an enhancement request, please check the Top4J [issue management database](https://github.com/OpenAnswers/top4j/issues) for existing issues before submitting [a new issue](https://github.com/OpenAnswers/top4j/issues/new).

Versioning
==========

Top4J uses a standard [SemVer](http://semver.org/) based semantic versioning scheme. For the versions available, see the [tags on this repository](https://github.com/OpenAnswers/top4j/tags).

Authors
=======

* **Ryan Young** - *Project founder* - [Open Answers](https://github.com/OpenAnswers)

See also the list of [contributors](https://github.com/OpenAnswers/top4j/contributors) who have participated in this project.

License
=======

This project is licensed under the Apache License 2.0 - see the [LICENSE.txt](/LICENSE.txt) file for more details

Acknowledgments
===============

This project makes use of some excellent open source libraries including:

* [JMXTerm](https://docs.cyclopsgroup.org/jmxterm)
* [JLine](https://github.com/jline/jline2)
* [Apache Commons](https://commons.apache.org/)

