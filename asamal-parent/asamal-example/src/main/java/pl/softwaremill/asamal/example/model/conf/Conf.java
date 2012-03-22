package pl.softwaremill.asamal.example.model.conf;

public enum Conf {
    INVOICE_NAME(""),
    INVOICE_COMPANY("Foo (Pty) Ltd."),
    INVOICE_VAT("FB 123-456-789"),
    INVOICE_ADDRESS("1 Foo St."),
    INVOICE_POSTAL_CODE("12345"),
    INVOICE_CITY("Foovaville"),
    INVOICE_COUNTRY("Barstan"),
    INVOICE_VAT_RATE("23"),

    TICKETS_MAX("200")
    ;

    final public String defaultValue;

    private Conf(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
