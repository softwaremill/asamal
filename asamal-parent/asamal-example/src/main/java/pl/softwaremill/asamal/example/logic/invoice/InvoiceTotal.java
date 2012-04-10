package pl.softwaremill.asamal.example.logic.invoice;

import pl.softwaremill.asamal.example.model.ticket.TicketCategory;

import java.math.BigDecimal;

public class InvoiceTotal {

    private final TicketCategory category;
    private final BigDecimal amount;
    private final BigDecimal vatAmount;
    private Integer numberOfTickets;

    public InvoiceTotal(TicketCategory category, BigDecimal amount, BigDecimal vatAmount, Integer numberOfTickets) {
        this.category = category;
        this.amount = amount;
        this.vatAmount = vatAmount;
        this.numberOfTickets = numberOfTickets;
    }

    public TicketCategory getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getVatAmount() {
        return vatAmount;
    }

    public Integer getNumberOfTickets() {
        return numberOfTickets;
    }

    @Override
    public String toString() {
        return "InvoiceTotal{" +
                "category=" + category +
                ", amount=" + amount +
                ", vatAmount=" + vatAmount +
                ", numberOfTickets=" + numberOfTickets +
                '}';
    }
}
