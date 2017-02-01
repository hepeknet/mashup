INTRO

I decided to use Java because I am much faster in it and given the fact that I can not use 3PP libraries I had
to be very fast and productive in order to implement all required features and also to have good code quality.

I did use some of Java 8 features (lambdas, CompletableFutures) - which are functionally very similar to Scala.


REQUIREMENTS

Oracle JDK 1.8.0_60 at least, the best is to use latest one available - 1.8.0_100 or higher! If you use older version then
not everything will work because I rely on some latest feature (parts of Javascript scripting engine shipped with JDK) 
to implement Json parsing without using any 3PPs.

If I did not do this I would have to write my own parser, deal with ASTs and different corner cases and it would easily
require few days just to do that.

Usually I would not depend on latest JVM version but in order to avoid writing my own JSON parser I decided to do so.
One of limitations given to me was to use JDK only but no other 3PP library.

I chose to depend on JDK 1.8 because it has functional features and also depending on specific version of dependency
is not a problem anymore in the world of microservices and containers (Docker).

CONFIGURATION

It is MANDATORY to change first two configuration values in src/main/resources/mashup.properties!

Other configuration values can also be modified but this is not needed in order to use application.

HOW TO RUN?

You have to have Maven 3.3.3 or later in order to build this software.
Maven must be available on the PATH.
Java must be available on the PATH. If not then please modify startMashup.sh script and put absolute path to Java binary there.

Execute these two commands to run the application.

mvn clean package

./startMashup.sh

In case that shell scripts can not be executed on your system (maybe you use Windows) simply copy paste command from 
startMashup.sh into your terminal (command line).

TESTS

JUnit tests are executed automatically during Maven build.

Integration tests can be executed in the following way:

- TODO

POTENTIAL IMPROVEMENTS

- Introduce 3PPs and reduce code doing plumbing things like: JSON parsing and conversion, caching, metrics, retries
- Introduce proper monitoring and exposing metrics. Some 3PP would be best for this.
- Introduce proper system testing using Docker and some scripting language (like Groovy)
- Introduce some kind of failure injection for more robust system tests (like ChaosMonkey)
- Use SLF4J for logging for improved performance and richer features
- Probably move configuration to Zookeeper or Consul maybe - currently using property files is not flexible enough, especially for distributed systems

OTHER THINGS THAT SHOULD BE PART OF PRODUCTION READY SOFTWARE

- Documentation, especially if we are exposing APIs remotely or exposing some client library. This is currently not the case for this project but it could expand.
	If we are providing user library then API should be clearly separated (separate jar) from implementation.

INTERNAL - TODO: 

- at least one integration test

