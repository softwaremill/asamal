# Asamal - lightweight JEE6 web framework

## What is it ?

Asamal is a Proof Of Concept web framework build completely on top of the JEE6 stack.

The aim is to build an (almost)completely functional lightweight, action-based web framework in a finite time.

### Modules

#### asamal

This is the actual framework

#### asamal-example

An example application, written simultaneously with the framework.

This is probably the best thing to look at if you would like to try it out yourself.

#### asamal-integration-tests

Integration tests

#### asamal-quickstart

Maven 3+ archetype, to quickstart your own project using asamal.

## User Guide

### Quick Start

The easiest to start right away is to use the provided maven archetype plugin.

Just run (you will need maven 3+)

 mvn archetype:generate -DarchetypeArtifactId=asamal-quickstart -DarchetypeGroupId=pl.softwaremill.asamal -DarchetypeVersion=1-SNAPSHOT -DarchetypeRepository=http://tools.softwaremill.pl/nexus/content/repositories/snapshots/

and then run mvn install - this will produce a war, which you will be able to deploy on JBoss 7.x (just copy to ${JBOSS_HOME}/standalone/deployments )

#### Controllers

To start writing you first action you will need a controller class that will hold your actions.

The class, except from being a public class, has to have two things

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

To do that, create a void method that will match the name of your action and annotate it with `pl.softwaremill.asamal.controller.annotation.Get` annotation.

```java
@Controller("admin")
public class AdminController extends ControllerBean {
	
	@Get
	public void index() {
		putInContext("var", "Hello Admin!");
	}
}
```

Now if you try to point your browser to `APP_URL/admin/index` Asamal will: 

* resolve the AdminController (look at the first **admin** element in the url)
* call the **index** method on it
* resolve **index.vm** located in `/view/admin` in your application WEB-APP and render it to the user

Wait, what **index.vm** are you talking about ? you might think...

Asamal uses [Apache Velocity](http://velocity.apache.org/) to render the web pages. Your velocity template might look like this:

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

##### POST

Writing POST actions is equally easy. Just annotate it with `pl.softwaremill.asamal.controller.annotation.Post`

The difference with POST actions is that by default they don't render any response. You might however choose to either redirect or include a view.

To perform a redirect call the `redirect(controller, view)` or `redirect(view)` on the ControllerBean.

If you choose to make an include call `include(view)`. 
Bare in mind that you cannot include views from different controllers and that 
including a view will not execute the action method for this view
- it will just render the desired velocity template, so if there are any variables
that are expected by this template, it might be a good idea to externalize setting of
them using some private method on the controller or such.

##### JSON

In Asamal it is very easy to produce JSON responses.

Just annotate the action with `pl.softwaremill.asamal.controller.annotation.Json` and make it return any java POJO.

```java
public class User {
	String name, lastName;
	
	// getters/setters etc. etc.
}

// And in our controller

@Json
public User user() {
	return new User("Tomek", "Szymanski");
}
```

browsing to `/{controller}/user` will render (with a proper content-type set to "application/json" of course!)
```
{name: "Tomek", lastName: "Szymanski"}
```

##### Autobinding & Validation

The ControllerBean has two methods available to perform autobinding

* doAutoBinding(String... parameterNames)
* doOptionalAutoBiding(String... parameterName)

Both will use parameter names as standard JavaBean paths and will try bind beans on the controller to those parameters.
The difference between them is that if thet non-optional version cannot find a specified parameter it will throw an exception, and prevent from rendering the page.

```java
@Controller("users")
public class UserController extends ControllerBean {
	
	@Inject 
	private EntityManager entityManager;

	User user = new User();
	
	@Post
	public void addNewUser() {
		doAutoBinding("user.name", "user.lastName");
		entityManager.persist(user);
	}
}
```

Asamal can also make use of the Java Validation API. You can run validation on any bean and it will automatically send error informations back to the user.

Imagine our User class from the previous example

```java
public class User {

	@NotNull
	@Size(min = 3, max = 30)
	private String name;
	
	@NotNull
	@Size(min = 3, max = 60)
	private String lastName;
	
}
```

and the action

```java
@Controller("users")
public class UserController extends ControllerBean {
	
	@Inject 
	private EntityManager entityManager;

	User user = new User();
	
	@Post
	public void addNewUser() {
		doAutoBinding("user.name", "user.lastName");
		
		if (validateBean("user", user)) {
			entityManager.persist(user);
		}
	}
}
```

Look at the `validateBean("user", user)` method - you need to pass the "user" prefix, 
because it is impossible to check in java the name of the passed variable.

#### View

##### Apache Velocity

As mentioned before Asamal uses velocity to serve the pages.

To learn the syntax it is best to go to the source, and read the [Velocity User Guide](http://velocity.apache.org/engine/releases/velocity-1.7/user-guide.html).

You will have access to all [VelocityTools](http://velocity.apache.org/tools/devel/summary.html)
on the web pages as well as few Asamal specific variables

<table>
<tr><th>Variable</th><th>Description</th></tr>
<tr>
	<td>$c</td>
	<td>The controller bean</td>
</tr>
<tr>
	<td>$a</td>
	<td>AsamalHelper (see below)</td>
</tr>
<tr>
	<td>$m</td>
	<td>The i18n messages object (see below)</td>
</tr>
<tr>
	<td>$pageTitle</td>
	<td>Simple string, easy to set from controller with setPageTitle(...) - might be removed in future</td>
</tr>
</table>

###### Templates

Templating language is very similar to the one you might now from JSF.

There is a master template file that specifies regions with the 
**\#includeRegion** directive, and then your child
pages define which template to use with the **\#layout** 
directive and contents of those regions with the **\#region** directive. 
This of course can be done many many times.

The master page has to be located in `/layout/` folder of you WEB-APP.

Simple example will show the idea

master.vm

```html
<html>
	<head><title>$pageTitle</title></head>
	<body>
		#includeRegion('content')
		
		<footer>#includeRegion('footer')</footer>
	</body>
</html>
```

And out action page index.vm

```html
#layout('master')

#region('content')
	<div>This is main content</div>
#end

#region('footer')
	This is footer
#end
```

Which will render

```html
<html>
	<head><title>$pageTitle</title></head>
	<body>
		<div>This is main content</div>
		
		<footer>This is footer</footer>
	</body>
</html>
```

Now your master.vm might have also specified \#layout('something') and so on.

###### Partials

Partials is an idea taken from the RoR framework.

Basically you might want to externalize some code snippet to be reused across different pages.

Partial file name always starts with an underscore (_) and has to be 
located in the `/view` folder in your WEB-APP
and is included with the **\#includePartal** directive.

Imagine this example

Out WEB-APP structure is as follows:

my-application.war
	/layout
		/master.vm
	/view
		/users
			/firstUserView.vm
			/_user_add.vm
		/home
			/secondUserView.vm


_user_add.vm

```html
<form method="post" action="$action">
	<label>User Name</label>
	<input type="text" name="user.name" value="$!user.name" />
	
	
	<label>User Last Name</label>
	<input type="text" name="user.lastName" value="$!user.lastName" />
	
	<input type="submit"/>
</form>
```

Now inside our pages

firstUserView.vm

```html
#layout('master')

#region('content')
	#includePartial('user_add.vm')
#end
```

secondUserView.vm

```html
#layout('master')

#region('content')
	#includePartial('/users/user_add.vm')
#end
```

The partial is used in two places now - if it is situated in the same directory
as the page, it will resolve just fine without any paths. If you are reusing path
that is global (used by other controllers) you have to provide the full path.

###### AsamalHelper

AsamalHelper is an utility class to generate action links in the application.

It is accessible via the `$a` variable from all you vm files.

|Method|Desciription|Method|Sample Usage|
|------|:----------:|:----:|----------:|
|**link(controller, view)**|The method will generate a GET link to given controller/action|GET|&lt;a href="$a.link('home','index')">Home</a&gt;|
|**formAction(controller,view)**|Generates form action link|POST|&lt;form method="post" action="$a.formAction('users','addUser')"&gt;|
|**formActionFormData(controller,view)**|Similar to formAction, but generates a link that will accept multipart forms - you have to use it when you are uploading a file|POST|&lt;form method="post" action="$a.formActionFormData('users','addUser')"&gt;|
|**jsonLink(controller,view)**|Generates a link to JSON-producing action. The action has to be annotated with @Json|GET|&lt;a href="$a.jsonLink('users', 'usersInJson')"&gt;Download in JSON&lt;/a&gt;|
|**jsLink(jsName)**|Generates a link to javascript file, which should be locate under /static/js in your WEB-APP|GET|&lt;script src="$a.jsLink("jquery-min-1.7.1.js")"></script&gt;
|**cssLink(cssName)**|Generates a link to CSS file, which should be locate under /static/css in your WEB-APP|GET|&lt;link href="$a.cssLink("bootstrap.css")" rel="stylesheet"&gt;|
|**imgLink(imgName)**|Generates a link to image, which should be locate under /static/img in your WEB-APP|GET|&lt;img src="$a.imgLink("logo.png")"/&gt;|
|**pdf(controller,view)|Generates a link to PDF version of the action|GET|See below|
</table>

##### Ajax

Asamal lets you rerender some elements on your page using their IDs.

Basically how it works is that using a javascript function that sits in the
autoincluded asamal.js, you create a link that
sends a set of form fields (or a whole form, or nothing) to a POST action, which 
then performs some logic, rerenders the page and returns using JSON parts of it
that you want to re-render. The DOM then gets updated on the client side.

Imagine you have a list of Users and you would like to update the list with ajax,
using some search criteria a user can provide.

searchUsers.vm

```html
<form>
	<label>Search Name:</label>
	<input type="text" name="searchName" id="searchName"/>
</form>

<a href="#" 
	onclick="ajaxPost('users, 'doSearch', 'searchName', 'userList')">
	Search
	</a>

<div id="userList">
	<ul>
		#foreach($user in $users)
			<li>$user</li> 
		#end
	</ul>
</div>
```

Note the way elementId and the div id is passed - it will be reused in the javascript call, 
since we can pass arrays here, we need to surround it with quotations, so javascript will
now that we are referring to the string, and not to the variable.

The controller

```java
@Controller("users")
public class UserController {
	
	@Inject
	UserService userService;

	@Post
	public void doSearch() {
		String searchName = getParameter("searchName");
		List<User> users = userService.searchByName(searchName);
		
		putInContext("users", users);
	}
}
```

Asamal will know what was the originating view and will render it, and return in
JSON only the contents of `<div id="userList"> ... </div>` which then will be 
updated on the client side.

Et voila!

### Interceptors

TBD

### Flash Scope

TBD

### Uploading files

TBD

### PDFs

Creating PDFs is very easy with Asamal. Just create any @Get action, with a corresponding
velocity template and access it adding to the url `/pdf` before the controller name,
or use the AsamalHelper to generate pdf link.

Asamal will use Flying Saucer and convert the html into pdf. To make the browser download
a desired file name, just follow the link by it.

**Attention** - this functionality is very POC-like. Even more then the rest ;-)

Example:

```html
<a href="$a.pdf('invoice','view')/10/invoice-10.pdf">Download Invoice</a>
```


### i18n and messages

Asamal has a good support for i18n. The bundle "messages.properties" has to be always available.

Inside your controller you can call a method on the controller called `getFromMessageBundle(String key, Object... params)` 
which will return formatted message that can optionally use parameters.

In your velocity templates instance of `pl.softwaremill.asamal.i18n.Messages` is available as **$m**.

You can access it in a longer, or shorter way.

If you need to just show the message without parameters, you can use map-like access

messages.properties

```
some.message = This is header
```

```html
<h2>$m['some.message']</h2>
```

If you need to pass some parameters you can use longer access

messages.properties

```
hello.user = My name is {1}, {0} {1}
```

```html
<div>$m.getFromMessageBundle('hello.user', $user.name, $user.lastName)</div>
```

### Developer Mode

Run your application server with -DASAMAL_DEV_DIR=*PATH_TO_YOUR_WEBAPP_DIR*.

If this is set, the velocity template files will be resolved from the specified directory,
not from you WEB-APP. This speeds up the development cycle very much, because
you do not need to redeploy your app every time you change something in the view.
