package pl.softwaremill.cdiexample.controller;

import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Controller;

import javax.ws.rs.Path;

/**
 * Home page controller
 *
 * User: szimano
 */
@Controller("school")
public class School extends ControllerBean {

    public void index() {
        System.out.println("Running index controller !");
    }
}
