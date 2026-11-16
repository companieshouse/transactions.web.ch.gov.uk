package uk.gov.companieshouse.transactions.web.controller.confirmstatements;

import static uk.gov.companieshouse.transactions.web.utils.constants.TokenClaims.RETURN_URL;
import static uk.gov.companieshouse.transactions.web.utils.constants.UrlConstants.CONFIRM_STATEMENTS_PATH;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import com.nimbusds.jwt.JWTClaimsSet;

import uk.gov.companieshouse.api.model.transaction.ProtectedData;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.util.DataMap;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.statement.Statement;
import uk.gov.companieshouse.transactions.web.service.confirmstatements.ConfirmStatementsService;
import uk.gov.companieshouse.transactions.web.service.token.TokenService;
import uk.gov.companieshouse.transactions.web.session.SessionService;
import uk.gov.companieshouse.transactions.web.utils.constants.TokenClaims;

@Controller
@RequestMapping(CONFIRM_STATEMENTS_PATH)
public class ConfirmStatementsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    private static final String ERROR_PAGE = "error";

    private static final String TOKEN_PARAM = "token";

    private static final String CLAIM_FORM_TYPE = TokenClaims.FORM_TYPE;

    private final ConfirmStatementsService confirmStatementsService;

    private final SessionService sessionService;

    private final TokenService tokenService;

    private final TransactionsService transactionsService;

    public ConfirmStatementsController(
            SessionService sessionService,
            ConfirmStatementsService confirmStatementsService,
            TokenService tokenService
            TransactionsService transactionsService) {
        this.transactionsService = transactionsService;
        this.confirmStatementsService = confirmStatementsService;
        this.sessionService = sessionService;
        this.tokenService = tokenService;
    }

    @GetMapping
    public String getStatementConfirmation(
            @RequestHeader(value = "X_REQUEST_ID", required = false) String xRequestId,
            @RequestParam(value = TOKEN_PARAM, required = true) String jwToken,
            @PathVariable("transactionId") String transactionId,
            @RequestParam(value = "role", required = false) String userRole,
            @RequestParam(value = "selectedStatements", required = false) List<String> selectedStatements,
            HttpServletRequest request,
            Model model) {

        JWTClaimsSet claims = tokenService.verifyTransactionsWebToken(jwToken, transactionId);

        if (claims == null) {
            LOGGER.errorContext(xRequestId, "Confirm Statements request received with an invalid token", null, null);
            return ERROR_PAGE;
        }

        String chsUserId = sessionService.getUserId();
        String formType = TokenService.getStringClaim(claims, CLAIM_FORM_TYPE);

        if (chsUserId == null || chsUserId.isBlank() || transactionId == null || transactionId.isBlank()) {
            LOGGER.errorContext(xRequestId,
                    "Confirm statements received without a user id in the session or a transaction id", null, null);
            return ERROR_PAGE;
        }

        DataMap confirmStatementsDataMap = new DataMap.Builder().chsUserId(chsUserId).transactionId(transactionId)
                .build();
        LOGGER.infoContext(xRequestId,
                "Confirm statements request received for " + transactionId + " and form type " + formType,
                confirmStatementsDataMap.getLogMap());

        if (selectedStatements != null && !selectedStatements.isEmpty()) {
            // PUT/PATCH to go in here, I think.
            String redirectUrl = TokenService.getStringClaim(claims, RETURN_URL);
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX + redirectUrl + "?lang="
                    + TokenService.getStringClaim(claims, "lang") + "&token=" + jwToken;
        }

        List<Statement> confirmStatements;
        try {
            confirmStatements = confirmStatementsService.getConfirmStatements(chsUserId);
            // need to move this into post, triggered by user submit
            Transaction transaction = new Transaction();
            transaction.setPresenterStatements(new String [] {"verified-statement"});
            transactionsService.updateTransaction(transactionId, transaction);

            TransactionWithProtectedData patchTransaction = new TransactionWithProtectedData();
            patchTransaction.setProtectedData(new ProtectedData());
            var protectedData = patchTransaction.getProtectedData();
            protectedData.setFormType("ACSP01");
            transactionsService.patchTransaction(transactionId, patchTransaction);
        } catch (ServiceException e) {
            LOGGER.errorRequest(request, e);
            return ERROR_PAGE;
        }

        model.addAttribute(TOKEN_PARAM, jwToken);
        model.addAttribute("statements", confirmStatements);
        return getTemplateName();
    }

    @ModelAttribute("templateName")
    private static String getTemplateName() {
        return "confirmStatement";
    }
}