package pl.softwaremill.asamal.example.controller;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import org.apache.velocity.context.Context;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.persistence.Transactional;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.softwaremill.asamal.AsamalParameters;
import pl.softwaremill.asamal.example.logic.admin.DiscountService;
import pl.softwaremill.asamal.example.logic.auth.LoginBean;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.logic.invoice.InvoiceTotalsCounter;
import pl.softwaremill.asamal.example.model.BaseEntity;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.conf.ConfigurationProperty;
import pl.softwaremill.asamal.example.model.json.ViewUsers;
import pl.softwaremill.asamal.example.model.security.User;
import pl.softwaremill.asamal.example.model.ticket.*;
import pl.softwaremill.asamal.example.service.conf.ConfigurationService;
import pl.softwaremill.asamal.example.service.email.EmailService;
import pl.softwaremill.asamal.example.service.exception.TicketsExceededException;
import pl.softwaremill.asamal.example.service.hash.StringHasher;
import pl.softwaremill.asamal.example.service.ticket.TicketOptionService;
import pl.softwaremill.asamal.example.service.ticket.TicketService;
import pl.softwaremill.asamal.example.service.user.UserService;
import pl.softwaremill.asamal.example.service.user.exception.UserExistsException;
import pl.softwaremill.asamal.i18n.Messages;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(Arquillian.class)
@Ignore
public class TicketsTest {

    @Deployment
    public static JavaArchive createDeployment()
    {
        return ShrinkWrap.create(JavaArchive.class, "test.jar")
                .addPackages(true, "org.fest")
//                .addPackages(true, "org.apache.velocity")
                .addPackages(true, "org.jdom")
                .addPackages(true, "pl.softwaremill.common.task")
                .addPackages(true, "pl.softwaremill.asamal.controller")
                .addClass(AsamalParameters.class)
                .addClass(Context.class)
                .addClasses(Multimap.class, Multiset.class)
                .addPackage(TicketService.class.getPackage())
                .addPackage(TicketsExceededException.class.getPackage())
                .addPackage(Messages.class.getPackage())
                .addPackage(Invoice.class.getPackage())
                .addPackage(User.class.getPackage())
                .addPackage(BaseEntity.class.getPackage())
                .addPackage(TicketOptionService.class.getPackage())
                .addClasses(ConfigurationBean.class, ConfigurationService.class, ConfigurationProperty.class, Conf.class)
                .addPackage(ViewUsers.class.getPackage())
                .addPackage(InvoiceTotalsCounter.class.getPackage())
                .addClass(Predicate.class)
                .addClass(Collections2.class)
                .addClass(DiscountService.class)
                .addClass(EmailService.class)
                .addClass(UserService.class)
                .addClass(UserExistsException.class)
                .addClass(StringHasher.class)
                .addPackage(LoginBean.class.getPackage())
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml")
                .addAsResource(EmptyAsset.INSTANCE, "messages.properties")
                .addAsManifestResource("test-persistence.xml", "persistence.xml");
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private TicketService ticketService;

    @Test
    @UsingDataSet("ticket-categories.yml")
    @Transactional
    public void shouldPersistInvoice() {
        // given
        TicketCategory tc = entityManager.find(TicketCategory.class, 1l);

        User u = entityManager.find(User.class, 1l);

        Set<Ticket> tickets = new HashSet<Ticket>();

        tickets.add(new Ticket("Tomek", "Szymanski", tc));

        Invoice i = new Invoice(tickets, "Paying", "Customer", "123-456", "Address", "00123", "City", "Country",
                InvoiceStatus.UNPAID, PaymentMethod.TRANSFER, u, new Date(), null, null, true, null, null);
        // when
        ticketService.addInvoice(i);

        // then
        i = entityManager.find(Invoice.class, i.getId());
        assertThat(i.getName()).isEqualTo("Paying");
        assertThat(i.getTickets()).hasSize(1);
        assertThat(i.getMethod()).isEqualTo(PaymentMethod.TRANSFER);
        Ticket ticket = i.getTickets().iterator().next();
        assertThat(ticket.getFirstName()).isEqualTo("Tomek");
        assertThat(ticket.getLastName()).isEqualTo("Szymanski");
        assertThat(ticket.getTicketCategory()).isEqualTo(tc);
    }

}
