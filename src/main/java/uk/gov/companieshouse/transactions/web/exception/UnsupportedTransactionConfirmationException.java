package uk.gov.companieshouse.transactions.web.exception;

/**
 * The class {@code UnsupportedTransactionConfirmationException} is a form of {@code Exception}
 * which is thrown if a transaction cannot be mapped to a {@code Confirmation} model
 */
public class UnsupportedTransactionConfirmationException extends Exception {

    /**
     * Constructs a new {@code UnsupportedTransactionConfirmationException} with a custom message
     * @param message The custom message
     */
    public UnsupportedTransactionConfirmationException(String message) {
        super(message);
    }

}
