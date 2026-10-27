package uk.gov.companieshouse.transactions.web.exception;

public class UnverifiedException extends Exception {

    public UnverifiedException(String message) {
        super(message);
    }

    public UnverifiedException(String message, Throwable cause) {
        super(message, cause);
    }
}
