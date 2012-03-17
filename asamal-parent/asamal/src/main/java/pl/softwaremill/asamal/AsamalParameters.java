package pl.softwaremill.asamal;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Class that unifies access to request parameters
 */
public class AsamalParameters {

    private HttpServletRequest request;

    private MultivaluedMap<String, Object> formValueMap;

    public AsamalParameters(HttpServletRequest request, MultivaluedMap<String, Object> formValueMap) {
        this.formValueMap = formValueMap;
        this.request = request;
    }

    /**
     * Gets the parameter's single (or first) value
     *
     * @param key Name of the parameter
     * @return Value or null if it doesn't exist
     */
    public String getParameter(String key) {
        if (formValueMap == null) {
            String[] values = request.getParameterMap().get(key);

            return (values == null || values.length == 0 ? null : values[0]);
        }
        return (String) formValueMap.getFirst(key);
    }

    public Object getObjectParameter(String key) {
        if (formValueMap == null) {
            String[] values = request.getParameterMap().get(key);

            return (values.length == 0 ? null : values[0]);
        }
        return formValueMap.getFirst(key);
    }

    /**
     * Gets all the values of a single parameter
     *
     * @param key Name of the parameter
     * @return List of values, or null if no such parameter
     */
    public List<String> getParameterValues(String key) {
        if (formValueMap == null) {
            if (request.getParameterMap().containsKey(key)) {
                return Arrays.asList(request.getParameterMap().get(key));
            }
            else {
                return null;
            }
        }

        if (formValueMap.containsKey(key)) {
            List<String> values = new ArrayList<String>();

            for (Object obj : formValueMap.get(key)) {
                values.add(obj.toString());
            }

            return values;
        }

        return null;
    }

    /**
     * Gets all the parameter's names available from the post/get
     *
     * @return Set of parameter names
     */
    public Set<String> getParameterNames() {
        if (formValueMap == null) {
            return request.getParameterMap().keySet();
        }
        return formValueMap.keySet();
    }

    public List<Object> getObjectParameterValues(String parameterName) {
        if (formValueMap == null) {
            List<Object> o = new ArrayList<Object>();
            for (String param : request.getParameterValues(parameterName)) {
                o.add(param);
            }
            return o;
        }
        return formValueMap.get(parameterName);
    }
}
