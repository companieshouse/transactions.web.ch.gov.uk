package uk.gov.companieshouse.transactions.web.service.confirmation.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.exception.UnsupportedTransactionConfirmationException;
import uk.gov.companieshouse.transactions.web.model.confirmation.Confirmation;
import uk.gov.companieshouse.transactions.web.service.confirmation.ConfirmationService;
import uk.gov.companieshouse.transactions.web.transformer.confirmation.ConfirmationTransformer;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfirmationServiceImplTests {

    @Mock
    private ConfirmationTransformer confirmationTransformer;

    @InjectMocks
    private ConfirmationService confirmationService = new ConfirmationServiceImpl();

    @Test
    @DisplayName("Get Transaction Confirmation - Success Path")
    void getTransactionConfirmationSuccess() throws UnsupportedTransactionConfirmationException, ServiceException {

        Transaction transaction = new Transaction();

        when(confirmationTransformer.getTransactionConfirmation(transaction))
                .thenReturn(new Confirmation());

        assertNotNull(confirmationService.getTransactionConfirmation(transaction));
    }

    @Test
    @DisplayName("Get Transaction Confirmation - Transformer Throws Exception")
    void getTransactionConfirmationThrowsException()
            throws UnsupportedTransactionConfirmationException {

        Transaction transaction = new Transaction();

        doThrow(UnsupportedTransactionConfirmationException.class)
                .when(confirmationTransformer).getTransactionConfirmation(transaction);

        assertThrows(ServiceException.class,
                () -> confirmationService.getTransactionConfirmation(transaction));
    }

}
