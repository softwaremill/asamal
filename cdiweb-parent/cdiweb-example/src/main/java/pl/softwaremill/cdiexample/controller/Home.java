package pl.softwaremill.cdiexample.controller;

import pl.softwaremill.cdiexample.model.Person;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Controller;
import pl.softwaremill.cdiweb.controller.annotation.Get;
import pl.softwaremill.cdiweb.controller.annotation.Json;
import pl.softwaremill.cdiweb.controller.annotation.Post;

import java.util.Arrays;

/**
 * Home page controller
 *
 * User: szimano
 */
@Controller("home")
public class Home extends ControllerBean {

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
        System.out.println("person = " + person);
    }


}
