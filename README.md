# Asamal - lightweight JEE6 web framework

## What is it ?

Asamal is a Proof Of Concept web framework build completely on top of the JEE6 stack.

The aim is to build an (almost)completely functional lightweigth, action-based web framework in a finite time.

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

To start writing you first acition you will need a controller class that will hold your actions.

The class, except from beeing a public class, has to have two things

* has to be annotated with `@Controller` annotation that will specify the controller mapping
* has to extend the `pl.softwaremill.asamal.controller.ControllerBean`

```java
@Controller("admin")
public class AdminController extends ControllerBean {
	// actions
}
```

Once you have that done, you can start writing actions!

##### GET

The simplest action is the HTTP GET method action. This means nothing else, that your action will be accessible via simple GET requests.

```java
@Controller("admin")
public class AdminController extends ControllerBean {
	
	@Get
	public void index() {
		putInContext("var", "Hello Admin!");
	}
}
```

Now if you try to point your browser to *APP_URL/admin/index* Asamal will: 

* resolve the AdminController (look at the first *admin* element in the url)
* call the *index* method on it
* resolve *index.vm* located in /view/admin in your application WAR and render it to the user

Wait, what **index.vm** are you talking about ? you might think...

Asamal uses (Apache Velocity)[http://velocity.apache.org/] to render the web pages. Your velocity template might look like this:

```html
<html>
	<head><title>Admin</title></head>
	<body>
		<h2>Welcome on the Admin page</h2>
		
		$var
	</body>
</html>
```

Notice the *$var* element - this is the variable that we have passed into context in our action.


##### JSON

##### POST

#### View

##### Apache Velocity

##### Custom Templating Language
