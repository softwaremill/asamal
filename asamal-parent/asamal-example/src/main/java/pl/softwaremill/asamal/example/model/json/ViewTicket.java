package pl.softwaremill.asamal.example.model.json;

import pl.softwaremill.asamal.example.model.ticket.Ticket;
import pl.softwaremill.asamal.example.model.ticket.TicketOption;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ViewTicket {
    private String firstName;

    private String lastName;

    private String category;

    private String company;

    private List<String> optionValues;

    public ViewTicket(Ticket ticket) {
        this.firstName = ticket.getFirstName();
        this.lastName = ticket.getLastName();
        this.company = ticket.getInvoice().getCompanyName();
        this.optionValues = new ArrayList<String>();

        Collections.sort(ticket.getOptions(), new Comparator<TicketOption>() {
            @Override
            public int compare(TicketOption ticketOption, TicketOption ticketOption1) {
                return ticketOption.getOptionDefinition().getId().
                        compareTo(ticketOption1.getOptionDefinition().getId());
            }
        });

        for (TicketOption option : ticket.getOptions()) {
            optionValues.add(option.getValue());
        }
        this.category = ticket.getTicketCategory().getName();
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public List<String> getOptionValues() {
        return optionValues;
    }

    public String getCategory() {
        return category;
    }

    public String getCompany() {
        return company;
    }

    @Override
    public String toString() {
        return "ViewTicket{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", category='" + category + '\'' +
                ", optionValues=" + optionValues +
                '}';
    }
}
