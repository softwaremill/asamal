package pl.softwaremill.asamal.get;

import pl.softwaremill.asamal.common.TestRecorder;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Get;

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