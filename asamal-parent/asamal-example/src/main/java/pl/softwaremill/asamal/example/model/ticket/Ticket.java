package pl.softwaremill.asamal.example.model.ticket;

import pl.softwaremill.asamal.example.model.BaseEntity;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "TICKET")
public class Ticket extends BaseEntity{

    @OneToMany(mappedBy = "ticket")
    private Set<Attendee> attendees;

    @ManyToOne
    private Invoice invoice;

    @ManyToOne
    private TicketCategory ticketCategory;

    public Set<Attendee> getAttendees() {
        return attendees;
    }

    public void setAttendees(Set<Attendee> attendees) {
        this.attendees = attendees;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}
