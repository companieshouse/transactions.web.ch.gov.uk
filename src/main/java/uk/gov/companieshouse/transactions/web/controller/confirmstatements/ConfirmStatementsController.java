package uk.gov.companieshouse.transactions.web.controller.confirmstatements;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.websocket.server.PathParam;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.api.model.transaction.TransactionStatus;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.statements.Statements;
import uk.gov.companieshouse.transactions.web.service.confirmation.ConfirmationService;
import uk.gov.companieshouse.transactions.web.service.confirmstatements.ConfirmStatementsService;
import uk.gov.companieshouse.transactions.web.service.transaction.TransactionsService;

import uk.gov.companieshouse.transactions.web.session.SessionService;

@Controller
@RequestMapping("/transaction/confirm")
public class ConfirmStatementsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    private static final String ERROR_PAGE = "error";

    // Placeholder until we have the next page path
    private static final String NEXT_PAGE = "transaction/next-page";

    private final ConfirmStatementsService confirmStatementsService;

    public ConfirmStatementsController(
            ConfirmStatementsService confirmStatementsService) {
        this.confirmStatementsService = confirmStatementsService;
    }

    @GetMapping
    public String getStatementConfirmation(
            @RequestParam("chs-user-id") String chsUserID,
            @RequestParam("form-type") String formType,
            HttpServletRequest request,
            Model model) {
        LOGGER.infoRequest(request, "Confirm Statements Page GET Request", new HashMap<>());
        model.addAttribute("nextPagePath", NEXT_PAGE);

        try {
            model.addAttribute("chsUserId", chsUserID);
            model.addAttribute("formType", formType);
            Statements toConfirm = confirmStatementsService.getConfirmStatements(chsUserID);
            model.addAttribute("statements", toConfirm.getStatementList());
            // Put to transactions API to be added later.
        } catch (Exception e) {
            LOGGER.errorRequest(request, e);
            return ERROR_PAGE;
        }
        return getTemplateName();
    }

    @ModelAttribute("templateName")
    private static String getTemplateName() {

        return "confirmStatement";
    }
}