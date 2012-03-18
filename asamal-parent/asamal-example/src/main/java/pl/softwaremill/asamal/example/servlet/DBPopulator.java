package pl.softwaremill.asamal.example.servlet;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DateTimeConverter;
import pl.softwaremill.asamal.example.model.security.User;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.service.exception.TicketsExceededException;
import pl.softwaremill.asamal.example.service.hash.StringHasher;
import pl.softwaremill.asamal.example.service.ticket.TicketService;
import pl.softwaremill.asamal.example.service.user.UserService;

import javax.annotation.sql.DataSourceDefinition;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Date;

@WebListener
@DataSourceDefinition(
        name = "java:jboss/datasources/AsamalExampleDS",
        url = "jdbc:mysql://localhost:3306/asamal-example",
        user = "root",
        className = "com.mysql.jdbc.jdbc2.optional.MysqlDataSource"
)
public class DBPopulator implements ServletContextListener{
    
    @Inject
    private UserService userService;
    
    @Inject
    private TicketService ticketService;

    @Inject
    private StringHasher stringHasher;

    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        if (entityManager.createQuery("select u from User u").getResultList().size() > 0) {
            // db is populated already
            return;
        }

        // create users
        userService.createNewUser(new User("szimano@szimano.org", stringHasher.encode("szimano"), true));

        // create main tickets
        try {
            ticketService.addTicketCategory(new TicketCategory(new Date(0l), null, 100, TicketCategory.ALL_CATEGORY,
                    "Total number of tickets", 0));
            ticketService.addTicketCategory(
                    new TicketCategory(new Date(),
                            new Date(new Date().getTime() + 3 * 24 * 60 * 60 * 1000), 50, "Early Birds",
                    "Early Birds", 300));
        } catch (TicketsExceededException e) {
            throw new RuntimeException(e);
        }

        // set converter
        DateTimeConverter dtConverter = new DateConverter();
        dtConverter.setPattern("yyyy-MM-dd");
        ConvertUtils.register(dtConverter, Date.class);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
