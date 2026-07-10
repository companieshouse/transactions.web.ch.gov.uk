package uk.gov.companieshouse.transactions.web.service.transaction;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterApiResponse;

public interface TransactionsService {

    /**
     * Retrieve a CHS {@code Transaction} for a given transaction id
     * @param transactionId
     * @return A transaction record
     * @throws ServiceException
     */
    Transaction getTransaction(String transactionId) throws ServiceException;

    /**
     * Determines whether a transaction's status is 'closed' or 'closed pending payment'
     * @param transaction
     * @return true if a transaction is closed or closed pending payment
     */
    boolean isTransactionClosedOrClosedPendingPayment(Transaction transaction);

    /**
     * Persists presenter data (obtained from the Presenter API) onto the transaction
     * by calling PATCH /transactions/{id}/presenter on the transactions API.
     *
     * @param transactionId    the ID of the transaction to update
     * @param presenterResponse the data returned by the Presenter API
     * @throws ServiceException if the PATCH call fails
     */
    void patchPresenterData(String transactionId, PresenterApiResponse presenterResponse) throws ServiceException;
}
