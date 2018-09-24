package uk.gov.companieshouse.transactions.web.service.confirmation.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.exception.UnsupportedTransactionConfirmationException;
import uk.gov.companieshouse.transactions.web.model.confirmation.Confirmation;
import uk.gov.companieshouse.transactions.web.service.confirmation.ConfirmationService;
import uk.gov.companieshouse.transactions.web.transformer.confirmation.ConfirmationTransformer;

@Service
public class ConfirmationServiceImpl implements ConfirmationService {

    @Autowired
    private ConfirmationTransformer confirmationTransformer;

    /**
     * {@inheritDoc}
     */
    @Override
    public Confirmation getTransactionConfirmation(Transaction transaction)
                                throws ServiceException {

        try {
            return confirmationTransformer.getTransactionConfirmation(transaction);

        } catch (UnsupportedTransactionConfirmationException e) {

            throw new ServiceException(e.getMessage());
        }
    }
}
