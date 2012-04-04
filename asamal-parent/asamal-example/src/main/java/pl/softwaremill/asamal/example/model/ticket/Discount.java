package pl.softwaremill.asamal.example.model.ticket;

import pl.softwaremill.asamal.example.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Discounts for tickets
 *
 * User: szimano
 */
@Entity
@Table(name = "DISCOUNT")
public class Discount extends BaseEntity {

    @Column(name = "discount_code", unique = true, nullable = false)
    @NotNull
    private String discountCode;

    @Column(name = "discount_amount", nullable = false)
    @NotNull
    @Min(1) @Max(100)
    private Integer discountAmount;

    @Column(name = "number_of_uses", nullable = false)
    @NotNull
    private Integer numberOfUses;

    @OneToMany(mappedBy = "discount")
    private Set<Ticket> tickets;

    public String getDiscountCode() {
        return discountCode;
    }

    public void setDiscountCode(String discountCode) {
        this.discountCode = discountCode;
    }

    public Integer getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(Integer discountAmount) {
        this.discountAmount = discountAmount;
    }

    public Integer getNumberOfUses() {
        return numberOfUses;
    }

    public void setNumberOfUses(Integer numberOfUses) {
        this.numberOfUses = numberOfUses;
    }

    public Set<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(Set<Ticket> tickets) {
        this.tickets = tickets;
    }
}
