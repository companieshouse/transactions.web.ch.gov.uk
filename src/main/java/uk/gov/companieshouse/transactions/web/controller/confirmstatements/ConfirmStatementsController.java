package uk.gov.companieshouse.transactions.web.controller.confirmstatements;

import static uk.gov.companieshouse.transactions.web.utils.constants.UrlConstants.CONFIRM_STATEMENTS_NEXT_PAGE_PATH;
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

import com.nimbusds.jwt.JWTClaimsSet;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.util.DataMap;
import uk.gov.companieshouse.transactions.web.utils.jwthelpers.JwtHelpers;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.statement.Statement;
import uk.gov.companieshouse.transactions.web.service.confirmstatements.ConfirmStatementsService;
import uk.gov.companieshouse.transactions.web.session.SessionService;

@Controller
@RequestMapping(CONFIRM_STATEMENTS_PATH)
public class ConfirmStatementsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    private static final String ERROR_PAGE = "error";

    private static final String TOKEN_PARAM = "token";

    private static final String CLAIM_FORM_TYPE = "form-type";

    private final ConfirmStatementsService confirmStatementsService;

    private final SessionService sessionService;

    public ConfirmStatementsController(
            SessionService sessionService,
            ConfirmStatementsService confirmStatementsService) {
        this.confirmStatementsService = confirmStatementsService;
        this.sessionService = sessionService;
    }

    @GetMapping
    public String getStatementConfirmation(
            @RequestHeader(value = "X_REQUEST_ID", required = false) String xRequestId,
            @RequestParam(value = TOKEN_PARAM, required = true) String jwToken,
            @PathVariable("transactionId") String transactionId,
            HttpServletRequest request,
            Model model) {

        JWTClaimsSet claims = JwtHelpers.readClaims(jwToken);

        if (claims == null) {
            LOGGER.errorContext(xRequestId, "Confirm Statements request JWT blank or malformed", null, null);
            return ERROR_PAGE;
        }

        String chsUserId = sessionService.getUserId();
        String formType = JwtHelpers.getStringClaim(claims, CLAIM_FORM_TYPE);

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
        List<Statement> confirmStatements;

        try {
            confirmStatements = confirmStatementsService.getConfirmStatements(chsUserId);
        } catch (ServiceException e) {
            LOGGER.errorRequest(request, e);
            return ERROR_PAGE;
        }

        model.addAttribute("nextPagePath", CONFIRM_STATEMENTS_NEXT_PAGE_PATH);
        model.addAttribute("statements", confirmStatements);
        model.addAttribute(TOKEN_PARAM, jwToken);
        return getTemplateName();
    }

    @ModelAttribute("templateName")
    private static String getTemplateName() {

        return "confirmStatement";
    }
}