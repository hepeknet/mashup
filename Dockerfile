FROM openjdk:8u111-jdk

MAINTAINER Borisa Zivkovic

RUN mkdir -p /opt/test/services/
ADD target/*.jar /opt/test/services/
ADD startDockerMashup.sh /opt/test/services/
RUN chmod +x /opt/test/services/startDockerMashup.sh