package pl.softwaremill.cdiexample.controller;

import pl.softwaremill.cdiexample.filters.AuthorizationFilter;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Controller;
import pl.softwaremill.cdiweb.controller.annotation.Filters;
import pl.softwaremill.cdiweb.controller.annotation.Get;

/**
 * Home page controller
 *
 * User: szimano
 */
@Controller("school")
@Filters(AuthorizationFilter.class)
public class School extends ControllerBean {

    @Get
    public void index() {
        System.out.println("Running index controller !");
    }
}
