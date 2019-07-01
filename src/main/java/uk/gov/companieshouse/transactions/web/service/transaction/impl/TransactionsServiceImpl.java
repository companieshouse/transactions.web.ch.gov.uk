package uk.gov.companieshouse.transactions.web.service.transaction.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.transactions.web.api.ApiClientService;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.service.transaction.TransactionsService;

@Service
public class TransactionsServiceImpl implements TransactionsService {

    @Autowired
    ApiClientService apiClientService;

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
    public boolean isTransactionClosedOrClosedPendingPayment(Transaction transaction) {

        return transaction.getStatus() == TransactionStatus.CLOSED || transaction.getStatus() == TransactionStatus.CLOSED_PENDING_PAYMENT ;
    }
}
