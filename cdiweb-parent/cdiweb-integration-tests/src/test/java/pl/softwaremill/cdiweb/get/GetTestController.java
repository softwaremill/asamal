package pl.softwaremill.cdiweb.get;

import pl.softwaremill.cdiweb.common.TestRecorder;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Controller;
import pl.softwaremill.cdiweb.controller.annotation.Get;

import javax.inject.Inject;

/**
 * User: szimano
 */
@Controller("get")
public class GetTestController extends ControllerBean {

    @Inject
    private TestRecorder recorder;

    @Get
    public void testMethod() {
        recorder.getMethodsCalled().add("testMethod");
    }

}