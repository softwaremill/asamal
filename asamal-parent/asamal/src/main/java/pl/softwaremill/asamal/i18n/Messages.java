package pl.softwaremill.asamal.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

public class Messages implements Map<String, String> {
    
    private ResourceBundle bundle = ResourceBundle.getBundle("messages");

    private final static Logger log = LoggerFactory.getLogger(Messages.class);

    /**
     * Reads message from resources and then if optional parameters are passed, formats it
     */
    public String getFromBundle(String key, Object... params) {
        String msg = bundle.getString(key);

        if (params != null && params.length > 0) {
            MessageFormat mf = new MessageFormat(msg);

            msg = mf.format(params);
        }

        return msg;
    }

    @Override
    public int size() {
        throw new RuntimeException("not available");
    }

    @Override
    public boolean isEmpty() {
        return bundle.getKeys().hasMoreElements();
    }

    @Override
    public boolean containsKey(Object o) {
        return bundle.containsKey((String) o);
    }

    @Override
    public boolean containsValue(Object o) {
        return false;
    }

    @Override
    public String get(Object o) {
        try {
            return bundle.getString((String) o);
        } catch (MissingResourceException e) {
            // do not fail everything. warn, and return the key
            log.warn("Key "+o+" not found in the messages bundle.");
            return o.toString();
        }
    }

    @Override
    public String put(String s, String s1) {
        throw new RuntimeException("not available");
    }

    @Override
    public String remove(Object o) {
        throw new RuntimeException("not available");
    }

    @Override
    public void putAll(Map<? extends String, ? extends String> map) {
        throw new RuntimeException("not available");
    }

    @Override
    public void clear() {
        throw new RuntimeException("not available");
    }

    @Override
    public Set<String> keySet() {
        throw new RuntimeException("not available");
    }

    @Override
    public Collection<String> values() {
        throw new RuntimeException("not available");
    }

    @Override
    public Set<Entry<String, String>> entrySet() {
        throw new RuntimeException("not available");
    }
}
