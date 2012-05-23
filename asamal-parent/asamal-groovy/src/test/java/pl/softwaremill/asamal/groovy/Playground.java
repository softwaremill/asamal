package pl.softwaremill.asamal.groovy;

import groovy.util.GroovyScriptEngine;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.fest.assertions.Assertions.assertThat;

/**
 * User: szimano
 */
public class Playground {

    @Test
    public void testGSE() throws Exception {
        // given

        GroovyScriptEngine gse = new GroovyScriptEngine("asamal-groovy/src/test/resources/groovy");

        // when

        Class testClass = gse.loadScriptByName("Test.groovy");

        // then

        Object o = testClass.newInstance();

        Method doSomething = testClass.getDeclaredMethod("doSomething");

        assertThat(doSomething.invoke(o)).isEqualTo("Hello Guys");
    }
}
