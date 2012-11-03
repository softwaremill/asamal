package pl.softwaremill.asamal.example.controller;

import org.supercsv.io.CsvListWriter;
import org.supercsv.prefs.CsvPreference;
import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.*;
import pl.softwaremill.asamal.example.filters.ActiveFilter;
import pl.softwaremill.asamal.example.filters.AuthorizationFilter;
import pl.softwaremill.asamal.example.model.json.ViewTicket;
import pl.softwaremill.asamal.example.model.json.ViewUsers;
import pl.softwaremill.asamal.example.service.ticket.TicketService;
import pl.softwaremill.common.cdi.security.Secure;

import javax.inject.Inject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller("stats")
@Secure("#{login.admin}")
@Filters({ActiveFilter.class, AuthorizationFilter.class})
public class Stats extends ControllerBean {

    @Inject
    private TicketService ticketService;

    @Get
    public void dashboard() {
    }

    @Get(params = "/page")
    @Json
    public ViewUsers loadAttendants(@PathParameter("page") Integer pageNumber) {
        return ticketService.getAllSoldTickets(pageNumber, 10);
    }

    @Get
    @ContentType("application/csv")
    public byte[] exportAttendees() {
        ViewUsers users = ticketService.getAllSoldTickets(-1, -1);

        System.out.println("users = " + users);

        StringWriter sw = new StringWriter();
        CsvListWriter writer = new CsvListWriter(sw, CsvPreference.EXCEL_PREFERENCE);

        List<String> header = new ArrayList<String>(Arrays.asList("First Name", "Last Name", "Company", "Category"));
        header.addAll(users.getTicketOptionLabels());

        try {
            writer.writeHeader(header.toArray(new String[header.size()]));

            for (ViewTicket viewTicket : users.getTickets()) {
                List<String> elements = new ArrayList<String>(Arrays.asList(viewTicket.getFirstName(),
                        viewTicket.getLastName(), viewTicket.getCompany(), viewTicket.getCategory()));
                elements.addAll(viewTicket.getOptionValues());

                writer.write(elements);
            }

            writer.close();

            return sw.toString().getBytes("UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
