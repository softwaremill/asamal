package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Filters;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.example.filters.AuthorizationFilter;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Home page controller
 *
 * User: szimano
 */
@Controller("home")
@Filters(AuthorizationFilter.class)
public class Home extends ControllerBean implements Serializable {

    @Get
    public void index() {
        System.out.println("Running index controller !");

        putInContext("list", Arrays.asList("One", "Two", "Three"));

        System.out.println("Extra path: "+Arrays.toString(getExtraPath()));
    }

}
