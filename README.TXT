REQUIREMENTS

Oracle JDK 1.8.0_60 at least, the best is to use latest one available! If you use older version then
not everything will work because I rely on some latest feature to implement Json parsing without using any
3PPs.

CONFIGURATION

It is required to change first two configuration values in src/main/resources/mashup.properties!

Other values can be modified also but this is not needed in order to use application.

HOW TO RUN?

You have to have Maven 3.3.3 or later in order to build this software.
Maven must be available on the PATH.
Java must be available on the PATH.

Execute these two commands to run the application.

mvn clean package
./startMashup.sh

In case that shell scripts can not be executed on your system simply copy paste command from startMashup.sh into
your terminal (command line).

TEST

JUnit tests are executed automatically during Maven build.
Integration tests can be executed in the following way:

POTENTIAL IMPROVEMENTS

- Introduce 3PPs and reduce code doing plumbing things like: JSON parsing and conversion, caching
- Introduce proper monitoring and exposing metrics. Some 3PP would be best for this.
- Introduce proper system testing using Docker and some scripting language (like Groovy)
- Introduce some kind of failure injection for more robust system tests (like ChaosMonkey)
- Use SLF4J for logging for improved performance and richer features
- Probably move configuration to Zookeeper or Consul maybe - currently using property files is not flexible enough, especially for distributed systems

INTERNAL - TODO: 

- at least one integration test

