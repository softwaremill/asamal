package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Get;

@Controller("come")
public class Come extends ControllerBean {

    @Get
    public void back() {

    }
}
