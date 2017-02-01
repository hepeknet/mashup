# I assume that Java binary is available in the system path (required version is Oracle JDK 1.8.0_60+
# I also assume that you properly configured first two config properties in src/main/resources/mashup.properties
# In case you do not have appropriate Java version in the system path please put absolute path below
# Before running this script     mvn clean package        must have been executed

/home/borisa/programs/jdk1.8.0_121/bin/java -jar ./target/mashup-0.0.1-SNAPSHOT.jar