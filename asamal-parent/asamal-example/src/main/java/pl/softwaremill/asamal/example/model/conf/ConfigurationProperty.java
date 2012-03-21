package pl.softwaremill.asamal.example.model.conf;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "CONF")
public class ConfigurationProperty implements Serializable {

    @Id
    @Column(name = "name", length = 40)
    private String key;

    @Column(name = "value", length = 255)
    private String value;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
