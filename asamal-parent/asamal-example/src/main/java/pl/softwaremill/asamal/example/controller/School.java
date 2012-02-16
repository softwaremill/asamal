package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.example.filters.AuthorizationFilter;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Filters;
import pl.softwaremill.asamal.controller.annotation.Get;
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
