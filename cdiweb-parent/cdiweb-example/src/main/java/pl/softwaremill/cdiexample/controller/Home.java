package pl.softwaremill.cdiexample.controller;

import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Controller;

import java.util.Arrays;

/**
 * Home page controller
 *
 * User: szimano
 */
@Controller("home")
public class Home extends ControllerBean {

    public void index() {
        System.out.println("Running index controller !");

        setParameter("list", Arrays.asList("One", "Two", "Three"));
    }

    public void register() {

    }
}
