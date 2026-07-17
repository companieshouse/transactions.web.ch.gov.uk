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
import uk.gov.companieshouse.api.handler.privatetransaction.PrivateTransactionResourceHandler;
import uk.gov.companieshouse.api.handler.privatetransaction.request.PrivateTransactionPresenterPatch;
import uk.gov.companieshouse.api.handler.transaction.TransactionsResourceHandler;
import uk.gov.companieshouse.api.handler.transaction.request.TransactionsGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.transactions.web.api.ApiClientService;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presenter.IndividualUserDto;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterApiResponse;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionsServiceImplTests {

    @Mock
    private ApiClientService apiClientServiceMock;

    @Mock
    private ApiClient apiClientMock;

    @Mock
    private InternalApiClient internalApiClientMock;

    @Mock
    private PrivateTransactionResourceHandler privateTransactionHandlerMock;

    @Mock
    private PrivateTransactionPresenterPatch presenterPatchMock;

    @Mock
    private TransactionsResourceHandler transactionResourceHandlerMock;

    @Mock
    private TransactionsGet transactionGetMock;

    @Mock
    private ApiResponse<Transaction> apiResponse;

    @Mock
    private ApiResponse<Void> voidApiResponse;

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

    // -----------------------------------------------------------------------
    // patchPresenterData tests
    // -----------------------------------------------------------------------

    private void initPresenterPatch() throws ApiErrorResponseException, URIValidationException {
        when(apiClientServiceMock.getInternalApiClient()).thenReturn(internalApiClientMock);
        when(internalApiClientMock.privateTransaction()).thenReturn(privateTransactionHandlerMock);
        when(privateTransactionHandlerMock.presenterPatch(anyString(), any())).thenReturn(presenterPatchMock);
    }

    @Test
    @DisplayName("Patch presenter data - success returns 204")
    void patchPresenterDataSuccess() throws Exception {
        initPresenterPatch();
        when(presenterPatchMock.execute()).thenReturn(voidApiResponse);
        when(voidApiResponse.getStatusCode()).thenReturn(204);

        PresenterApiResponse presenterResponse = new PresenterApiResponse(
                new IndividualUserDto("UVID-001", "Test Person", "01/01/1970"),
                List.of("Statement one", "Statement two"));

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(
                () -> transactionService.patchPresenterData(TRANSACTION_ID, presenterResponse));
    }

    @Test
    @DisplayName("Patch presenter data - unexpected status throws ServiceException")
    void patchPresenterDataUnexpectedStatusThrowsServiceException() throws Exception {
        initPresenterPatch();
        when(presenterPatchMock.execute()).thenReturn(voidApiResponse);
        when(voidApiResponse.getStatusCode()).thenReturn(500);

        PresenterApiResponse presenterResponse = new PresenterApiResponse(
                new IndividualUserDto("UVID-002", "Error Person", "02/02/1980"),
                List.of("Statement"));

        assertThrows(ServiceException.class,
                () -> transactionService.patchPresenterData(TRANSACTION_ID, presenterResponse));
    }

    @Test
    @DisplayName("Patch presenter data - ApiErrorResponseException throws ServiceException")
    void patchPresenterDataApiErrorThrowsServiceException() throws Exception {
        initPresenterPatch();
        when(presenterPatchMock.execute()).thenThrow(ApiErrorResponseException.class);

        PresenterApiResponse presenterResponse = new PresenterApiResponse(
                null, List.of("Statement"));

        assertThrows(ServiceException.class,
                () -> transactionService.patchPresenterData(TRANSACTION_ID, presenterResponse));
    }

}
