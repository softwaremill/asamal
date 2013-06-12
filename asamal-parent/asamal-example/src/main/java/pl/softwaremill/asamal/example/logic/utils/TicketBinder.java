package pl.softwaremill.asamal.example.logic.utils;

import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.example.controller.Tickets;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.Ticket;
import pl.softwaremill.asamal.example.model.ticket.TicketCategory;
import pl.softwaremill.asamal.example.model.ticket.TicketOption;
import pl.softwaremill.asamal.example.model.ticket.TicketOptionDefinition;
import pl.softwaremill.asamal.example.service.ticket.TicketOptionService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TicketBinder {

    @Inject
    private TicketOptionService optionService;

    private static final String NUMBER_OF_TICKETS_PREFIX = "numberOfTickets-";

    public void bindInvoiceDetails(ControllerBean bean) {
        // bind invoice
        bean.doOptionalAutoBinding("invoice.name", "invoice.companyName", "invoice.vat",
                "invoice.address", "invoice.postalCode", "invoice.city", "invoice.country", "invoice.notes");
    }

    public void bindTickets(Tickets bean, List<TicketCategory> availableCategories, Invoice invoice) {
        // initiate first

        Ticket[][] ticketsByCategory = new Ticket[availableCategories.size()][];
        bean.setTicketsByCategory(ticketsByCategory);

        ArrayList<String> paramNames = new ArrayList<String>();

        List<TicketOptionDefinition> optionDefinitions = optionService.getAllOptionDefinitions();

        for (int i = 0; i < availableCategories.size(); i++) {
            String numberOfTicketsPerCategory = bean.getParameter(
                    NUMBER_OF_TICKETS_PREFIX + availableCategories.get(i).getIdName());

            int numberOfAttendees;

            if (numberOfTicketsPerCategory != null &&
                    ((numberOfAttendees = Integer.parseInt(numberOfTicketsPerCategory)) > 0)) {
                ticketsByCategory[i] = new Ticket[numberOfAttendees];
                for (int j = 0; j < numberOfAttendees; j++) {
                    ticketsByCategory[i][j] = new Ticket();
                    ticketsByCategory[i][j].setTicketCategory(availableCategories.get(i));
                    ticketsByCategory[i][j].setInvoice(invoice);

                    String attendeePrefix = "ticketsByCategory[" + i + "][" + j + "]";
                    paramNames.add(attendeePrefix + ".firstName");
                    paramNames.add(attendeePrefix + ".lastName");

                    List<TicketOption> options = new ArrayList<TicketOption>();
                    for (int k = 0; k < optionDefinitions.size(); k++) {
                        TicketOptionDefinition optionDefinition = optionDefinitions.get(k);

                        TicketOption ticketOption = new TicketOption(optionDefinition, ticketsByCategory[i][j]);
                        ticketOption.setValue(bean.getParameter("ticketOption[" + i + "][" + j + "][" + k + "]"));
                        options.add(ticketOption);
                    }

                    ticketsByCategory[i][j].setOptions(options);
                }
            } else {
                ticketsByCategory[i] = null;
            }
        }

        // bind attendees
        bean.doOptionalAutoBinding(paramNames.toArray(new String[paramNames.size()]));

        bindInvoiceDetails(bean);

        // count toBePaid
        Integer toBePaid = 0;

        for (int i = 0; i < availableCategories.size(); i++) {
            toBePaid += availableCategories.get(i).getPrice() * (ticketsByCategory[i] == null ? 0 :
                    ticketsByCategory[i].length);
        }

        bean.putInContext("toBePaid", toBePaid);

        Map<String, Integer> toBuy = new HashMap<String, Integer>();

        for (String paramName : bean.getParameterNames()) {
            if (paramName.startsWith(NUMBER_OF_TICKETS_PREFIX)) {
                toBuy.put(paramName.substring(NUMBER_OF_TICKETS_PREFIX.length()),
                        new Integer(bean.getParameter(paramName)));
            }
        }
        bean.putInContext("ticketsToBuy", toBuy);
    }
}
