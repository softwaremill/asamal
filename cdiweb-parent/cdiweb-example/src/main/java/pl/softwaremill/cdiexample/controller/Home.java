package pl.softwaremill.cdiexample.controller;

import pl.softwaremill.cdiexample.model.Person;
import pl.softwaremill.cdiweb.controller.CDIWebContext;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Controller;
import pl.softwaremill.cdiweb.controller.annotation.Get;
import pl.softwaremill.cdiweb.controller.annotation.Json;
import pl.softwaremill.cdiweb.controller.annotation.Post;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Home page controller
 *
 * User: szimano
 */
@Controller("home")
public class Home extends ControllerBean implements Serializable {

    private Person person = new Person();

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
    
    public void notWorking() {}

    @Post
    public void doRegister() {
        System.out.println("Parameter names: "+getParameterNames());

        System.out.println("person = " + person);

        System.out.println("Parameter name = "+getParameter("person.name"));
        System.out.println("Parameter addresses = "+getParameterValues("person.addresses"));

        addMessageToFlash("User was registered successfully");
        addMessageToFlash("And i mean it");

        // on succesfull
        redirect("index");
    }

    @Get
    public void doGetRegister() {
        System.out.println("Parameter names: "+getParameterNames());

        System.out.println("Parameter name = "+getParameter("person.name"));
        
        redirect("index");
    }


}
