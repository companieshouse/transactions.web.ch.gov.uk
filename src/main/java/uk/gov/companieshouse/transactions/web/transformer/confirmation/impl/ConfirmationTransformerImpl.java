package uk.gov.companieshouse.transactions.web.transformer.confirmation.impl;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.transactions.web.confirmation.ConfirmationDescription;
import uk.gov.companieshouse.transactions.web.exception.UnsupportedTransactionConfirmationException;
import uk.gov.companieshouse.transactions.web.model.confirmation.Confirmation;
import uk.gov.companieshouse.transactions.web.transformer.confirmation.ConfirmationTransformer;

@Component
public class ConfirmationTransformerImpl implements ConfirmationTransformer {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMMM yyyy");

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("h:mma");

    private static final String EMAIL_KEY = "email";

    /**
     * {@inheritDoc}
     */
    @Override
    public Confirmation getTransactionConfirmation(Transaction transaction) throws UnsupportedTransactionConfirmationException {

        Confirmation confirmation = new Confirmation();

        confirmation.setCompanyName(transaction.getCompanyName());
        confirmation.setCompanyNumber(transaction.getCompanyNumber());

        String confirmationDescription =
                ConfirmationDescription.getConfirmationDescription(transaction.getDescription());
        confirmation.setConfirmationDescription(confirmationDescription);

        Instant closedInstant = Instant.parse(transaction.getClosedAt());
        LocalDateTime closedAt = LocalDateTime.ofInstant(closedInstant, ZoneOffset.UTC);

        confirmation.setClosedAtDate(closedAt.format(DATE_FORMATTER));
        confirmation.setClosedAtTime(closedAt.format(TIME_FORMATTER).toLowerCase());
        confirmation.setTransactionId(transaction.getId());
        confirmation.setClosedBy(transaction.getClosedBy().get(EMAIL_KEY));

        return confirmation;
    }
}
