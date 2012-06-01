package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.AsamalContext;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Filters;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.PathParameter;
import pl.softwaremill.asamal.controller.annotation.Post;
import pl.softwaremill.asamal.controller.annotation.RequestParameter;
import pl.softwaremill.asamal.example.filters.AuthorizationFilter;
import pl.softwaremill.asamal.example.logic.admin.DiscountService;
import pl.softwaremill.asamal.example.logic.auth.RegisterBean;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.security.User;
import pl.softwaremill.asamal.example.model.ticket.Discount;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.model.ticket.TicketOptionDefinition;
import pl.softwaremill.asamal.example.service.admin.AdminService;
import pl.softwaremill.asamal.example.service.conf.ConfigurationService;
import pl.softwaremill.asamal.example.service.email.EmailService;
import pl.softwaremill.asamal.example.service.exception.TicketsExceededException;
import pl.softwaremill.asamal.example.service.ticket.TicketOptionService;
import pl.softwaremill.asamal.example.service.ticket.TicketService;
import pl.softwaremill.common.cdi.security.Secure;
import pl.softwaremill.common.cdi.transaction.Transactional;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.List;

@Controller("admin")
@Filters(AuthorizationFilter.class)
@Secure("#{login.admin}")
public class Admin extends ControllerBean {


    private TicketCategory ticketCat = new TicketCategory();

    @Inject
    private TicketService ticketService;

    @Inject
    private DiscountService discountService;

    @Inject
    private AdminService adminService;

    @Inject
    private EmailService emailService;

    @Inject
    private RegisterBean registerBean;

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



    private Discount discount = new Discount();

    @Get
    public void discounts() {
    }

    private void bindDiscount() {
        doOptionalAutoBinding("discount.discountCode", "discount.discountAmount", "discount.numberOfUses",
                "discount.lateDiscount");

        if ("Unlimited".equals(getParameter("discount.unlimited"))) {
            discount.setNumberOfUses(-1);
        }
    }

    @Post
    public void addDiscount() {
        bindDiscount();

        boolean beanOk = validateDiscount();

        if (beanOk) {
            discountService.addDiscount(discount);

            addMessageToFlash("Discount added", AsamalContext.MessageSeverity.SUCCESS);

            redirect("discounts");
        } else {
            includeView("discounts");
        }
    }

    @Get(params = "/id")
    public void editDiscount(@PathParameter("id") Long id) {
        discount = discountService.loadDiscount(id);

        addObjectToFlash("discount", discount);
    }

    @Post
    public void doEditDiscount() {
        discount = (Discount) getObjectFromFlash("discount");

        bindDiscount();

        boolean beanOk = validateDiscount();

        if (beanOk) {
            discountService.mergeDiscount(discount);
            addMessageToFlash("Discount updated", AsamalContext.MessageSeverity.SUCCESS);

            redirect("discounts");
        }
        else {
            addObjectToFlash("discount", discount);

            includeView("editDiscount");
        }
    }

    private boolean validateDiscount() {
        boolean ok = validateBean("discount", discount);

        if (discount.getLateDiscount() != null && !discount.getLateDiscount().isEmpty()) {
           if (discountService.loadDiscount(discount.getLateDiscount()) == null) {
               addMessageToFlash("You are trying to use non-existing late discount with code: "+
                       discount.getLateDiscount(), AsamalContext.MessageSeverity.ERR);

               ok = false;
           }
        }

        return ok;
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
    public void deleteTicketCategory(@RequestParameter("id") String id) {
        ticketService.deleteTicketCategory(Long.valueOf(id));
    }

    @Post
    @Transactional
    public void deleteDiscount(@RequestParameter("id") String id) {
        discountService.deleteDiscount(Long.valueOf(id));
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

    @Get
    public void registerAdmin() { }

    @Post
    @Transactional
    public void doRegisterAdmin() {
        User user = registerBean.registerUser(this);

        if (user == null) {
            addMessageToFlash("Admin user creation failed", AsamalContext.MessageSeverity.ERR);
        }
        else {
            adminService.makeUserAnAdmin(user);

            addMessageToFlash("Admin user created", AsamalContext.MessageSeverity.SUCCESS);
        }

        redirect("registerAdmin");
    }

}
