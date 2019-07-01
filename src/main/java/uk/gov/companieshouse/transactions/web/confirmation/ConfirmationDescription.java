package uk.gov.companieshouse.transactions.web.confirmation;

import java.util.Arrays;
import uk.gov.companieshouse.transactions.web.exception.UnsupportedTransactionConfirmationException;

/**
 * Provides a mapping between a transaction description and its confirmation description
 */
public enum ConfirmationDescription {

    FULL_ACCOUNTS("Small Full Accounts", "Full accounts"),
    CIC_REPORT_AND_SMALL_FULL_ACCOUNTS("CIC Report and Small Full Accounts", "CIC report and accounts");

    private String transactionDescription;
    private String confirmationDescription;

    ConfirmationDescription(String transactionDescription, String confirmationDescription) {
        this.transactionDescription = transactionDescription;
        this.confirmationDescription = confirmationDescription;
    }

    public String getTransactionDescription() {
        return transactionDescription;
    }

    public String getConfirmationDescription() {
        return confirmationDescription;
    }

    /**
     * Retrieve a confirmation description for a given transaction description
     * @param transactionDescription The description field on the transaction
     * @return a confirmation description
     */
    public static String getConfirmationDescription(String transactionDescription)
                                    throws UnsupportedTransactionConfirmationException {

        return Arrays.stream(ConfirmationDescription.values())
                .filter(e -> e.getTransactionDescription().equalsIgnoreCase(transactionDescription))
                .findFirst()
                .orElseThrow(() ->
                        new UnsupportedTransactionConfirmationException("No confirmation description "
                                + "mapping for transaction description: " + transactionDescription))
                .getConfirmationDescription();
    }
}
