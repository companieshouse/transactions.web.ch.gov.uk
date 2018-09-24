package uk.gov.companieshouse.transactions.web.service.confirmation;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.confirmation.Confirmation;

public interface ConfirmationService {

    /**
     * Returns a {@link Confirmation} for a provided transaction
     *
     * @param transaction The transaction for which to provide a confirmation
     * @return A {@link Confirmation} model
     * @throws ServiceException if the transaction cannot be mapped to a confirmation
     */
    Confirmation getTransactionConfirmation(Transaction transaction) throws ServiceException;
}
