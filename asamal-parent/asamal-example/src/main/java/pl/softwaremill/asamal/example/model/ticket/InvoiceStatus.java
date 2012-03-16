package pl.softwaremill.asamal.example.model.ticket;

/**
 * Marks differents statuses of an invoice
 */
public enum InvoiceStatus {
    PAID,

    UNPAID,

    WAIT_TRANSFER,

    WAIT_PAYPAL,

    CANCELLED
}
