package uk.gov.companieshouse.transactions.web.service.transaction.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.transaction.TransactionsResourceHandler;
import uk.gov.companieshouse.api.handler.transaction.request.TransactionsGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.transactions.web.api.ApiClientService;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.service.transaction.TransactionsService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TransactionsServiceImplTests {

    @Mock
    private ApiClientService apiClientServiceMock;

    @Mock
    private ApiClient apiClientMock;

    @Mock
    private TransactionsResourceHandler transactionResourceHandlerMock;

    @Mock
    private TransactionsGet transactionGetMock;

    @Mock
    private ApiResponse<Transaction> apiResponse;

    @InjectMocks
    private TransactionsService transactionService = new TransactionsServiceImpl();

    private static final String TRANSACTION_ID = "111-222-333";
    private static final String TRANSACTION_URI = "/transactions/"+TRANSACTION_ID;


    private void init() {
        when(apiClientServiceMock.getApiClient()).thenReturn(apiClientMock);
        when(apiClientMock.transactions()).thenReturn(transactionResourceHandlerMock);
        when(transactionResourceHandlerMock.get(TRANSACTION_URI)).thenReturn(transactionGetMock);
    }

    private Transaction getTransactionWithStatus(TransactionStatus status){
        Transaction transaction = new Transaction();
        transaction.setStatus(status);
        return transaction;
    }

    @Test
    @DisplayName("Get transaction - success path")
    void getTransactionSuccess() throws Exception{
        init();
        when(transactionGetMock.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(new Transaction());
        assertNotNull(transactionService.getTransaction(TRANSACTION_ID));

    }

    @Test
    @DisplayName("Get transaction - throws ApiErrorResponseException")
    void getTransactionThrowsApiErrorResponseException() throws Exception {
        init();
        when(transactionGetMock.execute()).thenThrow(ApiErrorResponseException.class);
        assertThrows(ServiceException.class,
                () -> transactionService.getTransaction(TRANSACTION_ID));
    }

    @Test
    @DisplayName("Get transaction - throws URIValidationException")
    void getTransactionThrowsURIValidationException() throws Exception {
        init();
        when(transactionGetMock.execute()).thenThrow(URIValidationException.class);
        assertThrows(ServiceException.class,
                () -> transactionService.getTransaction(TRANSACTION_ID));
    }

    @Test
    @DisplayName("Is transaction closed - return true")
    void isTransactionClosedTrue() {
        assertTrue(transactionService.isTransactionClosedOrClosedPendingPayment(getTransactionWithStatus(TransactionStatus.CLOSED)));
    }

    @Test
    @DisplayName("Is transaction closed or closed pending payment - return false")
    void isTransactionClosedOrClosedPaymentPendingFalse() {
        assertFalse(transactionService.isTransactionClosedOrClosedPendingPayment(getTransactionWithStatus(TransactionStatus.OPEN)));
    }

    @Test
    @DisplayName("Is transaction closed pending payment - return true")
    void isTransactionClosedPendingPaymentTrue() {
        assertTrue(transactionService.isTransactionClosedOrClosedPendingPayment(getTransactionWithStatus(TransactionStatus.CLOSED_PENDING_PAYMENT)));
    }

}
