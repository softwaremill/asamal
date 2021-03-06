package pl.softwaremill.asamal.example.model.json;

import pl.softwaremill.asamal.example.logic.invoice.InvoiceTotals;
import pl.softwaremill.asamal.example.logic.invoice.InvoiceTotalsCounter;
import pl.softwaremill.asamal.example.model.ticket.Invoice;
import pl.softwaremill.asamal.example.model.ticket.InvoiceStatus;
import pl.softwaremill.asamal.example.model.ticket.Ticket;
import pl.softwaremill.common.util.dependency.D;

import java.text.SimpleDateFormat;

public class ViewInvoice {

    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private String invoiceNo;

    private String email;

    private String name;

    private String company;

    private String amount;

    private String discount;

    private String invoiceId;

    private String tickets = "";

    private InvoiceStatus status;

    private String dateApproved;

    private String dateCreated;

    public ViewInvoice(Invoice invoice) {
        boolean prof = invoice.getStatus() == InvoiceStatus.UNPAID;
        boolean cancelled = invoice.getStatus() == InvoiceStatus.CANCELLED;

        invoiceNo = (prof ? "PROF/" : "") + (cancelled ? "CANCELLED/" : "") + invoice.getMethod().toString() + "/" +
                (prof || cancelled ? invoice.getId() : invoice.getInvoiceNumber());

        name = invoice.getName();

        company = invoice.getCompanyName();

        invoiceId = String.valueOf(invoice.getId());

        InvoiceTotals totals = D.inject(InvoiceTotalsCounter.class).countInvoice(invoice);
        amount = totals.getTotalAmount().toString() + " + " + totals.getTotalVat() + " = " +
                totals.getTotalGrossAmount().toString();

        discount = (invoice.getDiscount() == null ? "" :
                invoice.getDiscount().getDiscountCode() + "( " + invoice.getDiscount().getDiscountAmount() + "%)");

        for (Ticket ticket : invoice.getTickets()) {
            tickets += ticket.getFirstName() + " " + ticket.getLastName() +"\n";
        }

        status = invoice.getStatus();

        dateApproved = (status == InvoiceStatus.PAID ? sdf.format(invoice.getDatePaid()) : "");
        dateCreated = sdf.format(invoice.getDateCreated());

        email = invoice.getUser().getUsername();
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public String getName() {
        return name;
    }

    public String getCompany() {
        return company;
    }

    public String getAmount() {
        return amount;
    }

    public String getDiscount() {
        return discount;
    }

    public String getTickets() {
        return tickets;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public String getDateApproved() {
        return dateApproved;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "ViewInvoice{" +
                "invoiceNo='" + invoiceNo + '\'' +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                ", company='" + company + '\'' +
                ", amount='" + amount + '\'' +
                ", discount='" + discount + '\'' +
                ", invoiceId='" + invoiceId + '\'' +
                ", tickets='" + tickets + '\'' +
                ", status=" + status +
                ", dateApproved='" + dateApproved + '\'' +
                ", dateCreated='" + dateCreated + '\'' +
                '}';
    }
}
