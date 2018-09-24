package uk.gov.companieshouse.transactions.web.service.transaction.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.transaction.TransactionResourceHandler;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.transactions.web.api.ApiClientService;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.service.transaction.TransactionsService;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionsServiceImplTests {

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private ApiClient apiClient;

    @Mock
    private TransactionResourceHandler transactionResourceHandler;

    @InjectMocks
    private TransactionsService transactionService = new TransactionsServiceImpl();

    private static final String TRANSACTION_ID = "111-222-333";

    @Test
    @DisplayName("Get transaction - success path")
    void getTransactionSuccess() throws ApiErrorResponseException, ServiceException {

        when(apiClientService.getApiClient()).thenReturn(apiClient);
        when(apiClient.transaction(TRANSACTION_ID)).thenReturn(transactionResourceHandler);

        when(transactionResourceHandler.get()).thenReturn(new Transaction());

        assertNotNull(transactionService.getTransaction(TRANSACTION_ID));
    }

    @Test
    @DisplayName("Get transaction - throws ApiErrorResponseException")
    void getTransactionApiResponseExceptionThrown() throws ApiErrorResponseException {

        when(apiClientService.getApiClient()).thenReturn(apiClient);
        when(apiClient.transaction(TRANSACTION_ID)).thenReturn(transactionResourceHandler);

        when(transactionResourceHandler.get()).thenThrow(ApiErrorResponseException.class);

        assertThrows(ApiErrorResponseException.class,
                () -> transactionResourceHandler.get());

        assertThrows(ServiceException.class,
                () -> transactionService.getTransaction(TRANSACTION_ID));
    }

    @Test
    @DisplayName("Is transaction closed - return true")
    void isTransactionClosedTrue() {

        Transaction closedTransaction = new Transaction();
        closedTransaction.setStatus(TransactionStatus.CLOSED);

        assertTrue(transactionService.isTransactionClosed(closedTransaction));
    }

    @Test
    @DisplayName("Is transaction closed - return false")
    void isTransactionClosedFalse() {

        Transaction openTransaction = new Transaction();
        openTransaction.setStatus(TransactionStatus.OPEN);

        assertFalse(transactionService.isTransactionClosed(openTransaction));
    }
}
