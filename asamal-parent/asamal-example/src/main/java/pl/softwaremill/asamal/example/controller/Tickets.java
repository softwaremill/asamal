package pl.softwaremill.asamal.example.controller;

import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.PageParameters;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Filters;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.PathParameter;
import pl.softwaremill.asamal.controller.annotation.Post;
import pl.softwaremill.asamal.example.filters.AuthorizationFilter;
import pl.softwaremill.asamal.example.logic.admin.DiscountService;
import pl.softwaremill.asamal.example.logic.auth.LoginBean;
import pl.softwaremill.asamal.example.logic.auth.RegisterBean;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.logic.invoice.InvoiceTotal;
import pl.softwaremill.asamal.example.logic.invoice.InvoiceTotals;
import pl.softwaremill.asamal.example.logic.invoice.InvoiceTotalsCounter;
import pl.softwaremill.asamal.example.logic.utils.TicketBinder;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.ticket.Discount;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.InvoiceStatus;
import pl.softwaremill.asamal.example.model.ticket.PaymentMethod;
import pl.softwaremill.asamal.example.model.ticket.Ticket;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.service.email.EmailService;
import pl.softwaremill.asamal.example.service.ticket.TicketService;
import pl.softwaremill.common.cdi.transaction.Transactional;
import pl.softwaremill.common.paypal.button.PaypalButtonGenerator;

import javax.inject.Inject;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tickets controller
 * <p/>
 * User: szimano
 */
@Controller("tickets")
public class Tickets extends ControllerBean implements Serializable {

    private final static Integer maxTickets = 5;

    private final static Logger log = LoggerFactory.getLogger(Tickets.class);

    @Inject
    private TicketService ticketService;

    @Inject
    private ConfigurationBean configurationBean;

    @Inject
    private EmailService emailService;

    private Invoice invoice = new Invoice();

    private List<TicketCategory> availableCategories;

    private Ticket[][] ticketsByCategory;

    @Inject
    private LoginBean loginBean;

    @Inject
    private InvoiceTotalsCounter invoiceTotalsCounter;

    @Inject
    private TicketBinder ticketBinder;

    @Inject
    private DiscountService discountService;

    @Inject
    private RegisterBean registerBean;

    @Get
    public void buy() {
        putInContext("toBePaid", 0);

        if (loginBean.isLoggedIn()) {
            // check if the user didn't have any invoices yet, so we can prefill has invoice data
            List<Invoice> invoices = ticketService.getInvoicesForUser(loginBean.getUser());

            if (!invoices.isEmpty()) {
                try {
                    invoice = (Invoice) BeanUtils.cloneBean(invoices.get(0));

                    // clear the per-invoice specific data
                    invoice.getTickets().clear();
                    invoice.setStatus(null);
                    invoice.setMethod(null);
                    invoice.setEditable(true);
                    invoice.setDiscount(null);
                    invoice.setDueDate(null);
                    invoice.setDateCreated(null);
                    invoice.setDatePaid(null);
                } catch (IllegalAccessException e) {
                    // something went wrong, log but ignore - we still want user to buy the ticket,
                    // just show him empty one

                    log.warn("Got error when trying to clone user's invoice", e);
                } catch (InstantiationException e) {
                    log.warn("Got error when trying to clone user's invoice", e);
                } catch (InvocationTargetException e) {
                    log.warn("Got error when trying to clone user's invoice", e);
                } catch (NoSuchMethodException e) {
                    log.warn("Got error when trying to clone user's invoice", e);
                }
            }
        }
    }

    @Post
    public void changeNumber() {
        ticketBinder.bindTickets(this, getAvailableCategories(), invoice);
    }

    @Post(skipViewHash = true)
    @Transactional
    public void doBuy() {
        ticketBinder.bindTickets(this, getAvailableCategories(), invoice);

        putInContext("username", getParameter("user.username"));

        boolean allGood = validateBean("invoice", invoice) &&
                // if not logged in, try creating the user first
                (loginBean.isLoggedIn() || registerBean.registerUser(this));

        String discountCode = getParameter("invoice.discount");
        Discount discount = null;

        if (discountCode != null && discountCode.length() > 0) {
            discount = discountService.loadDiscount(discountCode);

            if (discount == null) {
                allGood = false;
                addMessageToFlash(getFromMessageBundle("discount.code.wrong"), AsamalContext.MessageSeverity.ERR);
            } else {
                Integer numberOfTicketsOnDiscount = discountService.getNumberOfUses(discount);
                int totalTickets = getTotalTickets();

                if (discount.getNumberOfUses() > 0 &&
                        discount.getNumberOfUses() < (numberOfTicketsOnDiscount + totalTickets)) {
                    allGood = false;

                    if (discount.getNumberOfUses().equals(numberOfTicketsOnDiscount)) {
                        if (discountService.shouldShowLateDiscount(discount)) {
                            Discount lateDiscount = discountService.loadDiscount(discount.getLateDiscount());

                            addMessageToFlash(getFromMessageBundle("discount.code.expired.show.late",
                                    lateDiscount.getDiscountCode(), lateDiscount.getDiscountAmount()),
                                    AsamalContext.MessageSeverity.WARN);
                        }
                        else {
                            addMessageToFlash(getFromMessageBundle("discount.code.expired"),
                                    AsamalContext.MessageSeverity.ERR);
                        }
                    }
                    else {
                        addMessageToFlash(
                                getFromMessageBundle("discount.uses.left",
                                        discount.getNumberOfUses() - numberOfTicketsOnDiscount),
                                AsamalContext.MessageSeverity.ERR);
                    }
                }

                invoice.setDiscount(discount);
            }

        }

        String paymentMethod = getParameter("paymentMethod");

        if (paymentMethod != null) {
            invoice.setStatus(InvoiceStatus.UNPAID);
            invoice.setMethod(PaymentMethod.valueOf(paymentMethod.toUpperCase()));
        } else {
            allGood = false;
            addMessageToFlash(getFromMessageBundle("tickets.choose.payment"), AsamalContext.MessageSeverity.ERR);
        }

        invoice.setUser(loginBean.getUser());
        Date dateCreated = new Date();
        invoice.setDateCreated(dateCreated);
        invoice.setDueDate(new Date(dateCreated.getTime() + Invoice.SEVEN_DAYS));

        for (int i = 0; i < ticketsByCategory.length; i++) {
            if (ticketsByCategory[i] != null) {
                for (int j = 0; j < ticketsByCategory[i].length; j++) {
                    if (!validateBean("ticketsByCategory[" + i + "][" + j + "]", ticketsByCategory[i][j])) {
                        allGood = false;
                    }

                    for (int k = 0; k < ticketsByCategory[i][j].getOptions().size(); k++) {
                        if (!validateBean("ticketOption["+i+"]["+j+"]["+k+"]", ticketsByCategory[i][j].getOptions().get(k))) {
                            allGood = false;
                        }
                    }
                }
            }
        }

        // set the tickets

        Set<Ticket> allTickets = new HashSet<Ticket>();
        for (Ticket[] tickets : ticketsByCategory) {
            if (tickets != null)
                allTickets.addAll(Arrays.asList(tickets));
        }
        invoice.setTickets(allTickets);

        if (invoice.getTickets().isEmpty()) {
            allGood = false;

            addMessageToFlash(getFromMessageBundle("tickets.no.tickets"), AsamalContext.MessageSeverity.ERR);
        }

        if (!allGood) {
            addMessageToFlash(getFromMessageBundle("tickets.validation.errors"), AsamalContext.MessageSeverity.ERR);

            includeView("buy");
        } else {
            // check the amount
            InvoiceTotals invoiceTotals = invoiceTotalsCounter.countInvoice(invoice);

            ticketService.addInvoice(invoice);

            // if the user had 100% discount, make the invoice paid
            System.out.println("TOTAL: "+invoiceTotals.getTotalAmount());
            if (invoiceTotals.getTotalAmount().equals(new BigDecimal("0.00"))) {
                invoice.setDatePaid(new Date());
                invoice.setEditable(false);
                invoice.setStatus(InvoiceStatus.PAID);

                ticketService.updateInvoice(invoice);

                addMessageToFlash(getFromMessageBundle("tickets.free.book.ok"), AsamalContext.MessageSeverity.SUCCESS);
                redirect("home", "index");
            }
            else {
                addMessageToFlash(getFromMessageBundle("tickets.book.ok"), AsamalContext.MessageSeverity.SUCCESS);
                redirect("pay", new PageParameters(invoice.getId()));
            }

            // schedule thank you email
            emailService.sendThankYouEmail(invoice);
        }
    }

    private int getTotalTickets() {
        int i = 0;

        for (Ticket[] tickets : ticketsByCategory) {
            if (tickets != null)
                i += tickets.length;
        }

        return i;
    }

    @Get(params = "/id")
    @Filters(AuthorizationFilter.class)
    public void edit(@PathParameter("id") Long invoiceId) {
        invoice = ticketService.loadInvoice(invoiceId);

        if (!loginBean.isAdmin() && !invoice.getUser().equals(loginBean.getUser())) {
            throw new RuntimeException("You are trying to edit an invoice that does not belong to you!");
        }

        if (!invoice.getEditable()) {
            addMessageToFlash(getFromMessageBundle("invoice.already.accounted",
                    configurationBean.getProperty(Conf.INVOICE_ID) + invoiceId),
                    AsamalContext.MessageSeverity.WARN);

            redirect("home", "index");
        }

        putInContext("invoice", invoice);
    }

    @Post(params = "/id")
    @Filters(AuthorizationFilter.class)
    public void doUpdate(@PathParameter("id") Long invoiceId) {
        invoice = ticketService.loadInvoice(invoiceId);

        if (!loginBean.isAdmin() && !invoice.getUser().equals(loginBean.getUser())) {
            throw new RuntimeException("You are trying to edit an invoice that does not belong to you!");
        }

        ticketBinder.bindInvoiceDetails(this);

        if (invoice.getStatus() != InvoiceStatus.PAID) {
            invoice.setMethod(PaymentMethod.valueOf(getParameter("paymentMethod").toUpperCase()));
        }

        if (validateBean("invoice", invoice)) {
            ticketService.updateInvoice(invoice);

            addMessageToFlash(getFromMessageBundle("invoice.updated"), AsamalContext.MessageSeverity.SUCCESS);

            redirect("home", "index");
        } else {
            addMessageToFlash(getFromMessageBundle("tickets.validation.errors"), AsamalContext.MessageSeverity.ERR);

            includeView("edit");
        }
    }


    @Get(params = "/id")
    @Filters(AuthorizationFilter.class)
    public void pay(@PathParameter("id") Long invoiceId) {
        invoice = ticketService.loadInvoice(invoiceId);

        if (!loginBean.isAdmin() && !invoice.getUser().equals(loginBean.getUser())) {
            throw new RuntimeException("You are trying to pay for an invoice that does not belong to you!");
        }

        putInContext("invoice", invoice);
    }

    public String paypalButton() {
        PaypalButtonGenerator pbg = new PaypalButtonGenerator(configurationBean.getProperty(Conf.PAYPAL_EMAIL),
                configurationBean.getBooleanProperty(Conf.PAYPAL_SANDBOX),
                configurationBean.getProperty(Conf.INVOICE_CURRENCY))
                .withInvoiceNumber(String.valueOf(invoice.getId()))
                .withNotifyUrl(configurationBean.getProperty(Conf.SYSTEM_URL) + "/paypal");

        InvoiceTotals invoiceTotals = invoiceTotalsCounter.countInvoice(invoice);

        for (InvoiceTotal invoiceTotal : invoiceTotals.getAllTotals()) {
            pbg.addItem(invoiceTotal.getNumberOfTickets() + " x " + invoiceTotal.getCategory().getName(),
                    invoiceTotal.getAmount().setScale(2, BigDecimal.ROUND_HALF_DOWN).toString(),
                    "0",
                    invoiceTotal.getVatAmount().setScale(2, BigDecimal.ROUND_HALF_DOWN).toString());
        }

        return pbg.build();
    }

    public List<TicketCategory> getAvailableCategories() {
        if (availableCategories == null) {
            availableCategories = ticketService.getAvailableCategories();
        }

        return availableCategories;
    }

    public Integer getMaxTickets(TicketCategory category) {
        Integer soldTickets = ticketService.getSoldTicketsInCategory(category);
        Integer ticketsLeft = category.getNumberOfTickets() - soldTickets;

        return (ticketsLeft > maxTickets) ? maxTickets : ticketsLeft;
    }

    public Ticket[][] getTicketsByCategory() {
        return ticketsByCategory;
    }

    public void setTicketsByCategory(Ticket[][] ticketsByCategory) {
        this.ticketsByCategory = ticketsByCategory;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }
}
