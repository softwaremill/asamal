package pl.softwaremill.asamal.controller.testcontrollers;

/**
 * User: szimano
 */
public class TestPojo {
    private String test;

    private InTest inTest = new InTest();

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    public InTest getInTest() {
        return inTest;
    }

    public void setInTest(InTest inTest) {
        this.inTest = inTest;
    }
}
