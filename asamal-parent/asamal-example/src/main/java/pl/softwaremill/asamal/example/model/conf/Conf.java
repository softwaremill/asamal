package pl.softwaremill.asamal.example.model.conf;

public enum Conf {

    ACTIVE("false", TYPE.BOOLEAN),

    CONFERENCE_NAME("Conference"),

    CONFERENCE_LOGO(""),

    INVOICE_NAME(""),
    INVOICE_COMPANY("Foo (Pty) Ltd."),
    INVOICE_VAT("FB 123-456-789"),
    INVOICE_ADDRESS("1 Foo St."),
    INVOICE_POSTAL_CODE("12345"),
    INVOICE_CITY("Foovaville"),
    INVOICE_COUNTRY("Barstan"),
    INVOICE_VAT_RATE("23"),
    INVOICE_ID("Conference/2012/"),
    INVOICE_CURRENCY("PLN"),
    INVOICE_IBAN("PL 123456789101112131415"),
    INVOICE_BANK_NAME("Bank Of The Foo"),
    INVOICE_BANK_CODE("BXFOO"),

    TICKETS_MAX("200"),

    DISCOUNT_LATE_MAX_TIME("60"),

    PAYPAL_SANDBOX("true", TYPE.BOOLEAN),
    PAYPAL_EMAIL("email_paypal@test.com"),

    TICKETS_THANKYOU_MSG("Thank you for buying our tickets", TYPE.TEXT_AREA),

    TICKETS_THANKYOU_EMAIL_SUBJECT("Thank you for buying tickets!"),

    TICKETS_THANKYOU_EMAIL("Dear $name,\n" +
                                   "\n" +
                                   "Thank you for purchasing the tickets for Conference !\n" +
                                   "\n" +
                                   "Below is a list of your tickets:\n" +
                                   "#foreach($ticket in $tickets)\n" +
                                   "$ticket.firstName $ticket.lastName\n" +
                                   "#end\n" +
                                   "\n" +
                                   "--\n" +
                                   "The Conference Team", TYPE.TEXT_AREA),

    NOTIFY_EMAIL("foo@bar.com"),

    TICKETS_TRANSFER_RECEIVED_SUBJECT("Your payment for Conference has been received!"),

    TICKETS_TRANSFER_RECEIVED_EMAIL("Dear $name,\n" +
                                   "\n" +
                                   "We got your payment and everything is in order.\n" +
                                   "\n" +
                                   "You can check your invoice on $invoice_link\n" +
                                   "\n" +
                                   "That means your tickets are confirmed, see you there!" +
                                   "\n" +
                                   "--\n" +
                                   "The Conference Team", TYPE.TEXT_AREA),

    PASSWORD_FORGOT_SUBJECT("Your new password"),

    PASSWORD_FORGOT_EMAIL("Dear User,\n" +
            "\n" +
            "Your password has been reset. Your new password is $new_password .\n" +
            "\n" +
            "System will ask you to reset your password once you login.\n" +
            "\n" +
            "--\n" +
            "The Conference Team", TYPE.TEXT_AREA),

    INVOICE_EMAIL_SUBJECT("Invoice for Conference"),

    INVOICE_EMAIL("Dear $name,\n" +
            "\n" +
            "Your final invoice for the Conference is ready! Please follow the link below to download it:\n" +
            "\n" +
            "$invoice_link\n" +
            "\n" +
            "--\n" +
            "The Conference Team", TYPE.TEXT_AREA),

    SYSTEM_URL("fill-me-in-in-settings"),

    TICKETS_FINISHING_SUBJECT("Ticket category $category is finishing"),

    TICKETS_FINISHING_EMAIL("Dear Admins,\n" +
            "\n" +
            "Tickets in the category $category are finishing. Currently there are only $tickets tickets!" +
            "\n" +
            "--\n" +
            "The Conference Team", TYPE.TEXT_AREA),

    NUMBER_OF_TICKETS_FINISHING("3"),

    LATE_PAYMENT_SUBJECT("We have not seen your payment yet!"),

    LATE_PAYMENT_EMAIL("Dear $name,\n" +
            "\n" +
            "Over a week ago you have booked tickets for the Conference, but unfortunately we have not seen" +
            " your payment yet.\n" +
            "\n" +
            "Below is a list of the tickets you have ordered:\n" +
            "#foreach($ticket in $tickets)\n" +
            "* $ticket.firstName $ticket.lastName\n" +
            "#end\n" +
            "\n" +
            "If possible please go to the conference website and make your payments.\n" +
            "\n" +
            "If we do not get the payment within next 5 days we will have to cancel Your order.\n" +
            "\n" +
            "Do not hesitate to contact us if something is not in order,\n" +
            "\n" +
            "--\n" +
            "The Conference Team"),

    ;

    enum TYPE {
        STRING,
        BOOLEAN,
        TEXT_AREA
    }

    final public String defaultValue;
    final TYPE type;

    private Conf(String defaultValue) {
        this(defaultValue, TYPE.STRING);
    }

    private Conf(String defaultValue, TYPE type) {
        this.defaultValue = defaultValue;
        this.type = type;
    }

    public boolean isString() {
        return type == TYPE.STRING;
    }

    public boolean isBool() {
        return type == TYPE.BOOLEAN;
    }

    public boolean isTextArea() {
        return type == TYPE.TEXT_AREA;
    }
}
