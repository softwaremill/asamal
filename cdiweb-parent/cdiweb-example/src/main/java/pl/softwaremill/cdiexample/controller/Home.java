package pl.softwaremill.cdiexample.controller;

import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Controller;

/**
 * Home page controller
 *
 * User: szimano
 */
@Controller("home")
public class Home extends ControllerBean {

    public void index() {
        System.out.println("Running index controller !");
    }
}
