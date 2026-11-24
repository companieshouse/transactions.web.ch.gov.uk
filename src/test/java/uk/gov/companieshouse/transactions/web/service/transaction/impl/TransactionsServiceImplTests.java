package uk.gov.companieshouse.transactions.web.service.transaction.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.transaction.TransactionsResourceHandler;
import uk.gov.companieshouse.api.handler.privatetransaction.PrivateTransactionResourceHandler;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionPatchWithProtectedData;
import uk.gov.companieshouse.api.handler.transaction.request.TransactionsGet;
import uk.gov.companieshouse.api.handler.transaction.request.TransactionsUpdate;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.api.model.transaction.TransactionWithProtectedData;
import uk.gov.companieshouse.transactions.web.api.ApiClientService;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
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

    @Mock
    private TransactionsUpdate transactionUpdateMock;

    @Mock
    private InternalApiClient internalApiClientMock;

    @Mock
    private PrivateTransactionResourceHandler privateTransactionResourceHandlerMock;

    @Mock
    private PrivateTransactionPatchWithProtectedData transactionPatchMock;

    @InjectMocks
    private TransactionsServiceImpl transactionService;

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

    private void initUpdate(Transaction transaction) {
        when(apiClientServiceMock.getApiClient()).thenReturn(apiClientMock);
        when(apiClientMock.transactions()).thenReturn(transactionResourceHandlerMock);
        when(transactionResourceHandlerMock.update(TRANSACTION_URI, transaction)).thenReturn(transactionUpdateMock);
    }

    private void initPatch(TransactionWithProtectedData transaction) {
        when(apiClientServiceMock.getInternalApiClient()).thenReturn(internalApiClientMock);
        when(internalApiClientMock.privateTransaction()).thenReturn(privateTransactionResourceHandlerMock);
        when(privateTransactionResourceHandlerMock
                .patchWithProtectedData("/private/transactions/" + TRANSACTION_ID, transaction))
                .thenReturn(transactionPatchMock);
    }

    @Test
    @DisplayName("Update transaction - success path")
    void updateTransactionSuccess() throws Exception {
        Transaction transaction = new Transaction();
        initUpdate(transaction);
        transactionService.updateTransaction(TRANSACTION_ID, transaction);
        verify(transactionUpdateMock).execute();
    }

    @Test
    @DisplayName("Update transaction - throws ApiErrorResponseException")
    void updateTransactionThrowsApiErrorResponseException() throws Exception {
        Transaction transaction = new Transaction();
        initUpdate(transaction);
        when(transactionUpdateMock.execute()).thenThrow(ApiErrorResponseException.class);
        assertThrows(ServiceException.class, () -> transactionService.updateTransaction(TRANSACTION_ID, transaction));
    }

    @Test
    @DisplayName("Update transaction - throws URIValidationException")
    void updateTransactionThrowsURIValidationException() throws Exception {
        Transaction transaction = new Transaction();
        initUpdate(transaction);
        when(transactionUpdateMock.execute()).thenThrow(URIValidationException.class);
        assertThrows(ServiceException.class, () -> transactionService.updateTransaction(TRANSACTION_ID, transaction));
    }

    @Test
    @DisplayName("Patch transaction - success path")
    void patchTransactionSuccess() throws Exception {
        TransactionWithProtectedData transaction = new TransactionWithProtectedData();
        initPatch(transaction);
        transactionService.patchTransaction(TRANSACTION_ID, transaction);
        verify(transactionPatchMock).execute();
    }

    @Test
    @DisplayName("Patch transaction - throws ApiErrorResponseException")
    void patchTransactionThrowsApiErrorResponseException() throws Exception {
        TransactionWithProtectedData transaction = new TransactionWithProtectedData();
        initPatch(transaction);
        when(transactionPatchMock.execute()).thenThrow(ApiErrorResponseException.class);
        assertThrows(ServiceException.class, () -> transactionService.patchTransaction(TRANSACTION_ID, transaction));
    }

    @Test
    @DisplayName("Patch transaction - throws URIValidationException")
    void patchTransactionThrowsURIValidationException() throws Exception {
        TransactionWithProtectedData transaction = new TransactionWithProtectedData();
        initPatch(transaction);
        when(transactionPatchMock.execute()).thenThrow(URIValidationException.class);
        assertThrows(ServiceException.class, () -> transactionService.patchTransaction(TRANSACTION_ID, transaction));
    }

}
