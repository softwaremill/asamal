package pl.softwaremill.asamal.example.controller;

import com.ibm.icu.text.RuleBasedNumberFormat;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Filters;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.PathParameter;
import pl.softwaremill.asamal.example.filters.ActiveFilter;
import pl.softwaremill.asamal.example.filters.AuthorizationFilter;
import pl.softwaremill.asamal.example.logic.auth.LoginBean;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.InvoiceStatus;
import pl.softwaremill.asamal.example.service.ticket.TicketService;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.Locale;

@Controller("invoice")
@Filters({ActiveFilter.class, AuthorizationFilter.class})
public class InvoiceController extends ControllerBean {

    @Inject
    private TicketService ticketService;

    @Inject
    private LoginBean loginBean;

    @Get(params = "/id")
    public void pdf(@PathParameter("id") Long invoiceId) {
        Invoice invoice = ticketService.loadInvoice(invoiceId);

        if (!loginBean.isAdmin() && !invoice.getUser().equals(loginBean.getUser())) {
            throw new RuntimeException("You are trying to view an invoice that does not belong to you!");
        }

        if (invoice == null) {
            throw new RuntimeException("No such invoice with id: "+invoiceId);
        }

        putInContext("invoice", invoice);

        if (invoice.getStatus() == InvoiceStatus.UNPAID) {
            putInContext("invoiceType", "Proforma");
            putInContext("proformaId", "PROF/");
            putInContext("invoiceNumber", invoice.getId());
        } else if (invoice.getStatus() == InvoiceStatus.PAID) {
            putInContext("invoiceType", "VAT");
            putInContext("invoiceNumber", invoice.getInvoiceNumber());
        } else if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new RuntimeException("Cancelled invoice!");
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
