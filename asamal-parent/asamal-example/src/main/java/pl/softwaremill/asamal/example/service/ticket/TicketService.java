package pl.softwaremill.asamal.example.service.ticket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.json.ViewInvoice;
import pl.softwaremill.asamal.example.model.json.ViewUsers;
import pl.softwaremill.asamal.example.model.security.User;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.InvoiceStatus;
import pl.softwaremill.asamal.example.model.ticket.PaymentMethod;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.service.exception.TicketsExceededException;
import pl.softwaremill.asamal.i18n.Messages;
import pl.softwaremill.common.cdi.transaction.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

@Named("tickets")
public class TicketService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private Messages messages;

    @Inject
    private ConfigurationBean configurationBean;

    private static Logger log = LoggerFactory.getLogger(TicketService.class);

    @Transactional
    public List<TicketCategory> getTicketCategories() {
        return entityManager.createQuery("select t from TicketCategory t order by t.fromDate").getResultList();
    }

    @Transactional
    public void addTicketCategory(TicketCategory ticketCategory) throws TicketsExceededException {
        // check first if it won't be too much, once we add it
        Long maxAllowed = Long.parseLong(configurationBean.getProperty(Conf.TICKETS_MAX));

        Long allocatedTickets = countAllocatedTickets();

        if (allocatedTickets == null)
            allocatedTickets = 0l;

        long totalNewNumber = allocatedTickets + ticketCategory.getNumberOfTickets();

        if (maxAllowed < totalNewNumber) {
            throw new TicketsExceededException(messages.getFromBundle("tickets.number.exceeded",
                    maxAllowed - allocatedTickets));
        }

        entityManager.persist(ticketCategory);
    }

    @Transactional
    public TicketCategory loadCategory(Long id) {
        return entityManager.find(TicketCategory.class, id);
    }

    @Transactional
    public void updateTicketCategory(TicketCategory ticketCat) {
        entityManager.merge(ticketCat);
    }

    @Transactional
    public void deleteTicketCategory(Long id) {
        TicketCategory category = entityManager.find(TicketCategory.class, id);

        if (category.getTickets().isEmpty()) {
            entityManager.remove(category);
        } else {
            log.warn("Trying to delete non-empty ticket category " + category);
        }
    }

    @Transactional
    public Long countAllocatedTickets() {
        return (Long) entityManager.createQuery(
                "select sum(t.numberOfTickets) from TicketCategory t")
                .getSingleResult();
    }

    @Transactional
    public List<TicketCategory> getAvailableCategories() {
        return entityManager.createQuery(
                "select t from TicketCategory t where :now between t.fromDate and t.toDate order by t.name")
                .setParameter("now", new Date())
                .getResultList();
    }

    @Transactional
    public TicketCategory getTicketCategory(String name) {
        return (TicketCategory) entityManager.createQuery("select t from TicketCategory t where t.name = :name")
                .setParameter("name", name)
                .getSingleResult();
    }

    @Transactional
    public Integer getNumberOfBookedTicketsInCategory(TicketCategory ticketCategory) {
        return ((Long) entityManager.createQuery(
                "select count(t) from Ticket t where t.ticketCategory = :ticketCategory")
                .setParameter("ticketCategory", ticketCategory)
                .getSingleResult()).intValue();
    }

    @Transactional
    public void addInvoice(Invoice invoice) {
        entityManager.persist(invoice);
    }

    @Transactional
    public Invoice updateInvoice(Invoice invoice) {
        return entityManager.merge(invoice);
    }

    @Transactional
    public List<Invoice> getInvoicesForUser(User user) {
        return entityManager.createQuery("select i from Invoice i where i.user = :user").
                setParameter("user", user).getResultList();
    }

    public Invoice loadInvoice(Long invoiceId) {
        return entityManager.find(Invoice.class, invoiceId);
    }

    @Transactional
    public ViewUsers getAllSoldTickets(Integer pageNumber, Integer resultsPerPage) {
        List<String> optionLabels = entityManager.createQuery("select t.label from TicketOptionDefinition t" +
                " order by t.id")
                .getResultList();

        Query query = entityManager.createQuery(
                "select new pl.softwaremill.asamal.example.model.json.ViewTicket(t) from Ticket t" +
                        " where t.invoice.status = :status order by t.invoice.datePaid, t.id")
                .setParameter("status", InvoiceStatus.PAID);

        if (pageNumber >= 0) {
            query.setFirstResult(pageNumber * resultsPerPage).
                    setMaxResults(resultsPerPage);
        }

        return new ViewUsers(query.getResultList(), optionLabels);
    }

    @Transactional
    public Long countAllSoldTickets() {
        return (Long) entityManager.createQuery(
                "select count(t) from Ticket t" +
                        " where t.invoice.status = :status order by t.invoice.datePaid, t.id")
                .setParameter("status", InvoiceStatus.PAID)
                .getSingleResult();
    }

    @Transactional
    public Long getSoldByCategory(TicketCategory category) {
        return (Long) entityManager.createQuery(
                "select count(t) from Ticket t " +
                        "where t.ticketCategory = :category and t.invoice.status = :status")
                .setParameter("status", InvoiceStatus.PAID)
                .setParameter("category", category)
                .getSingleResult();
    }

    @Transactional
    public Long getNotPaidByCategory(TicketCategory category) {
        return (Long) entityManager.createQuery(
                "select count(t) from Ticket t " +
                        "where t.ticketCategory = :category and t.invoice.status = :status")
                .setParameter("status", InvoiceStatus.UNPAID)
                .setParameter("category", category)
                .getSingleResult();
    }

    public Long getNextInvoiceNumber(PaymentMethod method) {
        try {
            Long lastNumber = (Long) entityManager.createQuery("select max (i.invoiceNumber) from Invoice i " +
                    "where i.method = :method")
                    .setParameter("method", method)
                    .getSingleResult();

            if (lastNumber == null) {
                return 1l;
            }

            return lastNumber + 1l;
        } catch (NoResultException e) {
            return 1l;
        }
    }

    public List<ViewInvoice> getAllUnpaidInvoices(Integer pageNumber, int resultsPerPage, String search) {
        Query query = getQuery(search);

        if (pageNumber >= 0) {
            query.setFirstResult(pageNumber * resultsPerPage).
                    setMaxResults(resultsPerPage);
        }

        return query.getResultList();
    }

    public Long countAllUnpaidInvoices() {
        return (Long) entityManager.createQuery(
                "select count(i) from Invoice i" +
                        " where i.status = :status")
                .setParameter("status", InvoiceStatus.UNPAID).getSingleResult();
    }

    private Query getQuery(String search) {
        String queryStr = "select new pl.softwaremill.asamal.example.model.json.ViewInvoice(i) from Invoice i" +
                " where i.status = :status ";

        Long searchLong = null;
        boolean shouldSearch = search != null && !search.isEmpty();

        if (shouldSearch) {
            queryStr += " and ( ";

            try {
                searchLong = Long.valueOf(search);

                queryStr += "i.id = :searchLong or ";
            } catch (NumberFormatException e) {
                // ignore, just don't add id search
            }

            queryStr += "lower(i.companyName) like :search or lower(i.name) like :search) ";
        }

        queryStr += "order by i.id";

        Query query = entityManager.createQuery(queryStr)
                .setParameter("status", InvoiceStatus.UNPAID);

        if (shouldSearch) {
            query.setParameter("search", "%"+search.toLowerCase()+"%");
            if (searchLong != null) {
                query.setParameter("searchLong", searchLong);
            }
        }

        return query;
    }
}
