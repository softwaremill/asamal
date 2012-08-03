package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.DownloadDescription;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Download;
import pl.softwaremill.asamal.controller.annotation.Filters;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.Json;
import pl.softwaremill.asamal.controller.annotation.PathParameter;
import pl.softwaremill.asamal.controller.annotation.Post;
import pl.softwaremill.asamal.controller.annotation.RequestParameter;
import pl.softwaremill.asamal.example.filters.AuthorizationFilter;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.json.ViewInvoice;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.InvoiceStatus;
import pl.softwaremill.asamal.example.model.ticket.Ticket;
import pl.softwaremill.asamal.example.service.admin.AdminService;
import pl.softwaremill.asamal.example.service.email.EmailService;
import pl.softwaremill.asamal.example.service.ticket.TicketService;
import pl.softwaremill.common.cdi.security.Secure;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller("payments")
@Filters(AuthorizationFilter.class)
@Secure("#{login.admin}")
public class Payments extends ControllerBean {

    private final static SimpleDateFormat monthDateFormat = new SimpleDateFormat("MMMM yyyy");

    @Inject
    private TicketService ticketService;

    @Inject
    private EmailService emailService;

    @Inject
    private AdminService adminService;

    @Inject
    private ConfigurationBean configurationBean;

    @Get
    public void approvePayments() {
        putInContext("totalTickets", ticketService.countAllInvoices());
    }

    @Json
    public List<ViewInvoice> loadAttendants() {
        Integer pageNumber = Integer.valueOf(getExtraPath()[0]);
        String search = (getExtraPath().length > 1 ? getExtraPath()[1] : null);

        return ticketService.getAllInvoices(pageNumber, 10, search);
    }

    @Post
    public void approve() {
        try {
            Long invoiceId = Long.parseLong(getParameter("invoiceId"));

            Date datePaid = Admin.dateFormat.parse(getParameter("paymentDate"));

            Invoice invoice = ticketService.loadInvoice(invoiceId);

            invoice.setDatePaid(datePaid);
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setInvoiceNumber(ticketService.getNextInvoiceNumber(invoice.getMethod()));

            invoice = ticketService.updateInvoice(invoice);

            emailService.sendTransferAcceptedEmail(invoice);

            putInContext("totalTickets", ticketService.countAllInvoices());
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

        putInContext("totalTickets", ticketService.countAllInvoices());
    }

    @Get
    public void accounting() {

    }

    @Post
    public void closeMonth(@RequestParameter("accountingMonth") String month) {
        try {
            Calendar accMonth = Calendar.getInstance();

            accMonth.setTime(monthDateFormat.parse(month));

            adminService.closeAccountingMonth(accMonth);

            addMessageToFlash(getFromMessageBundle("accounting.closed", month), AsamalContext.MessageSeverity.SUCCESS);

            redirect("accounting");
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Download(params = "/year/month")
    public DownloadDescription downloadInvoices(@PathParameter("year") Integer year,
                                                @PathParameter("month") Integer month) {
        Calendar accMonth = Calendar.getInstance();
        accMonth.set(Calendar.YEAR, year);
        accMonth.set(Calendar.MONTH, month);
        accMonth.set(Calendar.DAY_OF_MONTH, 1);
        accMonth.set(Calendar.HOUR, 0);
        accMonth.set(Calendar.MINUTE, 0);
        accMonth.set(Calendar.SECOND, 0);
        accMonth.set(Calendar.MILLISECOND, 0);

        return adminService.generatePDFInvoicesForMonth(accMonth);
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
}

