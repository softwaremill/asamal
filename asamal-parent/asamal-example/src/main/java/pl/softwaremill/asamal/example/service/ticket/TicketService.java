package pl.softwaremill.asamal.example.service.ticket;

import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.service.exception.TicketsExceededException;
import pl.softwaremill.asamal.i18n.Messages;
import pl.softwaremill.common.cdi.transaction.Transactional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

@Named("tickets")
public class TicketService {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Inject
    private Messages messages;

    @Transactional
    public List<TicketCategory> getTicketCategories() {
        return entityManager.createQuery("select t from TicketCategory t order by t.fromDate").getResultList();
    }

    @Transactional
    public void addTicketCategory(TicketCategory ticketCategory) throws TicketsExceededException{
        // check first if it won't be too much, once we add it
        if (!ticketCategory.getName().equals(TicketCategory.ALL_CATEGORY)) {
            Long maxAllowed = getAllTicketCategory().getNumberOfTickets().longValue();

            Long allocatedTickets = countAllocatedTickets();

            if (allocatedTickets == null)
                allocatedTickets = 0l;

            long totalNewNumber = allocatedTickets + ticketCategory.getNumberOfTickets();

            if (maxAllowed < totalNewNumber) {
                throw new TicketsExceededException(messages.getFromBundle("tickets.number.exceeded",
                        maxAllowed - allocatedTickets));
            }
        }
        entityManager.persist(ticketCategory);
    }

    @Transactional
    public TicketCategory loadCategory(Long id) {
        return entityManager.find(TicketCategory.class, id);
    }
    
    @Transactional
    public TicketCategory getAllTicketCategory() {
        return (TicketCategory) entityManager.createQuery("select t from TicketCategory t where t.name = :name")
                .setParameter("name", TicketCategory.ALL_CATEGORY)
                .getSingleResult();
    }

    @Transactional
    public void updateTicketCategory(TicketCategory ticketCat) {
        entityManager.merge(ticketCat);
    }

    @Transactional
    public void deleteTicketCategory(Long id) {
        TicketCategory category = entityManager.find(TicketCategory.class, id);

        if (TicketCategory.ALL_CATEGORY.equals(category.getName())) {
            throw new RuntimeException("You cannot delete ALL category !");
        }
        entityManager.remove(category);
    }

    @Transactional
    public Long countAllocatedTickets() {
        return (Long) entityManager.createQuery(
                "select sum(t.numberOfTickets) from TicketCategory t where t.name != :all")
                .setParameter("all", TicketCategory.ALL_CATEGORY)
                .getSingleResult();
    }
    
    @Transactional
    public List<TicketCategory> getAvailableCategories() {
        return entityManager.createQuery(
                "select t from TicketCategory t where t.name != :all and :now between t.fromDate and t.toDate order by t.name")
                .setParameter("now", new Date())
                .setParameter("all", TicketCategory.ALL_CATEGORY)
                .getResultList();
    }

    @Transactional
    public TicketCategory getTicketCategory(String name) {
        return (TicketCategory) entityManager.createQuery("select t from TicketCategory t where t.name = :name")
                .setParameter("name", name)
                .getSingleResult();
    }
    
    @Transactional
    public Integer getSoldTicketsInCategory(TicketCategory ticketCategory) {
        return ((Long) entityManager.createQuery(
                "select count(t) from Ticket t where t.ticketCategory = :ticketCategory")
                .setParameter("ticketCategory", ticketCategory)
                .getSingleResult()).intValue();
    }
}
