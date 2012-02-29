package pl.softwaremill.asamal.example.servlet;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DateTimeConverter;
import pl.softwaremill.asamal.example.model.security.User;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.service.hash.StringHasher;
import pl.softwaremill.asamal.example.service.ticket.TicketService;
import pl.softwaremill.asamal.example.service.user.UserService;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Date;

@WebListener
public class DBPopulator implements ServletContextListener{
    
    @Inject
    private UserService userService;
    
    @Inject
    private TicketService ticketService;

    @Inject
    private StringHasher stringHasher;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // create users
        userService.createNewUser(new User("szimano@szimano.org", stringHasher.encode("szimano"), true));

        // create main tickets
        ticketService.addTicketCategory(new TicketCategory(new Date(0l), null, 100, TicketCategory.ALL_CATEGORY,
                "Total number of tickets", 0));
        ticketService.addTicketCategory(new TicketCategory(new Date(), new Date(), 50, "Early Birds",
                "Early Birds", 300));

        // set converter
        DateTimeConverter dtConverter = new DateConverter();
        dtConverter.setPattern("yyyy-MM-dd");
        ConvertUtils.register(dtConverter, Date.class);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
