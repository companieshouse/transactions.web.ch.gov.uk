package uk.gov.companieshouse.transactions.web.service.transaction.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
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
            return apiClient.transaction(transactionId).get();
        } catch (ApiErrorResponseException e) {
            throw new ServiceException("Error retrieving transaction", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTransactionClosed(Transaction transaction) {

        return transaction.getStatus() == TransactionStatus.CLOSED;
    }
}
