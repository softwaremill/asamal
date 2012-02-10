package pl.softwaremill.cdiexample.controller;

import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Controller;
import pl.softwaremill.cdiweb.controller.annotation.Get;

/**
 * Home page controller
 *
 * User: szimano
 */
@Controller("school")
public class School extends ControllerBean {

    @Get
    public void index() {
        System.out.println("Running index controller !");
    }
}
