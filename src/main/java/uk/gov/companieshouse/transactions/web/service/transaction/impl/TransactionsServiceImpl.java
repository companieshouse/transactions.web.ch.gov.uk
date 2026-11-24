package uk.gov.companieshouse.transactions.web.service.transaction.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.api.model.transaction.TransactionWithProtectedData;
import uk.gov.companieshouse.transactions.web.api.ApiClientService;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.service.transaction.TransactionsService;

@Service
public class TransactionsServiceImpl implements TransactionsService {

    private static final String TRANSACTIONS_URI = "/transactions/";
    private static final String PRIVATE_TRANSACTIONS_URI = "/private/transactions/";

    final ApiClientService apiClientService;

    public TransactionsServiceImpl(ApiClientService apiClientService) {
        this.apiClientService = apiClientService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Transaction getTransaction(String transactionId) throws ServiceException {

        ApiClient apiClient = apiClientService.getApiClient();

        try {
            return apiClient.transactions().get("/transactions/" + transactionId).execute().getData();

        } catch (ApiErrorResponseException e) {
            throw new ServiceException("Error retrieving transaction", e);
        } catch (URIValidationException e) {
            throw new ServiceException("Invalid URI for transaction resource", e);
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTransaction(String transactionId, Transaction transaction) throws ServiceException {

        try {
            apiClientService.getApiClient().transactions()
                    .update(TRANSACTIONS_URI + transactionId, transaction).execute();

        } catch (ApiErrorResponseException e) {
            throw new ServiceException("Error updating transaction", e);
        } catch (URIValidationException e) {
            throw new ServiceException("Invalid URI for transaction resource", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void patchTransaction(String transactionId, TransactionWithProtectedData transaction) throws ServiceException {

        try {
            apiClientService.getInternalApiClient().privateTransaction()
                    .patchWithProtectedData(PRIVATE_TRANSACTIONS_URI + transactionId, transaction).execute();

        } catch (ApiErrorResponseException e) {
            throw new ServiceException("Error patching transaction", e);
        } catch (URIValidationException e) {
            throw new ServiceException("Invalid URI for transaction resource", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTransactionClosedOrClosedPendingPayment(Transaction transaction) {

        return transaction.getStatus() == TransactionStatus.CLOSED || transaction.getStatus() == TransactionStatus.CLOSED_PENDING_PAYMENT ;
    }
}
