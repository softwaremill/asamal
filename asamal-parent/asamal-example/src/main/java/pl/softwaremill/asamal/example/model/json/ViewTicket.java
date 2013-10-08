package pl.softwaremill.asamal.example.model.json;

import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.ticket.Ticket;
import pl.softwaremill.asamal.example.model.ticket.TicketOption;
import pl.softwaremill.common.util.dependency.D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ViewTicket {
    private String firstName;

    private String lastName;

    private String category;

    private String company;

    private String email;

    private String invoiceNo;

    private List<String> optionValues;

    public ViewTicket(Ticket ticket) {
        this.firstName = ticket.getFirstName();
        this.lastName = ticket.getLastName();
        this.company = ticket.getInvoice().getCompanyName();
        this.optionValues = new ArrayList<String>();
        this.email = ticket.getInvoice().getUser().getUsername();
        this.invoiceNo = D.inject(ConfigurationBean.class).getProperty(Conf.INVOICE_ID) +
                ticket.getInvoice().getMethod().toString() + "/" + ticket.getInvoice().getInvoiceNumber();

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

    public String getEmail() {
        return email;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    @Override
    public String toString() {
        return "ViewTicket{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", category='" + category + '\'' +
                ", company='" + company + '\'' +
                ", email='" + email + '\'' +
                ", invoiceNo='" + invoiceNo + '\'' +
                ", optionValues=" + optionValues +
                '}';
    }
}
