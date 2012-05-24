package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.DownloadDescription;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Download;
import pl.softwaremill.asamal.controller.annotation.Filters;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.PathParameter;
import pl.softwaremill.asamal.controller.annotation.Post;
import pl.softwaremill.asamal.controller.annotation.RequestParameter;
import pl.softwaremill.asamal.example.filters.ActiveFilter;
import pl.softwaremill.asamal.example.filters.AuthorizationFilter;
import pl.softwaremill.asamal.example.logic.admin.DiscountService;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.ticket.Discount;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.InvoiceStatus;
import pl.softwaremill.asamal.example.model.ticket.Ticket;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.model.ticket.TicketOptionDefinition;
import pl.softwaremill.asamal.example.service.admin.AdminService;
import pl.softwaremill.asamal.example.service.conf.ConfigurationService;
import pl.softwaremill.asamal.example.service.email.EmailService;
import pl.softwaremill.asamal.example.service.exception.TicketsExceededException;
import pl.softwaremill.asamal.example.service.ticket.TicketOptionService;
import pl.softwaremill.asamal.example.service.ticket.TicketService;
import pl.softwaremill.common.cdi.security.Secure;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller("admin")
@Filters({ActiveFilter.class, AuthorizationFilter.class})
@Secure("#{login.admin}")
public class Admin extends ControllerBean {

    private final static SimpleDateFormat monthDateFormat = new SimpleDateFormat("MMMM yyyy");

    private TicketCategory ticketCat = new TicketCategory();

    @Inject
    private TicketService ticketService;

    @Inject
    private DiscountService discountService;

    @Inject
    private ConfigurationBean configurationBean;

    @Inject
    private AdminService adminService;

    @Inject
    private EmailService emailService;

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

    @Get(params = "/id")
    public void editTicketCat(@PathParameter("id") Long categoryId) {
        ticketCat = ticketService.loadCategory(categoryId);

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

            emailService.sendTransferAcceptedEmail(invoice);

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

    private Discount discount = new Discount();

    @Get
    public void discounts() {
    }

    @Post
    public void addDiscount() {
        doOptionalAutoBinding("discount.discountCode", "discount.discountAmount", "discount.numberOfUses",
                "discount.lateDiscount");

        if ("Unlimited".equals(getParameter("discount.unlimited"))) {
            discount.setNumberOfUses(-1);
        }

        boolean beanOk = validateBean("discount", discount);

        if (beanOk) {
            discountService.addDiscount(discount);

            addMessageToFlash("Discount added", AsamalContext.MessageSeverity.SUCCESS);

            redirect("discounts");
        } else {
            includeView("discounts");
        }
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
            String parameter = getParameter(conf.toString());

            if (conf.isBool()) {
                if ("on".equals(parameter)) {
                    parameter = "true";
                } else {
                    parameter = "false";
                }
            }
            configurationService.saveProperty(conf, parameter);
        }

        addMessageToFlash(getFromMessageBundle("configuration.saved"), AsamalContext.MessageSeverity.SUCCESS);

        // reset config cache
        configBean.setProperties(null);

        redirect("configuration");
    }

    @Inject
    private TicketOptionService optionService;

    private TicketOptionDefinition option = new TicketOptionDefinition();

    public TicketOptionDefinition getOption() {
        return option;
    }

    public void setOption(TicketOptionDefinition option) {
        this.option = option;
    }

    @Get
    public void options() {
        putInContext("option", option);
    }

    @Post
    public void addOptionDefinition() {
        doAutoBinding("option.label", "option.type", "option.config");

        optionService.addNewOption(option);

        addMessageToFlash("Ticket option added", AsamalContext.MessageSeverity.SUCCESS);
    }

    @Get(params = "/id")
    public void editOption(@PathParameter("id") Long id) {
        putInContext("option", option = optionService.loadOption(id));

        addObjectToFlash("option", option);
    }

    @Post
    public void updateOption() {
        option = (TicketOptionDefinition) getObjectFromFlash("option");

        doAutoBinding("option.label", "option.type", "option.config");

        optionService.updateOption(option);

        redirect("options");
        addMessageToFlash("Option Updated", AsamalContext.MessageSeverity.SUCCESS);
    }

    @Post
    public void deleteOption(@RequestParameter("id") String id) {
        optionService.remove(Long.valueOf(id));

        addMessageToFlash("Option Deleted", AsamalContext.MessageSeverity.SUCCESS);
    }

    @Get
    public void email() { }

    @Post
    public void sendEmail(@RequestParameter("subject") String subject, @RequestParameter("message") String message) {
        emailService.sendEmailToAll(subject, message);

        addMessageToFlash("Emails scheduled for sending", AsamalContext.MessageSeverity.SUCCESS);

        redirect("email");
    }

    public List<Discount> getDiscounts() {
        return discountService.getDiscounts();
    }

    public Discount getDiscount() {
        return discount;
    }
}
