package pl.softwaremill.asamal.example.logic.invoice;

import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.ticket.Discount;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.Ticket;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;

import javax.inject.Inject;
import javax.inject.Named;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;

/**
 * Counts all the amounts on the given invoice
 */
@Named("totalsCounter")
public class InvoiceTotalsCounter {

    private ConfigurationBean configurationBean;

    @Inject
    public InvoiceTotalsCounter(ConfigurationBean configurationBean) {
        this.configurationBean = configurationBean;
    }

    public InvoiceTotals countInvoice(Invoice invoice) {

        InvoiceTotals totals = new InvoiceTotals();

        for (Map.Entry<TicketCategory, Collection<Ticket>> invoiceEntry :
                invoice.getTicketsByCategory().asMap().entrySet()) {
            TicketCategory category = invoiceEntry.getKey();

            Discount discount = invoice.getDiscount();

            int numberOfTickets = invoiceEntry.getValue().size();

            BigDecimal price = new BigDecimal(category.getPrice()).
                    multiply(new BigDecimal(numberOfTickets));

            if (discount != null) {
                totals.setDiscount(discount);
                price = price.multiply(new BigDecimal(1).subtract(
                        new BigDecimal(discount.getDiscountAmount()).divide(new BigDecimal(100))));
            }

            BigDecimal vatAmount = price.multiply(new BigDecimal(configurationBean.getProperty(Conf.INVOICE_VAT_RATE))
                    .divide(new BigDecimal(100)));

            totals.addNewTotal(category, price, vatAmount, numberOfTickets);
        }

        return totals;
    }

}
