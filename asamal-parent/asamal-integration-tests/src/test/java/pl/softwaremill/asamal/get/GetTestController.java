package pl.softwaremill.asamal.get;

import pl.softwaremill.asamal.common.TestRecorder;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.PathParameter;
import pl.softwaremill.asamal.controller.annotation.RequestParameter;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

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

    @Get
    public void testWithParams(@RequestParameter("param") String param) {
        recorder.getMethodsCalled().add("testWithParams_" + param);
    }

    @Get
    public void testWithParamsRequired(@RequestParameter(value = "param", required = true) String param) {
        recorder.getMethodsCalled().add("testWithParamsRequired_" + param);
    }

    @Get
    public void testWithObjectParamsRequired(@RequestParameter(value = "param", required = true) List<Object> param) {
        recorder.getMethodsCalled().add("testWithObjectParamsRequired_" + Arrays.toString((Object[]) param.get(0)));
    }

    @Get(params = "/id/name")
    public void testWithPathParams(@PathParameter("id") Long id, @PathParameter("name") String name) {
        recorder.getMethodsCalled().add("testWithPathParams id = " + id + " and name = " + name);
    }

    @Get(params = "/pid")
    public void testWithPathParamsWrong(@PathParameter("id") Long id) {
        recorder.getMethodsCalled().add("testWithPathParamsWrong id = " + id);
    }

    @Get(params = "/id")
    public void testWithPathParamsMixed(@PathParameter("id") Long id, @RequestParameter("param") String param) {
        recorder.getMethodsCalled().add("testWithPathParamsMixed id = " + id + " param = " + param);
    }

}