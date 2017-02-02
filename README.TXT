INTRO

One of limitations given to me was to use JDK only but no other 3PP library for the main code.

I was given a choice between Java and Scala.

I decided to use Java because I am much faster in it and given the fact that I could not use 3PP libraries I had
to be very fast and productive in order to implement all required features and also to have good code quality.

I did use some of Java 8 features (lambdas, CompletableFutures) - which are functionally very similar to what Scala
language provides.


REQUIREMENTS

Oracle JDK 1.8.0_60 at least, the best is to use latest one available - 1.8.0_100 or higher! If you use older version then
not everything might work because I rely on some latest feature (parts of Javascript scripting engine shipped with JDK) 
to implement Json parsing without using any 3PPs.

These are not specific to Oracle JDK, these features are standard JDK features. This software was tested with OpenJDK and it works.

Another option would be for me to write my own JSON parser, deal with ASTs and different corner cases and it would easily
require few days just to do that.

Usually I would not depend on latest JVM version but in order to avoid writing my own JSON parser I decided to do so.

I chose to depend on JDK 1.8 because it has functional features similar to Scala.
Depending on specific version of dependency is not a problem anymore in the world of microservices and containers (Docker)
where every microservice can choose its own dependencies without affecting others as long as APIs it exposes are stable
and versioned.

CONFIGURATION

It is MANDATORY to change first two configuration values in file src/main/resources/mashup.properties

Other configuration values can also be modified but this is optional.

HOW TO RUN?

You have to have Maven 3.3.3 or later in order to build this software.
Maven must be available on the PATH.
Java must be available on the PATH. If not then please modify startMashup.sh script and put absolute path to Java binary there.

Execute these two commands to build and run the application:

mvn clean package (this will also execute JUnit tests)

./startMashup.sh

If shell scripts can not be executed on your system (maybe you use Windows) simply copy paste command from 
startMashup.sh into your terminal (command line).

TESTS

JUnit tests are executed automatically during Maven build. 
Ideally there should be much more JUnit tests but I was able to achieve only ~60% of coverage.
Few more hours of work should be done to add extra tests and maybe refactor a thing or two while doing that.

We would have to add integration tests and system tests. Those tests would test network failures, problems with DNS, problems with
slow responses from remote dependencies etc. This can be done by mocking dependencies using some kind of embedded Jetty etc.

We would have to add stress tests and reliability tests - but this would make sense only if this was not console only app, like it is today.
If this application exposed some RESTful APIs to others then we would have to test its robustness further more, test deployment etc.

POTENTIAL IMPROVEMENTS

- Introduce 3PPs and reduce code doing plumbing things like: JSON parsing and conversion, caching, metrics, retries.
- Introduce proper monitoring and expose internal metrics. Some 3PPs would be useful to do this or maybe internal libraries in your company.
- Introduce proper system testing using Docker and some scripting language (like Groovy)
- Introduce some kind of failure injection for more robust system tests (like ChaosMonkey)
- Do local performance testing and watch how JVM behaves (GC, hotspots etc). Try to optimize most obvious problems.
- Use SLF4J for logging for improved performance and richer feature set than what JUL is giving us. Again JUL is used because of limitations of this task
- Probably move configuration to Zookeeper or Consul maybe - currently using property files is not flexible enough, especially for distributed systems.
   If we decided to move configuration to centralized location then application should fetch its configuration when needed and not only at startup like
   it is doing today.
- If we decided to use Java only we could implement a lot of crosscutting concerns (like logging or exposing metrics) as annotation-based API so that
  it is easier to focus on business logic only. Either use 3PP for this or some internal libraries already available.

OTHER THINGS THAT SHOULD BE PART OF PRODUCTION READY SOFTWARE BUT I DID NOT MANAGE TO DO THEM

- Documentation, especially if we are exposing APIs remotely or exposing some client library. 
    This is currently not the case for this project but it could expand.
	If we are providing user library then API should be clearly separated (separate jar) from implementation jars.

