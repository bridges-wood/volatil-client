# volatil - Java Chat App (Client)

A multithreaded console chat application designed to demonstrate client-server and multithreading programming practices. Each message is ephemeral and anonymised, lasting only as long as the users that saw it are connected.

## Requirements

1. JRE 11 or above
2. Maven 3.6.3 or above

## Installation

Although this repository should be using the latest version of the core volatil package, to ensure you have the most up-to-date version you will need to do the following:

1. Download the latest version of the core volatil package [here]().
2. Run the following command:

```
mvn install:install-file \
-Dfile=core-{version}.jar \
-DgroupId=com.volatil \
-DartifactId=core \
-Dversion={version} \
-Dpackaging=jar \
-DlocalRepositoryPath=lib \;
```

## Getting Started

Execute `mvn package` and run the generated jar file with `java -jar target/client-{version}.jar`
