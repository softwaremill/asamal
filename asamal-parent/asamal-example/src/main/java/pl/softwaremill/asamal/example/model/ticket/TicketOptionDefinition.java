package pl.softwaremill.asamal.example.model.ticket;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.Set;

@Entity
public class TicketOptionDefinition implements Serializable {

    @Id
    @GeneratedValue
    public Long id;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    public TicketOptionType type;

    @OneToMany(mappedBy = "optionDefinition")
    private Set<TicketOption> options;

    @Column(length = 1024, name = "config")
    public String config;

    @Column(name = "label")
    public String label;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TicketOptionType getType() {
        return type;
    }

    public void setType(TicketOptionType type) {
        this.type = type;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public Set<TicketOption> getOptions() {
        return options;
    }

    public void setOptions(Set<TicketOption> options) {
        this.options = options;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
