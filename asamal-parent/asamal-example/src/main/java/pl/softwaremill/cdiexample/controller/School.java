package pl.softwaremill.cdiexample.controller;

import pl.softwaremill.cdiexample.filters.AuthorizationFilter;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Controller;
import pl.softwaremill.cdiweb.controller.annotation.Filters;
import pl.softwaremill.cdiweb.controller.annotation.Get;
import pl.softwaremill.common.cdi.security.Secure;

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

    @Get
    @Secure("#{login.admin}")
    public void admin() {

    }
}
