package pl.softwaremill.asamal.example.servlet;

import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.InvoiceStatus;
import pl.softwaremill.asamal.example.service.email.EmailService;
import pl.softwaremill.common.cdi.transaction.Transactional;

import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Calendar;
import java.util.List;

@Singleton
public class NotPaidTicketsNotifier {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private EmailService emailService;

    @Schedule(second="20", minute="10",hour="1", persistent=false)
    @Transactional
    public void checkUnpaidInvoices(){
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, -7);

        List<Invoice> invoices = entityManager.createQuery(
                "select i from Invoice i where i.status = :status and " +
                "i.dateCreated < :date and (i.notified is null or i.notified = false)")
                .setParameter("status", InvoiceStatus.UNPAID)
                .setParameter("date", c.getTime())
                .getResultList();

        for (Invoice invoice : invoices) {
            emailService.sendLatePaymentNotification(invoice);
            invoice.setNotified(true);
        }
    }
}
