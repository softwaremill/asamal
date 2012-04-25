package pl.softwaremill.asamal.example.service.admin;

import org.apache.commons.io.IOUtils;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.service.email.EmailService;
import pl.softwaremill.asamal.httphandler.GetHandler;
import pl.softwaremill.common.cdi.transaction.Transactional;
import pl.softwaremill.common.util.dependency.D;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AdminService {

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private EmailService emailService;

    @Inject
    private GetHandler asamalGetHandler;

    @Inject
    private ConfigurationBean configurationBean;

    @Transactional
    public void closeAccountingMonth(Calendar monthStart) {
        Calendar monthEnd = (Calendar) monthStart.clone();
        monthEnd.add(Calendar.MONTH, 1);

        List<Invoice> invoices = entityManager.createQuery(
                "select i from Invoice i where i.datePaid >= :dateStart and i.datePaid < :dateEnd and i.editable = true")
                .setParameter("dateStart", monthStart.getTime())
                .setParameter("dateEnd", monthEnd.getTime())
                .getResultList();

        for (Invoice invoice : invoices) {
            invoice.setEditable(false);
            entityManager.merge(invoice);

            emailService.sendInvoiceEmail(invoice);
        }
    }

    public InputStream generatePDFInvoicesForMonth(Calendar monthStart) {
        try {
            Calendar monthEnd = (Calendar) monthStart.clone();
            monthEnd.add(Calendar.MONTH, 1);

            List<Long> invoices = entityManager.createQuery(
                    "select i.id from Invoice i where i.datePaid >= :dateStart and i.datePaid < :dateEnd")
                    .setParameter("dateStart", monthStart.getTime())
                    .setParameter("dateEnd", monthEnd.getTime())
                    .getResultList();

            PipedInputStream inputStream = new PipedInputStream();

            ZipOutputStream zipOutputStream = new ZipOutputStream(new PipedOutputStream(inputStream));

            HttpServletRequest request = D.inject(HttpServletRequest.class);
            HttpServletResponse response = D.inject(HttpServletResponse.class);

            // for each invoice generate PDF and put it in the zip file
            for (Long invoiceId : invoices) {
                InputStream input = (InputStream) asamalGetHandler.handlePDFGet(request, response, "invoice", "pdf",
                        invoiceId.toString());

                ZipEntry ze = new ZipEntry(
                        configurationBean.getProperty(Conf.INVOICE_ID).replaceAll("/", "_").toLowerCase() + invoiceId);
                zipOutputStream.putNextEntry(ze);

                IOUtils.copy(input, zipOutputStream);
            }

            zipOutputStream.close();

            return inputStream;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
