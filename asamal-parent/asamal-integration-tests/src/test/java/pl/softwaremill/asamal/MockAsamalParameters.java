package pl.softwaremill.asamal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MockAsamalParameters extends AsamalParameters {

    private Map<String, Object> paramaters;

    public MockAsamalParameters(Map<String, Object> paramaters) {
        super(null, null);
        this.paramaters = paramaters;
    }

    @Override
    public String getParameter(String key) {
        return (String)paramaters.get(key);
    }

    @Override
    public Object getObjectParameter(String key) {
        return paramaters.get(key);
    }

    @Override
    public List<String> getParameterValues(String key) {
        return Collections.singletonList(getParameter(key));
    }

    @Override
    public Set<String> getParameterNames() {
        return paramaters.keySet();
    }

    @Override
    public List<Object> getObjectParameterValues(String parameterName) {
        return Collections.singletonList(getObjectParameter(parameterName));
    }
}
