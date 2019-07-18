package uk.gov.companieshouse.transactions.web.controller.confirmation;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.service.confirmation.ConfirmationService;
import uk.gov.companieshouse.transactions.web.service.transaction.TransactionsService;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/transaction/{transactionId}/confirmation")
public class ConfirmationController {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    private static final String ERROR_PAGE = "error";

    @Autowired
    private TransactionsService transactionsService;

    @Autowired
    private ConfirmationService confirmationService;

    @GetMapping
    public String getConfirmation(@PathVariable String transactionId,
                                  HttpServletRequest request,
                                  @RequestParam("status") Optional<String> paymentStatus,
                                  Model model) {

        try {
            Transaction transaction = transactionsService.getTransaction(transactionId);

            if (paymentStatus.isPresent() && paymentStatus.get().equals("cancelled")) {

                return UrlBasedViewResolver.REDIRECT_URL_PREFIX + transaction.getResumeJourneyUri();
            }

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
