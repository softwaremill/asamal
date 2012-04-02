package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.Post;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.InvoiceStatus;
import pl.softwaremill.asamal.example.model.ticket.Ticket;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.service.conf.ConfigurationService;
import pl.softwaremill.asamal.example.service.exception.TicketsExceededException;
import pl.softwaremill.asamal.example.service.ticket.TicketService;
import pl.softwaremill.common.cdi.security.Secure;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Controller("admin")
@Secure("#{login.admin}")
public class Admin extends ControllerBean{

    private TicketCategory ticketCat = new TicketCategory();

    @Inject
    private TicketService ticketService;
    
    @Inject
    private ConfigurationBean configurationBean;
    
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    private final static String[] TICKET_CATEGORY_PARAMS = new String[]{"ticketCat.name", "ticketCat.description",
            "ticketCat.fromDate", "ticketCat.toDate", "ticketCat.numberOfTickets", "ticketCat.price",
            "ticketCat.invoiceDescription"};

    public TicketCategory getTicketCat() {
        return ticketCat;
    }

    public void setTicketCat(TicketCategory ticketCat) {
        this.ticketCat = ticketCat;
    }

    @Get
    public void tickets() {
        putInContext("ticketCat", ticketCat);
    }

    @Get
    public void editTicketCat() {
        ticketCat = ticketService.loadCategory(Long.valueOf(getExtraPath()[0]));

        putInContext("ticketCat", ticketCat);

        addObjectToFlash("ticketCat", ticketCat);
    }

    @Get
    public void approvePayments() {

    }

    @Post
    public void searchPayment() {
        putInContext("invoice", ticketService.loadInvoice(Long.parseLong(getParameter("paymentId"))));
    }

    @Post
    public void approve() {
        try {
            Long invoiceId = Long.parseLong(getParameter("invoiceId"));

            Date datePaid = dateFormat.parse(getParameter("paymentDate"));

            Invoice invoice = ticketService.loadInvoice(invoiceId);

            invoice.setDatePaid(datePaid);
            invoice.setStatus(InvoiceStatus.PAID);

            invoice = ticketService.updateInvoice(invoice);

            putInContext("invoice", invoice);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }


    }

    @Post
    public void cancel() {
        Long invoiceId = Long.parseLong(getParameter("invoiceId"));

        Invoice invoice = ticketService.loadInvoice(invoiceId);

        invoice.setStatus(InvoiceStatus.CANCELLED);

        invoice = ticketService.updateInvoice(invoice);

        putInContext("invoice", invoice);
    }

    public BigDecimal countTotalAmount(Invoice invoice) {
        BigDecimal amount = new BigDecimal("0.00");

        BigDecimal vatAmount = new BigDecimal(configurationBean.getProperty(Conf.INVOICE_VAT_RATE))
                .divide(new BigDecimal("100")).add(new BigDecimal("1"));

        for (Ticket ticket : invoice.getTickets()) {
            amount = amount.add(new BigDecimal(ticket.getTicketCategory().getPrice()));
        }

        return amount.multiply(vatAmount).setScale(2);
    }

    @Post
    public void addTicketCategory() {
        doAutoBinding(TICKET_CATEGORY_PARAMS);

        try {
            ticketService.addTicketCategory(ticketCat);

            addMessageToFlash("Ticket Category added succesfuly", AsamalContext.MessageSeverity.SUCCESS);
        } catch (TicketsExceededException e) {
            addMessageToFlash(e.getMessage(), AsamalContext.MessageSeverity.ERR);
        }
    }

    @Post
    public void editTicketCategory() {
        ticketCat = (TicketCategory) getObjectFromFlash("ticketCat");

        doOptionalAutoBinding(TICKET_CATEGORY_PARAMS);

        ticketService.updateTicketCategory(ticketCat);

        addMessageToFlash("Ticket Category edited succesfuly", AsamalContext.MessageSeverity.SUCCESS);

        redirect("tickets");
    }
    
    @Post
    public void deleteTicketCategory() {
        ticketService.deleteTicketCategory(Long.valueOf(getParameter("id")));
    }

    @Inject
    private ConfigurationService configurationService;

    @Inject
    private ConfigurationBean configBean;

    @Get
    public void configuration() {

    }

    @Post
    public void saveConfig() {
        for (Conf conf : Conf.values()) {
            configurationService.saveProperty(conf, getParameter(conf.toString()));
        }

        addMessageToFlash(getFromMessageBundle("configuration.saved"), AsamalContext.MessageSeverity.SUCCESS);

        // reset config cache
        configBean.setProperties(null);

        redirect("configuration");
    }
}
