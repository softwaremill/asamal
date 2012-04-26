package pl.softwaremill.asamal.example.model.ticket;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class TicketOption {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToOne
    @JoinColumn(name = "definition")
    private TicketOptionDefinition optionDefinition;

    @Column(length = 1024, name = "value")
    private String value;

    @ManyToOne
    @JoinColumn(name = "ticket")
    private Ticket ticket;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TicketOptionDefinition getOptionDefinition() {
        return optionDefinition;
    }

    public void setOptionDefinition(TicketOptionDefinition optionDefinition) {
        this.optionDefinition = optionDefinition;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }
}
