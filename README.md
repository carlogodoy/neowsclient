# neowsclient

Start by cloning the project.

git clone https://github.com/carlogodoy/neowsclient.git

----------

An already built JAR can be found in release/ directory.

To Run:

~/dev/neowsclient/NeoWSClient$ java -jar release/NeoWSClient-1.0-SNAPSHOT.jar

----------

To build from source:

~/dev/neowsclient/NeoWSClient$ mvn clean ; mvn install

~/dev/neowsclient/NeoWSClient$ mvn package

~/dev/neowsclient/NeoWSClient$ java -jar target/NeoWSClient-1.0-SNAPSHOT.jar


----------

Project includes sources:
src/main/java/org/neo/App.js       - startup class with main method
src/main/java/org/neo/Neo.js        - Neo object
src/main/java/org/neo/NeoWSClient.js  - most of the working logic is in this class
src/main/java/org/neo/Utils.js    - utility class

---------

note: no JUnit tests were added at this time for the sake of simplicity.
The project is setup to start adding JUnit tests.

---------
