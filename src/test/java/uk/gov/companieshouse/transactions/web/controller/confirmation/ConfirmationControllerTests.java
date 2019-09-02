package uk.gov.companieshouse.transactions.web.controller.confirmation;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.confirmation.Confirmation;
import uk.gov.companieshouse.transactions.web.service.confirmation.ConfirmationService;
import uk.gov.companieshouse.transactions.web.service.transaction.TransactionsService;
import uk.gov.companieshouse.transactions.web.session.SessionService;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfirmationControllerTests {

    private MockMvc mockMvc;

    @Mock
    private TransactionsService transactionsService;

    @Mock
    private ConfirmationService confirmationService;

    @Mock
    private SessionService sessionService;

    @Mock
    private Map<String, Object> sessionData;

    @InjectMocks
    private ConfirmationController controller;

    private static final String TRANSACTION_ID = "transactionId";

    private static final String CONFIRMATION_PATH = "/transaction/" + TRANSACTION_ID +
                                                        "/confirmation";

    private static final String STATE = "state";

    private static final String MISMATCHED_STATE = "mismatchedState";

    private static final String CONFIRMATION_VIEW = "transactionConfirmation";

    private static final String CONFIRMATION_MODEL_ATTR = "confirmation";

    private static final String TEMPLATE_NAME_ATTR = "templateName";

    private static final String ERROR_VIEW = "error";

    private static final String PAYMENT_STATE = "payment_state";

    @BeforeEach
    private void setup() {

        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("Get confirmation view - success path with state parameter")
    void getConfirmationSuccessWithStateParam() throws Exception {

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);

        when(sessionData.get(PAYMENT_STATE)).thenReturn(STATE);

        Transaction closedTransaction = new Transaction();

        when(transactionsService.getTransaction(TRANSACTION_ID)).thenReturn(closedTransaction);

        when(transactionsService.isTransactionClosedOrClosedPendingPayment(closedTransaction)).thenReturn(true);

        when(confirmationService.getTransactionConfirmation(closedTransaction))
                .thenReturn(new Confirmation());

        this.mockMvc.perform(get(CONFIRMATION_PATH)
        .param("state", STATE))
                .andExpect(view().name(CONFIRMATION_VIEW))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(CONFIRMATION_MODEL_ATTR))
                .andExpect(model().attributeExists(TEMPLATE_NAME_ATTR));

        verify(sessionData).remove(PAYMENT_STATE);
    }

    @Test
    @DisplayName("Get confirmation view - mismatched state parameter")
    void getConfirmationWithMismatchedStateParam() throws Exception {

        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);

        when(sessionData.get(PAYMENT_STATE)).thenReturn(MISMATCHED_STATE);

        this.mockMvc.perform(get(CONFIRMATION_PATH)
        .param("state", "mismatchedState"))
                .andExpect(view().name(ERROR_VIEW))
                .andExpect(status().isOk());

        verify(sessionData).remove(PAYMENT_STATE);
    }

    @Test
    @DisplayName("Get confirmation view - success path without state parameter")
    void getConfirmationSuccessWithoutStateParam() throws Exception {

        Transaction closedTransaction = new Transaction();

        when(transactionsService.getTransaction(TRANSACTION_ID)).thenReturn(closedTransaction);

        when(transactionsService.isTransactionClosedOrClosedPendingPayment(closedTransaction)).thenReturn(true);

        when(confirmationService.getTransactionConfirmation(closedTransaction))
                .thenReturn(new Confirmation());

        this.mockMvc.perform(get(CONFIRMATION_PATH))
                .andExpect(view().name(CONFIRMATION_VIEW))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(CONFIRMATION_MODEL_ATTR))
                .andExpect(model().attributeExists(TEMPLATE_NAME_ATTR));

        verify(sessionService, never()).getSessionDataFromContext();
    }

    @Test
    @DisplayName("Get confirmation view - Payment Cancelled Path")
    void getPaymentCancelled() throws Exception {

        Transaction closedTransaction = new Transaction();

        when(transactionsService.getTransaction(TRANSACTION_ID)).thenReturn(closedTransaction);
        String journeyUrl = UrlBasedViewResolver.REDIRECT_URL_PREFIX+closedTransaction.getResumeJourneyUri();

        this.mockMvc.perform(get(CONFIRMATION_PATH)
        .param("status", "cancelled"))
            .andExpect(status().is3xxRedirection())
            .andExpect(view().name(journeyUrl));
    }

    @Test
    @DisplayName("Get confirmation view - Payment Not Cancelled Path")
    void getPaymentNotCancelled() throws Exception {

        Transaction closedTransaction = new Transaction();

        when(transactionsService.getTransaction(TRANSACTION_ID)).thenReturn(closedTransaction);

        when(transactionsService.isTransactionClosedOrClosedPendingPayment(closedTransaction)).thenReturn(true);

        when(confirmationService.getTransactionConfirmation(closedTransaction))
            .thenReturn(new Confirmation());

        this.mockMvc.perform(get(CONFIRMATION_PATH)
            .param("status", "paid"))
            .andExpect(view().name(CONFIRMATION_VIEW))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists(CONFIRMATION_MODEL_ATTR))
            .andExpect(model().attributeExists(TEMPLATE_NAME_ATTR));
    }

    @Test
    @DisplayName("Get confirmation view - transaction open")
    void getConfirmationForOpenTransaction() throws Exception {

        Transaction openTransaction = new Transaction();

        when(transactionsService.getTransaction(TRANSACTION_ID)).thenReturn(openTransaction);

        when(transactionsService.isTransactionClosedOrClosedPendingPayment(openTransaction)).thenReturn(false);

        this.mockMvc.perform(get(CONFIRMATION_PATH))
                .andExpect(view().name(ERROR_VIEW))
                .andExpect(status().isOk());

        verify(confirmationService, never()).getTransactionConfirmation(any());
    }

    @Test
    @DisplayName("Get confirmation view - ServiceException thrown on getTransaction")
    void getConfirmationServiceExceptionThrowOnGetTransaction() throws Exception {

        when(transactionsService.getTransaction(TRANSACTION_ID)).thenThrow(ServiceException.class);

        this.mockMvc.perform(get(CONFIRMATION_PATH))
                .andExpect(view().name(ERROR_VIEW))
                .andExpect(status().isOk());

        assertThrows(ServiceException.class, () -> transactionsService.getTransaction(TRANSACTION_ID));

        verify(confirmationService, never()).getTransactionConfirmation(any());
    }

    @Test
    @DisplayName("Get confirmation view - ServiceException thrown on getTransactionConfirmation")
    void getConfirmationServiceExceptionThrowOnGetTransactionConfirmation() throws Exception {

        Transaction closedTransaction = new Transaction();

        when(transactionsService.getTransaction(TRANSACTION_ID)).thenReturn(closedTransaction);

        when(transactionsService.isTransactionClosedOrClosedPendingPayment(closedTransaction)).thenReturn(true);

        when(confirmationService.getTransactionConfirmation(closedTransaction)).thenThrow(ServiceException.class);

        this.mockMvc.perform(get(CONFIRMATION_PATH))
                .andExpect(view().name(ERROR_VIEW))
                .andExpect(status().isOk());

        assertThrows(ServiceException.class, () ->
                confirmationService.getTransactionConfirmation(closedTransaction));
    }

}
