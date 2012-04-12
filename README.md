# Asamal - lightweight JEE6 web framework

## What is it ?

Asamal is a Proof Of Concept web framework build completely on top of the JEE6 stack.

The aim is to build an (almost)completely functional web framework in a finite time.

### Modules

#### asamal

This is the actual framework

#### asamal-example

An example application, written simultaneusly with the framework.

This is probably the best thing to look at if you would like to try it out yourself.

#### asamal-integration-tests

Integration tests

#### asamal-quickstart

Maven 3+ archetype, to kickstart your own project using asamal.

## User Guide

### Quick Start

The easiest to start right away is to use the provided maven archetype plugin.

Just run (you will need maven 3+)

 mvn archetype:generate -DarchetypeArtifactId=asamal-quickstart -DarchetypeGroupId=pl.softwaremill.asamal -DarchetypeVersion=1-SNAPSHOT -DarchetypeRepository=http://tools.softwaremill.pl/nexus/content/repositories/snapshots/

and then run mvn install - this will produce a war, which you will be able to deploy on JBoss 7.x (just copy to ${JBOSS_HOME}/standalone/deployments )

#### Controllers

```java
@Controller("admin")
public class AdminController extends ControllerBean {
	
	// actions
}
```

##### GET

##### JSON

##### POST

#### View

##### Apache Velocity

##### Custom Templating Language
