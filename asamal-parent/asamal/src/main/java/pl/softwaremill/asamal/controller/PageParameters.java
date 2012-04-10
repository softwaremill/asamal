package pl.softwaremill.asamal.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Represents a list of parameters passed after /controller/view
 */
public class PageParameters {

    private Object[] parameters;

    public PageParameters(Object... parameters) {
        this.parameters = parameters;
    }

    public String serialize() {
        StringBuffer sb = new StringBuffer();

        for (Object parameter : parameters) {
            try {
                sb.append("/").append(URLEncoder.encode(parameter.toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        return sb.toString();
    }
}
