mvn -B archetype:generate -DarchetypeGroupId=org.apache.maven.archetypes -DgroupId=com.laogongshuo.es -DartifactId=elasticsearch
mvn dependency:resolve
#mvn clean package
#mvn exec:java -Dexec.mainClass="com.laogongshuo.es.App"
mvn clean compile assembly:single
java -jar target/elasticsearch-1.0-SNAPSHOT-jar-with-dependencies.jar
