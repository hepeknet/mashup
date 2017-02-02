All questions or problems please send me an email at borisha.zivkovic@gmail.com

## INTRO

One of restrictions given to me was to use JDK only but no other 3PP library for the main code.
This impacted a lot of my decisions.

I was given a choice between two programming languages Java and Scala.

I decided to use Java because I am much faster in it and given the fact that I could not use 3PP libraries I had
to be very fast and productive in order to implement all required features and also to have good code quality.

I did use some of Java 8 features (lambdas, CompletableFutures) - which are functionally very similar to what Scala
language provides.


## REQUIREMENTS

I developed this software using Oracle JDK jdk1.8.0_121
It also passed successful tests with Open JDK 8u111-jdk (as verified in Dockerfile below)

To run this software you need at least Oracle JDK 1.8.0_60, the best is to use latest one available - 1.8.0_100 or higher! 
If you use older version then not everything might work because I rely on some latest feature (functions parts of Javascript 
scripting engine shipped with JDK) to implement JSON parsing without using any 3PPs.

These are not specific to Oracle JDK, these features are standard features of any JDK implementation.

Usually I would not depend on latest JDK version but in order to avoid writing my own JSON parser I decided to do so.

In order to support older versions of Java I would have to write my own JSON parser, deal with ASTs and different corner cases 
and it that itself would easily take me few days to implement.

I chose to depend on JDK 1.8 (and not earlier version of Java) because it has functional features similar to Scala.
Depending on specific version of dependency is not a problem anymore in the world of microservices and containers (Docker)
where every microservice can choose its own dependencies without affecting others as long as APIs it exposes are stable
and versioned.

## CONFIGURATION

It is MANDATORY to change first two configuration values in file src/main/resources/mashup.properties

twitter_auth_key (Twitter consumer key)
twitter_auth_secret (Twitter consumer secret)

You have to do this BEFORE building and running software.

Other configuration values can also be modified but this is optional.

## HOW TO BUILD SOFTWARE?

Before building software make sure you configured it properly. See previous step.

You have to have Maven 3.3.3 or later in order to build this software. Maven must be available on the PATH. 

To build software execute (this will also execute JUnit tests):

mvn clean package

## HOW TO RUN SOFTWARE?

To run this software you first have to build it. See previous step.

There are two ways to run this software:

1) Using Docker on Linux

If you do not want to install specific version of Java then you can run this inside Docker. Assumption is that you have
one of latest Docker version and Linux installed. This software was tested with Docker 1.12 and Ubuntu Linux.

Execute following commands (in the root of project):

./buildDockerImage.sh
./runDockerImage.sh

This will bring you to mashup app where you can enter keywords to be used for searching Github and Twitter APIs.

2) Not using Docker

Appropriate version of Java must be available on the PATH (see above for required Java version).
If Java is not on the PATH then please modify startMashup.sh script and put absolute path to correct Java binary there.

Execute this to run the application (in the root of project):

./startMashup.sh

If shell scripts can not be executed on your system (maybe you use Windows) simply copy paste command from 
startMashup.sh into your terminal (command line).

## TESTS

JUnit tests are executed automatically during Maven build. 
Ideally there should be much more JUnit tests but I was able to achieve only ~60% of coverage.
Few more hours of work should be done to add extra tests and maybe refactor a thing or two while writing tests.

We would also have to add integration tests and system tests. Those tests would test network failures, problems with DNS, problems with
slow responses from remote dependencies etc. This can be done by mocking dependencies using some kind of embedded Jetty etc.

We would also have to add stress tests, concurrency, stability and reliability tests - but this would make sense mostly if this was not 
single-user console only application, like it is today.

If, for example, this application exposed some RESTful APIs to others then we would have to treat it differently than how we treat it now.

## POTENTIAL IMPROVEMENTS

- Introduce 3PPs and reduce code doing plumbing things like: JSON parsing and conversion, caching, metrics, retries.
- Introduce proper monitoring and expose internal metrics. Some 3PPs would be useful to do this or maybe internal libraries in your company.
- Introduce proper system testing using Docker and some scripting language (like Groovy)
- Introduce some kind of failure injection for more robust system tests (like ChaosMonkey)
- Do local performance (and stress) testing and watch how JVM behaves (GC, hotspots etc). Try to optimize most obvious problems. We can use jvisualvm for this.
- Use SLF4J for logging for improved performance and richer feature set than what JUL is giving us. Again JUL is used because of limitations of this task

- Probably move configuration to Zookeeper or Consul maybe - currently using property files is not flexible enough, especially for distributed systems.
   If we decided to move configuration to centralized location then application should fetch its configuration when needed and not only at startup like
   it is doing today.

- If we decided to use Java only we could implement a lot of crosscutting concerns (like logging or exposing metrics) as annotation-based API so that
  it is easier to focus on business logic only. Either use 3PP for this or some internal libraries already available

## OTHER THINGS THAT SHOULD BE PART OF PRODUCTION READY SOFTWARE BUT I DID NOT MANAGE TO DO THEM

- Documentation, especially if we are exposing APIs remotely or exposing some client library. 
    This is currently not the case for this project but it could expand.
	If we are providing user library then API should be clearly separated (separate jar) from implementation jars.