package uk.gov.companieshouse.transactions.web.controller.confirmstatements;

import java.util.HashMap;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.statement.Statement;
import uk.gov.companieshouse.transactions.web.service.confirmstatements.ConfirmStatementsService;

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
        List<Statement> confirmStatements;

        try {
            confirmStatements = confirmStatementsService.getConfirmStatements(chsUserID);
        } catch (ServiceException e) {
            LOGGER.errorRequest(request, e);
            return ERROR_PAGE;
        }
        model.addAttribute("nextPagePath", NEXT_PAGE);
        model.addAttribute("chsUserId", chsUserID);
        model.addAttribute("formType", formType);
        model.addAttribute("statements", confirmStatements);
        return getTemplateName();
    }

    @ModelAttribute("templateName")
    private static String getTemplateName() {

        return "confirmStatement";
    }
}