package uk.gov.companieshouse.transactions.web.exception;

public class AcspNotLinkedException extends Exception {

    public AcspNotLinkedException(String message) {
        super(message);
    }

    public AcspNotLinkedException(String message, Throwable cause) {
        super(message, cause);
    }
}
