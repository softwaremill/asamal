package pl.softwaremill.asamal.httphandler;

import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Get;

@Controller("textarea")
public class WhiteSpaceController extends ControllerBean {

    @Get
    public void whitespace() {

    }

    @Get
    public void html() {

    }
}
