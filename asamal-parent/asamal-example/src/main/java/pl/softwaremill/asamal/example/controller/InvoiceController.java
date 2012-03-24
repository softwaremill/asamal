package pl.softwaremill.asamal.example.controller;

import com.ibm.icu.text.RuleBasedNumberFormat;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.InvoiceStatus;
import pl.softwaremill.asamal.example.service.ticket.TicketService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Locale;

@Controller("invoice")
public class InvoiceController extends ControllerBean {

    @Inject
    private TicketService ticketService;

    @Get
    public void pdf() {
        if (getExtraPath().length == 0) {
            throw new RuntimeException("No invoice id available");
        }

        Long invoiceId = Long.parseLong(getExtraPath()[0]);

        Invoice invoice = ticketService.loadInvoice(invoiceId);

        if (invoice == null) {
            throw new RuntimeException("No such invoice with id: "+invoiceId);
        }

        putInContext("invoice", invoice);

        if (invoice.getStatus() == InvoiceStatus.UNPAID) {
            putInContext("invoiceType", "Proforma");
            putInContext("proformaId", "PROF/");
        } else if (invoice.getStatus() != InvoiceStatus.CANCELLED) {
            putInContext("invoiceType", "VAT");
        }
    }
    
    public String toString(String locale, String value, String currency) {
        BigDecimal val = new BigDecimal(value);

        BigDecimal[] bigDecimals = val.divideAndRemainder(new BigDecimal(1));
        int integer = bigDecimals[0].intValue();
        int reminder = bigDecimals[1].multiply(new BigDecimal(100)).intValue();

        return new RuleBasedNumberFormat(new Locale(locale), RuleBasedNumberFormat.SPELLOUT).format(integer) +
                " " + currency + " " + reminder + "/100";
    }
}
