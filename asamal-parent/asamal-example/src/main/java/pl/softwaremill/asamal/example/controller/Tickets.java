package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Filters;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.Post;
import pl.softwaremill.asamal.example.filters.AuthorizationFilter;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.service.ticket.TicketService;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Get
    public void buy() {
    }

    @Post
    public void changeNumber() {
        Map<String, Integer> toBuy = new HashMap<String, Integer>();

        for (String paramName : getParameterNames()) {
            if (paramName.startsWith(NUMBER_OF_TICKETS_PREFIX)) {
                toBuy.put(paramName.substring(NUMBER_OF_TICKETS_PREFIX.length()),
                        new Integer(getParameter(paramName)));
            }
        }
        putInContext("ticketsToBuy", toBuy);
    }

    public List<TicketCategory> getAvailableCategories() {
        return ticketService.getAvailableCategories();
    }
    
    public Integer getMaxTickets(TicketCategory category) {
        Integer soldTickets = ticketService.getSoldTicketsInCategory(category);
        Integer ticketsLeft = category.getNumberOfTickets() - soldTickets;

        return (ticketsLeft > maxTickets) ? maxTickets : ticketsLeft;
    }
}
