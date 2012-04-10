package pl.softwaremill.asamal.example.service.conf;

import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.conf.ConfigurationProperty;
import pl.softwaremill.common.cdi.transaction.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class ConfigurationService {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Transactional
    public void saveProperty(Conf conf, String value) {
        ConfigurationProperty property = entityManager.find(ConfigurationProperty.class, conf.toString());

        if (property == null) {
            property = new ConfigurationProperty();
            property.setKey(conf.toString());
        }

        property.setValue(value);

        entityManager.merge(property);
    }

    @Transactional
    public void saveProperty(Conf conf, Boolean value) {
        ConfigurationProperty property = entityManager.find(ConfigurationProperty.class, conf.toString());

        if (property == null) {
            property = new ConfigurationProperty();
            property.setKey(conf.toString());
        }

        property.setValue(value.toString());

        entityManager.merge(property);
    }
    
    @Transactional
    public ConfigurationProperty getProperty(Conf conf) {
        return entityManager.find(ConfigurationProperty.class, conf.toString());
    }

    @Transactional
    public List<ConfigurationProperty> getAllProperties() {
        return entityManager.createQuery("select cp from ConfigurationProperty cp").getResultList();
    }
}
