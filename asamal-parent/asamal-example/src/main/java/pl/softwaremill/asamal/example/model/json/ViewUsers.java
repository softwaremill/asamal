package pl.softwaremill.asamal.example.model.json;

import java.util.List;

public class ViewUsers {

    List<ViewUsers> users;

    List<String> ticketOptionLabels;

    public ViewUsers(List<ViewUsers> users, List<String> ticketOptionLabels) {
        this.users = users;
        this.ticketOptionLabels = ticketOptionLabels;
    }

    public List<ViewUsers> getUsers() {
        return users;
    }

    public List<String> getTicketOptionLabels() {
        return ticketOptionLabels;
    }
}
