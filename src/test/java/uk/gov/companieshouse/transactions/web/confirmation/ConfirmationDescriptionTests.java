package uk.gov.companieshouse.transactions.web.confirmation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.transactions.web.exception.UnsupportedTransactionConfirmationException;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ConfirmationDescriptionTests {

    @Test
    @DisplayName("Get Confirmation Description - Valid Transaction Description")
    void getConfirmationDescriptionSuccess() throws UnsupportedTransactionConfirmationException {

        String validTransactionDescription = "Small Full Accounts";

        String expectedConfirmationDesction = "Full accounts";

        String confirmationDescription =
                ConfirmationDescription.getConfirmationDescription(validTransactionDescription);

        assertEquals(expectedConfirmationDesction, confirmationDescription);
    }

    @Test
    @DisplayName("Get Confirmation Description - Invalid Transaction Description")
    void getConfirmationDescriptionThrowsException() {

        String invalidTransactionDescription = "invalidTransactionDescription";

        assertThrows(UnsupportedTransactionConfirmationException.class, () ->
                ConfirmationDescription.getConfirmationDescription(invalidTransactionDescription));
    }

}
