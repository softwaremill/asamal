package pl.softwaremill.asamal.example.service.admin;

import org.apache.commons.io.IOUtils;
import pl.softwaremill.asamal.controller.DownloadDescription;
import pl.softwaremill.asamal.example.logic.conf.ConfigurationBean;
import pl.softwaremill.asamal.example.model.conf.Conf;
import pl.softwaremill.asamal.example.model.security.User;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.InvoiceStatus;
import pl.softwaremill.asamal.example.model.ticket.PaymentMethod;
import pl.softwaremill.asamal.example.service.email.EmailService;
import pl.softwaremill.asamal.request.http.GetHandler;
import pl.softwaremill.common.cdi.transaction.Transactional;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
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

    @Inject
    private Instance<HttpServletRequest> servletRequestInstance;

    @Inject
    private Instance<HttpServletResponse> servletResponseInstance;

    @Transactional
    public void closeAccountingMonth(Calendar monthStart) {
        Calendar monthEnd = (Calendar) monthStart.clone();
        monthEnd.add(Calendar.MONTH, 1);

        List<Invoice> invoices = entityManager.createQuery(
                "select i from Invoice i where i.datePaid >= :dateStart and i.datePaid < :dateEnd and i.editable = true" +
                        " and i.status = :status")
                .setParameter("dateStart", monthStart.getTime())
                .setParameter("dateEnd", monthEnd.getTime())
                .setParameter("status", InvoiceStatus.PAID)
                .getResultList();

        for (Invoice invoice : invoices) {
            invoice.setEditable(false);
            entityManager.merge(invoice);

            emailService.sendInvoiceEmail(invoice);
        }
    }

    public DownloadDescription generatePDFInvoicesForMonth(Calendar monthStart) {
        try {
            Calendar monthEnd = (Calendar) monthStart.clone();
            monthEnd.add(Calendar.MONTH, 1);

            List<Long> invoices = entityManager.createQuery(
                    "select i.id from Invoice i where i.datePaid >= :dateStart and i.datePaid < :dateEnd" +
                            " and i.status = :status and i.method != :method")
                    .setParameter("dateStart", monthStart.getTime())
                    .setParameter("dateEnd", monthEnd.getTime())
                    .setParameter("status", InvoiceStatus.PAID)
                    .setParameter("method", PaymentMethod.FREE)
                    .getResultList();

            File reports = File.createTempFile("reports", ".pdf");

            FileOutputStream fos = new FileOutputStream(reports);

            ZipOutputStream zipOutputStream = new ZipOutputStream(
                    new BufferedOutputStream(fos));

            // for each invoice generate PDF and put it in the zip file
            for (Long invoiceId : invoices) {
                System.out.println("Generating invoice: "+invoiceId);
                InputStream input = (InputStream) asamalGetHandler.handlePDFGet(servletRequestInstance.get(),
                        servletResponseInstance.get(), "invoice", "pdf",
                        invoiceId.toString(), null);

                ZipEntry ze = new ZipEntry(
                        configurationBean.getProperty(Conf.INVOICE_ID).replaceAll("/", "_").toLowerCase() + invoiceId +
                                ".pdf");
                zipOutputStream.putNextEntry(ze);

                IOUtils.copy(input, zipOutputStream);
            }

            zipOutputStream.close();

            return new DownloadDescription(new BufferedInputStream(new FileInputStream(reports)),
                    "invoices-"+monthStart.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())
                            +"-"+monthStart.get(Calendar.YEAR)+".zip");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public User makeUserAnAdmin(User user) {
        user.setAdmin(true);

        return entityManager.merge(user);
    }
}
