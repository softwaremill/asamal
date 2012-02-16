package pl.softwaremill.asamal.common;

import javax.enterprise.context.ApplicationScoped;
import java.util.HashSet;
import java.util.Set;

/**
 * User: szimano
 */
@ApplicationScoped
public class TestRecorder {

    private Set<String> methodsCalled = new HashSet<String>();

    public Set<String> getMethodsCalled() {
        return methodsCalled;
    }

    public void clear() {
        methodsCalled.clear();
    }
}
