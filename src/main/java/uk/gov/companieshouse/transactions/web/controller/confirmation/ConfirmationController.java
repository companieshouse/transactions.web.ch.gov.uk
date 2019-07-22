package uk.gov.companieshouse.transactions.web.controller.confirmation;

import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.service.confirmation.ConfirmationService;
import uk.gov.companieshouse.transactions.web.service.transaction.TransactionsService;

import javax.servlet.http.HttpServletRequest;
import uk.gov.companieshouse.transactions.web.session.SessionService;

@Controller
@RequestMapping("/transaction/{transactionId}/confirmation")
public class ConfirmationController {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    private static final String ERROR_PAGE = "error";

    private static final String PAYMENT_STATE = "payment_state";

    @Autowired
    private TransactionsService transactionsService;

    @Autowired
    private ConfirmationService confirmationService;

    @Autowired
    private SessionService sessionService;

    @GetMapping
    public String getConfirmation(@PathVariable String transactionId,
                                  @RequestParam("state") Optional<String> paymentState,
                                  HttpServletRequest request,
                                  Model model) {

        if (paymentState.isPresent()) {

            Map<String, Object> sessionData = sessionService.getSessionDataFromContext();

            String sessionPaymentState = (String) sessionData.get(PAYMENT_STATE);
            sessionData.remove(PAYMENT_STATE);

            if (!paymentState.get().equals(sessionPaymentState)) {

                LOGGER.errorRequest(request, "Payment state appears to have been tampered! "
                        + "Expected: " + sessionPaymentState + ", Received: " + paymentState.get());
                return ERROR_PAGE;
            }
        }

        try {
            Transaction transaction = transactionsService.getTransaction(transactionId);

            if (!transactionsService.isTransactionClosedOrClosedPendingPayment(transaction)) {
                LOGGER.errorRequest(request, "Transaction " + transactionId + " has not been closed");
                return ERROR_PAGE;
            }

            model.addAttribute("confirmation", confirmationService.getTransactionConfirmation(transaction));

            return getTemplateName();

        } catch (ServiceException ex) {

            LOGGER.errorRequest(request, ex.getMessage(), ex);
            return ERROR_PAGE;
        }
    }

    @ModelAttribute("templateName")
    private static String getTemplateName() {

        return "transactionConfirmation";
    }
}
