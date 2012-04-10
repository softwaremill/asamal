package pl.softwaremill.asamal.example.logic.invoice;

import pl.softwaremill.asamal.example.model.ticket.Discount;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents invoice total amounts
 */
public class InvoiceTotals {

    private List<InvoiceTotal> allTotals = new ArrayList<InvoiceTotal>();
    private BigDecimal totalAmount = new BigDecimal(0);
    private BigDecimal totalVat = new BigDecimal(0);
    private Discount discount;

    public void addNewTotal(TicketCategory category, BigDecimal amount, BigDecimal vatAmount, Integer numberOfTickets) {
        allTotals.add(new InvoiceTotal(category, amount.setScale(2, BigDecimal.ROUND_HALF_DOWN),
                vatAmount.setScale(2, BigDecimal.ROUND_HALF_DOWN), numberOfTickets));

        totalAmount = totalAmount.add(amount);
        totalVat = totalVat.add(vatAmount);
    }

    public BigDecimal getTotalAmount() {
        return totalAmount.setScale(2, BigDecimal.ROUND_HALF_DOWN);
    }

    public BigDecimal getTotalVat() {
        return totalVat.setScale(2, BigDecimal.ROUND_HALF_DOWN);
    }

    public BigDecimal getTotalGrossAmount() {
        return totalAmount.add(totalVat).setScale(2, BigDecimal.ROUND_HALF_DOWN);
    }

    public List<InvoiceTotal> getAllTotals() {
        return allTotals;
    }

    public void setDiscount(Discount discount) {
        this.discount = discount;
    }

    public Discount getDiscount() {
        return discount;
    }

    public boolean isDiscounted() {
        return discount != null;
    }
}