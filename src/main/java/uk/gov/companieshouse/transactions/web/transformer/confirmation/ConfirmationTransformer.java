package uk.gov.companieshouse.transactions.web.transformer.confirmation;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.transactions.web.exception.UnsupportedTransactionConfirmationException;
import uk.gov.companieshouse.transactions.web.model.confirmation.Confirmation;

public interface ConfirmationTransformer {

    /**
     * Get the {@link Confirmation} model for a provided transaction
     * @param transaction The transaction for which to retrieve a {@link Confirmation}
     * @return A transaction confirmation
     * @throws UnsupportedTransactionConfirmationException If the transaction cannot be mapped to
     *         a confirmation
     */
    Confirmation getTransactionConfirmation(Transaction transaction)
            throws UnsupportedTransactionConfirmationException;
}
