INTRO

I decided to use Java because I am much faster in it and given the fact that I can not use 3PP libraries I had
to be very fast and productive in order to implement all features but also to have good code quality.

I did use some of Java 8 features (lambdas, CompletableFutures) - which are functionally very similar to Scala.


REQUIREMENTS

Oracle JDK 1.8.0_60 at least, the best is to use latest one available - 1.8.0_100 or higher! If you use older version then
not everything will work because I rely on some latest feature to implement Json parsing without using any
3PPs.

Usually I would not depend on latest JVM version but in order to avoid writing my own JSON parser I decided to do so.
One of limitations given to me was to use JDK only but no other 3PP library.

CONFIGURATION

It is required to change first two configuration values in src/main/resources/mashup.properties!

Other values can be modified also but this is not needed in order to use application.

HOW TO RUN?

You have to have Maven 3.3.3 or later in order to build this software.
Maven must be available on the PATH.
Java must be available on the PATH. If not then please modify startMashup.sh script and put absolute path to Java binary there.

Execute these two commands to run the application.

mvn clean package
./startMashup.sh

In case that shell scripts can not be executed on your system (if you use Windows) simply copy paste command from 
startMashup.sh into your terminal (command line).

TESTS

JUnit tests are executed automatically during Maven build.
Integration tests can be executed in the following way:

POTENTIAL IMPROVEMENTS

- Introduce 3PPs and reduce code doing plumbing things like: JSON parsing and conversion, caching, metrics, retries
- Introduce proper monitoring and exposing metrics. Some 3PP would be best for this.
- Introduce proper system testing using Docker and some scripting language (like Groovy)
- Introduce some kind of failure injection for more robust system tests (like ChaosMonkey)
- Use SLF4J for logging for improved performance and richer features
- Probably move configuration to Zookeeper or Consul maybe - currently using property files is not flexible enough, especially for distributed systems

INTERNAL - TODO: 

- at least one integration test

