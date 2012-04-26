package pl.softwaremill.asamal.example.controller;

import pl.softwaremill.asamal.controller.ControllerBean;
import pl.softwaremill.asamal.controller.annotation.Controller;
import pl.softwaremill.asamal.controller.annotation.Get;
import pl.softwaremill.asamal.controller.annotation.Json;
import pl.softwaremill.asamal.controller.annotation.PathParameter;
import pl.softwaremill.asamal.example.model.json.ViewUsers;
import pl.softwaremill.asamal.example.service.ticket.TicketService;
import pl.softwaremill.common.cdi.security.Secure;

import javax.inject.Inject;

@Controller("stats")
@Secure("#{login.admin}")
public class Stats extends ControllerBean {

    @Inject
    private TicketService ticketService;

    @Get
    public void dashboard() {}

    @Json(params = "/page")
    public ViewUsers loadAttendants(@PathParameter("page") Integer pageNumber) {
        return ticketService.getAllSoldTickets(pageNumber, 10);
    }
}
