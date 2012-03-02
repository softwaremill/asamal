package pl.softwaremill.asamal.example.model.ticket;

import pl.softwaremill.asamal.example.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "INVOICE")
public class Invoice extends BaseEntity{

    @OneToMany(mappedBy = "invoice")
    private Set<Ticket> tickets;

    @Column(name = "name")
    private String name;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "vat")
    private String vat;

    @Column(name = "address")
    private String address;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "city")
    private String city;

    public Invoice() {
    }

    public Invoice(Set<Ticket> tickets, String name, String companyName, String vat, String address,
                   String postalCode, String city) {
        this.tickets = tickets;
        this.name = name;
        this.companyName = companyName;
        this.vat = vat;
        this.address = address;
        this.postalCode = postalCode;
        this.city = city;
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
}
