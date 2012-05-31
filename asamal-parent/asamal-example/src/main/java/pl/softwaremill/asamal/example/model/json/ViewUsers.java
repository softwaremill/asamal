package pl.softwaremill.asamal.example.model.json;

import java.util.List;

public class ViewUsers {

    List<ViewTicket> tickets;

    List<String> ticketOptionLabels;

    public ViewUsers(List<ViewTicket> tickets, List<String> ticketOptionLabels) {
        this.tickets = tickets;
        this.ticketOptionLabels = ticketOptionLabels;
    }

    public List<ViewTicket> getTickets() {
        return tickets;
    }

    public List<String> getTicketOptionLabels() {
        return ticketOptionLabels;
    }

    @Override
    public String toString() {
        return "ViewUsers{" +
                "tickets=" + tickets +
                ", ticketOptionLabels=" + ticketOptionLabels +
                '}';
    }
}
