package pl.softwaremill.cdiexample.controller;

import pl.softwaremill.cdiexample.filters.AuthorizationFilter;
import pl.softwaremill.cdiexample.logic.auth.LoginBean;
import pl.softwaremill.cdiexample.model.Person;
import pl.softwaremill.cdiweb.controller.CDIWebContext;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Controller;
import pl.softwaremill.cdiweb.controller.annotation.Get;
import pl.softwaremill.cdiweb.controller.annotation.Json;
import pl.softwaremill.cdiweb.controller.annotation.Post;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Home page controller
 *
 * User: szimano
 */
@Controller("home")
public class Home extends ControllerBean implements Serializable {

    private Person person = new Person();

    @Inject
    private LoginBean loginBean;

    public LoginBean getLoginBean() {
        return loginBean;
    }

    public void setLoginBean(LoginBean loginBean) {
        this.loginBean = loginBean;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    @Get
    public void index() {
        System.out.println("Running index controller !");

        setParameter("list", Arrays.asList("One", "Two", "Three"));

        System.out.println("Extra path: "+Arrays.toString(getExtraPath()));
    }

    @Get
    public void register() {

    }

    @Json
    public Person showJSON() {
        person = new Person();
        person.setAddresses(Arrays.asList("One Ad", "Second Ad"));
        person.setName("Tomek");
        person.setLastName("Szymanski");

        return person;
    }
    
    @Json
    public Map<String, String> showJSONMap() {
        Map<String, String> map= new HashMap<String, String>();
        map.put("test", "this is test");
        map.put("post", "this is post");
        map.put("most", "this is most");

        return map;
    }
    
    public void notWorking() {}

    @Post
    public void doRegister() {
        System.out.println("Parameter names: "+getParameterNames());

        doAutoBinding("person.name", "person.lastName", "person.addresses");

        if (person.getName().length() == 0) {
            addMessageToFlash("Name cannot be null", CDIWebContext.MessageSeverity.ERR);
            includeView("register");

            return;
        }

        System.out.println("person = " + person);

        System.out.println("Parameter name = "+getParameter("person.name"));
        System.out.println("Parameter addresses = "+getParameterValues("person.addresses"));

        addMessageToFlash("User was registered successfully", CDIWebContext.MessageSeverity.SUCCESS);
        addMessageToFlash("And i mean it", CDIWebContext.MessageSeverity.SUCCESS);

        addMessageToFlash("Info", CDIWebContext.MessageSeverity.INFO);

        addMessageToFlash("Warn", CDIWebContext.MessageSeverity.WARN);

        addMessageToFlash("ERROR baby, error.", CDIWebContext.MessageSeverity.ERR);

        // on succesfull
        redirect("index");
    }

    @Get
    public void doGetRegister() {
        doAutoBinding("person.name", "person.lastName", "person.addresses");

        System.out.println("Parameter names: "+getParameterNames());

        System.out.println("Parameter name = "+getParameter("person.name"));
        
        redirect("index");
    }

    @Get
    public void login() {
        String previousURI = (String) getObjectFromFlash(AuthorizationFilter.PREVIOUS_URI);

        System.out.println("login() prevoius = " + previousURI);

        if (previousURI != null) {
            addObjectToFlash(AuthorizationFilter.PREVIOUS_URI, previousURI);
        }
    }

    @Post
    public void doLogin() {
        String previousURI = (String) getObjectFromFlash(AuthorizationFilter.PREVIOUS_URI);

        System.out.println("doLogin() previous = " + previousURI);

        if (loginBean.doLogin(getParameter("login"), getParameter("password"))) {
            addMessageToFlash("Logged in as "+getParameter("login"), CDIWebContext.MessageSeverity.SUCCESS);

            if (previousURI != null) {
                redirectToURI(previousURI);
            }
            else {
                redirect("index");
            }

        } else {
            // put the PREV URI into flash again
            addObjectToFlash(AuthorizationFilter.PREVIOUS_URI, previousURI);
            includeView("login");
        }
    }

    @Post
    public void doImage() {
        doAutoBinding("person.file", "person.addresses");

        System.out.println("person.getAddresses() = " + person.getAddresses());
        System.out.println("person.file.class = " + person.getFile().getClass());
        System.out.println("person.file = " + person.getFile());

        System.out.println("Object parameter file = " + getContext().getObjectParameter("person.file"));
    }


    @Get
    public void logout() {
        loginBean.logout();

        redirect("index");
    }

    @Get
    public void rerender() {

    }

    @Post
    public void doReRender() {
        doOptionalAutoBinding("person.name", "person.lastName");

        includeView("rerender");
    }
}
