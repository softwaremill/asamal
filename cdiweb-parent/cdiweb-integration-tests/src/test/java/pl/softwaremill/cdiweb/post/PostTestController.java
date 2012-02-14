package pl.softwaremill.cdiweb.post;

import pl.softwaremill.cdiweb.common.TestRecorder;
import pl.softwaremill.cdiweb.controller.ControllerBean;
import pl.softwaremill.cdiweb.controller.annotation.Controller;
import pl.softwaremill.cdiweb.controller.annotation.Post;

import javax.inject.Inject;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: szimano
 */
@Controller("post")
public class PostTestController extends ControllerBean {

    @Inject
    private TestRecorder recorder;

    @Post
    public void doPost() {
        recorder.getMethodsCalled().add("doPost");
    }

    @Post
    public void doFormDataPost() {
        recorder.getMethodsCalled().add("doFormDataPost");

        assertThat(getParameterValues("a")).containsExactly("a");
        assertThat(getParameterValues("b")).containsExactly("b", "c");
    }
}