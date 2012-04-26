package pl.softwaremill.asamal.example.model.ticket;

import org.hibernate.validator.constraints.NotEmpty;
import pl.softwaremill.asamal.example.model.BaseEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "TICKET")
public class Ticket extends BaseEntity{

    @Column(name = "first_name")
    @NotEmpty
    @Size(min = 2)
    private String firstName;

    @Column(name = "last_name")
    @NotEmpty
    private String lastName;

    @ManyToOne
    private Invoice invoice;

    @ManyToOne
    private TicketCategory ticketCategory;

    @OneToMany(mappedBy = "ticket", cascade = CascadeType.ALL)
    private List<TicketOption> options;

    public Ticket() {
    }

    public Ticket(String firstName, String lastName, TicketCategory ticketCategory) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.ticketCategory = ticketCategory;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public TicketCategory getTicketCategory() {
        return ticketCategory;
    }

    public void setTicketCategory(TicketCategory ticketCategory) {
        this.ticketCategory = ticketCategory;
    }

    public List<TicketOption> getOptions() {
        return options;
    }

    public void setOptions(List<TicketOption> options) {
        this.options = options;
    }
}
