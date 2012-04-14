package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.PageParameters;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Filters;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.Post;
import pl.softwaremill.asamal.example.filters.AuthorizationFilter;
import pl.softwaremill.asamal.example.logic.auth.LoginBean;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.logic.invoice.InvoiceTotal;
import pl.softwaremill.asamal.example.logic.invoice.InvoiceTotals;
import pl.softwaremill.asamal.example.logic.invoice.InvoiceTotalsCounter;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.ticket.Discount;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.InvoiceStatus;
import pl.softwaremill.asamal.example.model.ticket.PaymentMethod;
import pl.softwaremill.asamal.example.model.ticket.Ticket;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.service.email.EmailService;
import pl.softwaremill.asamal.example.service.ticket.TicketService;
import pl.softwaremill.common.paypal.button.PaypalButtonGenerator;

import javax.inject.Inject;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Tickets controller
 *
 * User: szimano
 */
@Controller("tickets")
@Filters(AuthorizationFilter.class)
public class Tickets extends ControllerBean implements Serializable {
    
    private final static Integer maxTickets = 5;
    private static final String NUMBER_OF_TICKETS_PREFIX = "numberOfTickets-";

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

    @Get
    public void buy() {
        putInContext("toBePaid", 0);
    }

    @Post
    public void changeNumber() {
        bindTickets();

        Map<String, Integer> toBuy = new HashMap<String, Integer>();

        for (String paramName : getParameterNames()) {
            if (paramName.startsWith(NUMBER_OF_TICKETS_PREFIX)) {
                toBuy.put(paramName.substring(NUMBER_OF_TICKETS_PREFIX.length()),
                        new Integer(getParameter(paramName)));
            }
        }
        putInContext("ticketsToBuy", toBuy);
    }

    @Post
    public void doBuy() {
        bindTickets();

        boolean allGood = validateBean("invoice", invoice);

        String discountCode = getParameter("invoice.discount");
        Discount discount = null;

        if (discountCode != null && discountCode.length() > 0) {
            discount = ticketService.loadDiscount(discountCode);

            if (discount == null) {
                allGood = false;
                addMessageToFlash(getFromMessageBundle("discount.code.wrong"), AsamalContext.MessageSeverity.ERR);
            }
            else {
                if (discount.getNumberOfUses() > 0 && discount.getNumberOfUses() <= discount.getNumberOfTickets()) {
                    allGood = false;
                    addMessageToFlash(getFromMessageBundle("discount.code.expired"), AsamalContext.MessageSeverity.ERR);
                }

                invoice.setDiscount(discount);
            }

        }

        String paymentMethod = getParameter("paymentMethod");

        if (paymentMethod != null) {
            invoice.setStatus(InvoiceStatus.UNPAID);
            invoice.setMethod(PaymentMethod.valueOf(paymentMethod.toUpperCase()));
        }
        else {
            allGood = false;
            addMessageToFlash(getFromMessageBundle("tickets.choose.payment"), AsamalContext.MessageSeverity.ERR);
        }

        invoice.setUser(loginBean.getUser());
        Date dateCreated = new Date();
        invoice.setDateCreated(dateCreated);
        invoice.setDueDate(new Date(dateCreated.getTime() + Invoice.SEVEN_DAYS));

        for (int i = 0; i < ticketsByCategory.length; i++) {
            for (int j = 0; j < ticketsByCategory[i].length; j++) {
                if (!validateBean("ticketsByCategory["+i+"]["+j+"]", ticketsByCategory[i][j])) {
                    allGood = false;
                }
            }
        }

        // set the tickets

        Set<Ticket> allTickets = new HashSet<Ticket>();
        for (Ticket[] tickets : ticketsByCategory) {
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
        }
        else {
            ticketService.addInvoice(invoice);

            addMessageToFlash(getFromMessageBundle("tickets.book.ok"), AsamalContext.MessageSeverity.SUCCESS);
            redirect("pay", new PageParameters(invoice.getId()));

            // schedule thank you email
            emailService.sendThankYouEmail(invoice);
        }
    }

    private void bindTickets() {
        // initiate first

        ticketsByCategory = new Ticket[getAvailableCategories().size()][];
        ArrayList<String> paramNames = new ArrayList<String>();

        for (int i = 0; i < getAvailableCategories().size(); i++) {
            int numberOfAttendees = Integer.parseInt(
                    getParameter(NUMBER_OF_TICKETS_PREFIX + getAvailableCategories().get(i).getIdName())
            );
            ticketsByCategory[i] = new Ticket[numberOfAttendees];
            for (int j = 0; j < numberOfAttendees; j++) {
                ticketsByCategory[i][j] = new Ticket();
                ticketsByCategory[i][j].setTicketCategory(getAvailableCategories().get(i));
                ticketsByCategory[i][j].setInvoice(invoice);

                String attendeePrefix = "ticketsByCategory["+i+"]["+j+"]";
                paramNames.add(attendeePrefix+".firstName");
                paramNames.add(attendeePrefix+".lastName");
            }
        }

        // bind attendees
        doOptionalAutoBinding(paramNames.toArray(new String[paramNames.size()]));

        // bind invoice
        doOptionalAutoBinding("invoice.name", "invoice.companyName", "invoice.vat",
                "invoice.address", "invoice.postalCode", "invoice.city", "invoice.country");

        // count toBePaid
        Integer toBePaid = 0;

        for (int i = 0; i < getAvailableCategories().size(); i++) {
            toBePaid += getAvailableCategories().get(i).getPrice() * ticketsByCategory[i].length;
        }

        putInContext("toBePaid", toBePaid);
    }

    @Get
    public void pay() {
        invoice = ticketService.loadInvoice(Long.parseLong(getExtraPath()[0]));

        putInContext("invoice", invoice);
    }

    public String paypalButton() {
        PaypalButtonGenerator pbg = new PaypalButtonGenerator(configurationBean.getProperty(Conf.PAYPAL_EMAIL),
                configurationBean.getBooleanProperty(Conf.PAYPAL_SANDBOX),
                configurationBean.getProperty(Conf.INVOICE_CURRENCY)).withInvoiceNumber(String.valueOf(invoice.getId()));

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
