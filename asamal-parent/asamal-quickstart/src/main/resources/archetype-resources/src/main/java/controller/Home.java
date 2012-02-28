#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${package}.controller;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.Json;
import pl.softwaremill.asamal.controller.annotation.Post;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Home page controller
 *
 * User: szimano
 */
@Controller("home")
public class Home extends ControllerBean implements Serializable {

    @Get
    public void index() {
        System.out.println("Running index controller !");

        putInContext("list", Arrays.asList("One", "Two", "Three"));
    }
}
