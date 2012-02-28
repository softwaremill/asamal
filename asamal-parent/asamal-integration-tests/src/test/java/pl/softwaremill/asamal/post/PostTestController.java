package pl.softwaremill.asamal.post;

import pl.softwaremill.asamal.common.TestRecorder;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Post;

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

    @Post(skipViewHash = true)
    public void doPostWithoutViewHashCheck() {
        recorder.getMethodsCalled().add("doPostWithoutViewHashCheck");
    }
}