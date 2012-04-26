package pl.softwaremill.asamal.example.servlet;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.commons.beanutils.converters.DateTimeConverter;
import pl.softwaremill.asamal.example.converter.EnumConverter;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.security.User;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.model.ticket.TicketOptionType;
import pl.softwaremill.asamal.example.service.conf.ConfigurationService;
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
        url = "jdbc:mysql://localhost:3306/asamal-example?useUnicode=true&characterEncoding=UTF-8&characterSetResults=UTF-8",
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
    
    @Inject
    private ConfigurationService configurationService;

    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        updateConfigurations();
        
        if (entityManager.createQuery("select u from User u").getResultList().size() == 0) {
            populateDB();
        }

        // set converter
        DateTimeConverter dtConverter = new DateConverter();
        dtConverter.setPattern("yyyy-MM-dd");
        ConvertUtils.register(dtConverter, Date.class);
        ConvertUtils.register(new EnumConverter(), TicketOptionType.class);
    }

    private void updateConfigurations() {
        // if a property doesn't exist in the db, add it with the default value

        for (Conf conf : Conf.values()) {
            if (configurationService.getProperty(conf) == null) {
                configurationService.saveProperty(conf, conf.defaultValue);
            }
        }
        
    }

    private void populateDB() {
        // create users
        userService.createNewUser(new User("szimano@szimano.org", stringHasher.encode("szimano"), true));

        // create main tickets
        try {
            ticketService.addTicketCategory(
                    new TicketCategory(new Date(),
                            new Date(new Date().getTime() + 3 * 24 * 60 * 60 * 1000), 50, "Early Birds",
                            "Early Birds", 300, "Early Birds Tickets"));
        } catch (TicketsExceededException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}
