package uk.gov.companieshouse.transactions.web.transformer.confirmation.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.transactions.web.exception.UnsupportedTransactionConfirmationException;
import uk.gov.companieshouse.transactions.web.model.confirmation.Confirmation;
import uk.gov.companieshouse.transactions.web.transformer.confirmation.ConfirmationTransformer;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfirmationTransformerImplTests {

    private static final String COMPANY_NAME = "companyName";

    private static final String COMPANY_NUMBER = "companyNumber";

    private static final String VALID_TRANSACTION_DESCRIPTION = "Small Full Accounts";

    private static final String EXPECTED_CONFIRMATION_DESCRIPTION = "Full accounts";

    private static final String INVALID_TRANSACTION_DESCRIPTION = "invalidTransactionDescription";

    private static final String CLOSED_AT_DATE_STRING = "2018-07-01T12:34:00.000Z";

    private static final String EXPECTED_CONFIRMATION_CLOSED_DATE = "1 July 2018";

    private static final String EXPECTED_CONFIRMATION_CLOSED_TIME = "12:34 PM";

    private static final String TRANSACTION_ID = "transactionId";

    private static final String CLOSED_BY = "closed@by.email";

    private static final String EMAIL_KEY = "email";

    private ConfirmationTransformer confirmationTransformer;

    @BeforeEach
    void setUp() {
        confirmationTransformer = new ConfirmationTransformerImpl();
    }

    @Test
    @DisplayName("Get Transaction Confirmation - Valid Transaction")
    void getTransactionConfirmationSuccess() throws UnsupportedTransactionConfirmationException {

        Confirmation confirmation =
                confirmationTransformer.getTransactionConfirmation(constructValidTransaction());

        assertEquals(COMPANY_NAME, confirmation.getCompanyName());
        assertEquals(COMPANY_NUMBER, confirmation.getCompanyNumber());
        assertEquals(EXPECTED_CONFIRMATION_DESCRIPTION, confirmation.getConfirmationDescription());
        assertEquals(EXPECTED_CONFIRMATION_CLOSED_DATE, confirmation.getClosedAtDate());
        assertEquals(EXPECTED_CONFIRMATION_CLOSED_TIME, confirmation.getClosedAtTime());
        assertEquals(TRANSACTION_ID, confirmation.getTransactionId());
        assertEquals(CLOSED_BY, confirmation.getClosedBy());
    }

    @Test
    @DisplayName("Get Transaction Confirmation - Invalid Transaction")
    void getTransactionConfirmationThrowsException() {

        assertThrows(UnsupportedTransactionConfirmationException.class, () ->
                confirmationTransformer.getTransactionConfirmation(constructInvalidTransaction()));
    }

    /**
     * Construct a valid transaction record which can be mapped to a {@link Confirmation} model
     * @return a valid transaction
     */
    private Transaction constructValidTransaction() {

        Transaction transaction = new Transaction();

        transaction.setCompanyName(COMPANY_NAME);
        transaction.setCompanyNumber(COMPANY_NUMBER);
        transaction.setDescription(VALID_TRANSACTION_DESCRIPTION);
        transaction.setClosedAt(CLOSED_AT_DATE_STRING);
        transaction.setId(TRANSACTION_ID);

        Map<String, String> closedBy = new HashMap<>();
        closedBy.put(EMAIL_KEY, CLOSED_BY);

        transaction.setClosedBy(closedBy);

        return transaction;
    }

    /**
     * Construct a transaction record which cannot be mapped to a {@link Confirmation} model
     * @return an invalid transaction
     */
    private Transaction constructInvalidTransaction() {

        Transaction transaction = new Transaction();

        transaction.setDescription(INVALID_TRANSACTION_DESCRIPTION);

        return transaction;
    }
}
