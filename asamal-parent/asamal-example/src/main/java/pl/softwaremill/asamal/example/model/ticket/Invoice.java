package pl.softwaremill.asamal.example.model.ticket;

import pl.softwaremill.asamal.example.model.BaseEntity;
import pl.softwaremill.asamal.example.model.security.User;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "INVOICE")
public class Invoice extends BaseEntity{

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL)
    private Set<Ticket> tickets;

    @Column(name = "name", nullable = false)
    @NotNull
    private String name;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "vat")
    private String vat;

    @Column(name = "address", nullable = false)
    @NotNull
    private String address;

    @Column(name = "postal_code", nullable = false)
    @NotNull
    private String postalCode;

    @Column(name = "city", nullable = false)
    @NotNull
    private String city;

    @Column(name = "country", nullable = false)
    @NotNull
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "satus")
    private InvoiceStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    public Invoice() {
    }

    public Invoice(Set<Ticket> tickets, String name, String companyName, String vat, String address,
                   String postalCode, String city, String country, InvoiceStatus status, User user) {
        this.tickets = tickets;
        this.name = name;
        this.companyName = companyName;
        this.vat = vat;
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
        this.country = country;
        this.status = status;
        this.user = user;
    }

    public Set<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(Set<Ticket> tickets) {
        this.tickets = tickets;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getVat() {
        return vat;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
