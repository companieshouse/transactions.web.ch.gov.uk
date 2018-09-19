package uk.gov.companieshouse.transactions.web.service.confirmation;

import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.confirmation.Confirmation;

public interface ConfirmationService {

    Confirmation getTransactionConfirmation(Transaction transaction) throws ServiceException;
}
