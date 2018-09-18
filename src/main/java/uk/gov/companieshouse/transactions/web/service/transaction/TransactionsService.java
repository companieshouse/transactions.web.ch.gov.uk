package uk.gov.companieshouse.transactions.web.service.transaction;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;

public interface TransactionsService {

    /**
     * Retrieve a CHS {@code Transaction} for a given transaction id
     * @param transactionId
     * @return A transaction record
     * @throws ServiceException
     */
    Transaction getTransaction(String transactionId) throws ServiceException;
}
