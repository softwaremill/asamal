package pl.softwaremill.asamal.get;

import org.junit.Ignore;

import javax.inject.Named;

/**
 * User: szimano
 */
@Named("testNamedBean")
@Ignore
public class TestNamedBean {

    private String value = "Some Value From Named Bean";

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
