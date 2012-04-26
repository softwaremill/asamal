package pl.softwaremill.asamal.example.model.json;

import pl.softwaremill.asamal.example.model.ticket.Ticket;
import pl.softwaremill.asamal.example.model.ticket.TicketOption;

import java.util.ArrayList;
import java.util.List;

public class ViewTicket {
    private String firstName;

    private String lastName;

    private String category;

    private List<String> optionValues;

    public ViewTicket(Ticket ticket) {
        this.firstName = ticket.getFirstName();
        this.lastName = ticket.getLastName();
        this.optionValues = new ArrayList<String>();
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
}
