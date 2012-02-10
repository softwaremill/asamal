package pl.softwaremill.cdiexample.controller;

import pl.softwaremill.cdiexample.model.Person;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Controller;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;

/**
 * Home page controller
 *
 * User: szimano
 */
@Controller("home")
@Path("/home")
public class Home extends ControllerBean {

    private Person person = new Person();

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void index() {
        System.out.println("Running index controller !");

        setParameter("list", Arrays.asList("One", "Two", "Three"));
    }

    public void register() {

    }

    @POST
    @Path("/doRegister")
    public void doRegister(MultivaluedMap<String, String> form) {
        System.out.println("form = " + form);
        doPostMagic(form);

        System.out.println("person = " + person);
    }


}
