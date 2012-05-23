package pl.softwaremill.asamal.groovy;

import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.common.util.dependency.D;
import pl.softwaremill.common.util.dependency.DependencyProvider;

import java.lang.annotation.Annotation;

/**
 * User: szimano
 */
public class GroovyResourceResolver implements DependencyProvider {

    private GroovyScriptEngine gse;

    public GroovyResourceResolver(AsamalResourceConnector asamalResourceConnector) {
        gse = new GroovyScriptEngine(asamalResourceConnector);
    }

    public <T> T inject(Class<T> cls, Annotation... qualifiers) {
        if (ControllerBean.class.isAssignableFrom(cls)) {
            // look for the groovy script

            for (Annotation qualifier : qualifiers) {
                if (qualifier instanceof Controller) {
                    try {
                        return (T) gse.loadScriptByName(((Controller) qualifier).value() + ".groovy").newInstance();
                    } catch (InstantiationException e) {
                        throw new RuntimeException(e);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    } catch (ResourceException e) {
                        throw new RuntimeException(e);
                    } catch (ScriptException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }

        return null;
    }
}
