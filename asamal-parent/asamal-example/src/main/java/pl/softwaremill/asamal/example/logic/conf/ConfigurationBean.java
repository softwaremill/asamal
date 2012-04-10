package pl.softwaremill.asamal.example.logic.conf;

import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.conf.ConfigurationProperty;
import pl.softwaremill.asamal.example.service.conf.ConfigurationService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

@Named("conf")
@Singleton
public class ConfigurationBean implements Serializable {

    private Map<Conf, String> properties;

    @Inject
    private ConfigurationService configurationService;

    public Map<Conf, String> getProperties() {
        if (properties == null) {
            properties = new TreeMap<Conf, String>();

            for (ConfigurationProperty property : configurationService.getAllProperties()) {
                properties.put(Conf.valueOf(property.getKey()), property.getValue());
            }
        }
        return properties;
    }

    public void setProperties(Map<Conf, String> properties) {
        this.properties = properties;
    }
    
    public String getProperty(Conf conf) {
        return getProperties().get(conf);
    }
    
    public String getProperty(String conf) {
        return getProperty(Conf.valueOf(conf));
    }

    public Boolean getBooleanProperty(Conf conf) {
        if (!conf.isBool()) {
            throw new RuntimeException("Trying to access non-bool property as Boolean");
        }
        return Boolean.parseBoolean(getProperties().get(conf));
    }

    public Boolean getBooleanProperty(String conf) {
        return getBooleanProperty(Conf.valueOf(conf));
    }
    
    public Integer getAsInt(String conf) {
        return Integer.parseInt(getProperty(conf));
    }
}
