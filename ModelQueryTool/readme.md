# The ModelQueryTool
A 3D, feature based, shape retrieval system built in Java. Made by Pieter Michels for the master course Multimedia Retrieval (INFOMMR).

___
## Installation
In this section all the steps needed to run the tool are detailed.
I highly advise running the tool using an IDE.
These instructions will assume you have access to [IntelliJ](https://www.jetbrains.com/idea/) but any IDE of your preference should work.

### Java
The tool is confirmed to work with Java 21, which can be installed from [here](https://www.oracle.com/java/technologies/downloads/).
Make sure that Java version 21 is used by configuring it in your IDE.

### Libraries
In you IDE, add the following groups of libraries from the libraries folder.

Apache commons math, required for PCA alignment.
```
commons-math3-3.6.1.jar
```
The JOGL libaries.
```
jogamp-all-platforms\jar\gluegen-rt.jar
jogamp-all-platforms\jar\jogl-all.jar
```
Jackson, required for reading and writing to feature files.
```
jackson-core-2.15.2.jar
jackson-annotations-2.15.2.jar
jackson-databind-2.15.2.jar
jackson-core-2.15.2-sources.jar
```
Hnswlib, for the K-nearest neighbour search.
```
eclipse-collections-9.2.0.jar
eclipse-collections-api-9.2.0.jar
hnswlib-core-1.1.0.jar
eclipse-collections-9.2.0-sources.jar
eclipse-collections-api-9.2.0-sources.jar
hnswlib-core-1.1.0-sources.jar
```

### Python
The tool also requires a few python libraries, namely `pymeshlab` and `networkx`, both of which can be installed using `pip`.
Make sure you know which command is used to run python on your machine (It should be one of `py` or `python`).
This will be important when running the tool, which asks for a python handle.

### Running the tool
With all the libaries installed, the tool should run.
___
