package uk.gov.companieshouse.transactions.web.service.transaction;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionWithProtectedData;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;

public interface TransactionsService {

    /**
     * Retrieve a CHS {@code Transaction} for a given transaction id
     * @param transactionId
     * @return A transaction record
     * @throws ServiceException
     */
    Transaction getTransaction(String transactionId) throws ServiceException;

    /**
     * Updates (PUT) a CHS {@code Transaction}
     * @param transactionId the id of the transaction to update
     * @param transaction the transaction to update
     * @throws ServiceException
     */
    void updateTransaction(String transactionId, Transaction transaction) throws ServiceException;

    /**
     * Patches (PATCH) a CHS {@code TransactionWithProtectedData} via the private transactions API
     * @param transactionId the id of the transaction to patch
     * @param transaction the transaction data to patch
     * @throws ServiceException
     */
    void patchTransaction(String transactionId, TransactionWithProtectedData transaction) throws ServiceException;

    /**
     * Determines whether a transaction's status is 'closed' or 'closed pending payment'
     * @param transaction
     * @return true if a transaction is closed or closed pending payment
     */
    boolean isTransactionClosedOrClosedPendingPayment(Transaction transaction);
}
