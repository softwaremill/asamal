package pl.softwaremill.asamal.example.model.ticket;

import pl.softwaremill.asamal.example.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Set;

/**
 * Stores different ticket categories with the numbers
 */
@Entity
@Table(name = "TICKET_CATEGORY")
public class TicketCategory extends BaseEntity {

    @Column(name = "from_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fromDate;

    @Column(name = "to_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date toDate;

    @Column(name = "number_of_tickets", nullable = false)
    private Integer numberOfTickets;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;
    
    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "invoice_description", nullable = false)
    private String invoiceDescription;
    
    @OneToMany(mappedBy = "ticketCategory")
    private Set<Ticket> tickets;

    public TicketCategory(Date fromDate, Date toDate, Integer numberOfTickets, String name, String description,
                          Integer price, String invoiceDescription) {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.numberOfTickets = numberOfTickets;
        this.name = name;
        this.description = description;
        this.price = price;
        this.invoiceDescription = invoiceDescription;
    }

    public TicketCategory() {
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public Integer getNumberOfTickets() {
        return numberOfTickets;
    }

    public void setNumberOfTickets(Integer numberOfTickets) {
        this.numberOfTickets = numberOfTickets;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Set<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(Set<Ticket> tickets) {
        this.tickets = tickets;
    }

    public String getInvoiceDescription() {
        return invoiceDescription;
    }

    public void setInvoiceDescription(String invoiceDescription) {
        this.invoiceDescription = invoiceDescription;
    }

    /**
     * ID friendly transformed name (no spaces, urlencoded)
     */
    public String getIdName() {
        try {
            return URLEncoder.encode(name.replaceAll(" ", ""), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
