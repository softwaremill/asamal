package pl.softwaremill.asamal.example.service.ticket;

import pl.softwaremill.asamal.example.model.ticket.TicketOptionDefinition;
import pl.softwaremill.asamal.example.model.ticket.TicketOptionType;
import pl.softwaremill.common.cdi.transaction.Transactional;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Named("options")
@Transactional
public class TicketOptionService {

    @PersistenceContext
    private EntityManager entityManager;

    public List<TicketOptionDefinition> getAllOptions() {
        return entityManager.createQuery("select d from TicketOptionDefinition d order by d.label").getResultList();
    }

    public void addNewOption(TicketOptionDefinition optionDefinition) {
        entityManager.persist(optionDefinition);
    }

    public TicketOptionDefinition updateOption(TicketOptionDefinition optionDefinition) {
        return entityManager.merge(optionDefinition);
    }

    public TicketOptionType[] getTypes() {
        return TicketOptionType.values();
    }

    public TicketOptionDefinition loadOption(Long id) {
        return entityManager.find(TicketOptionDefinition.class, id);
    }

    public void remove(Long id) {
        entityManager.remove(loadOption(id));
    }
}
