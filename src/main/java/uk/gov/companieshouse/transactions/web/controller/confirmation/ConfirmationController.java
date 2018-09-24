package uk.gov.companieshouse.transactions.web.controller.confirmation;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.service.confirmation.ConfirmationService;
import uk.gov.companieshouse.transactions.web.service.transaction.TransactionsService;

@Controller
@RequestMapping("/transaction/{transactionId}/confirmation")
public class ConfirmationController {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    private static final String CONFIRMATION_PAGE = "transactionConfirmation";

    private static final String ERROR_PAGE = "error";

    @Autowired
    TransactionsService transactionsService;

    @Autowired
    ConfirmationService confirmationService;

    @GetMapping
    public String getConfirmation(@PathVariable String transactionId,
                                  HttpServletRequest request,
                                  Model model) {

        try {
            Transaction transaction = transactionsService.getTransaction(transactionId);

            if (!transactionsService.isTransactionClosed(transaction)) {
                LOGGER.errorRequest(request, "Transaction " + transactionId + " has not been closed");
                return ERROR_PAGE;
            }

            model.addAttribute("confirmation", confirmationService.getTransactionConfirmation(transaction));

            return CONFIRMATION_PAGE;

        } catch (ServiceException ex) {

            LOGGER.errorRequest(request, ex.getMessage(), ex);
            return ERROR_PAGE;
        }
    }

}
