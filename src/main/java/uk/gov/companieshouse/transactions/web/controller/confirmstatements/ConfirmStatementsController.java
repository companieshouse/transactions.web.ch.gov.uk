package uk.gov.companieshouse.transactions.web.controller.confirmstatements;

import static uk.gov.companieshouse.transactions.web.util.constants.UrlConstants.CONFIRM_STATEMENTS_NEXT_PAGE_PATH;
import static uk.gov.companieshouse.transactions.web.util.constants.UrlConstants.CONFIRM_STATEMENTS_PATH;

import java.text.ParseException;
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
import com.nimbusds.jwt.JWTParser;

import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.util.DataMap;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.statement.Statement;
import uk.gov.companieshouse.transactions.web.service.confirmstatements.ConfirmStatementsService;

@Controller
@RequestMapping(CONFIRM_STATEMENTS_PATH)
public class ConfirmStatementsController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    private static final String ERROR_PAGE = "error";

    private static final String TOKEN_PARAM = "token";

    private static final String CLAIM_CHS_USER_ID = "chs-user-id";

    private static final String CLAIM_FORM_TYPE = "form-type";

    private final ConfirmStatementsService confirmStatementsService;

    public ConfirmStatementsController(
            ConfirmStatementsService confirmStatementsService) {
        this.confirmStatementsService = confirmStatementsService;
    }

    @GetMapping
    public String getStatementConfirmation(
            @RequestHeader(value = "X_REQUEST_ID", required = false) String xRequestId,
            @RequestParam(value = TOKEN_PARAM, required = true) String jwToken,
            @PathVariable("transactionId") String transactionId,
            HttpServletRequest request,
            Model model) {

        JWTClaimsSet claims = readClaims(jwToken);

        if (claims == null) {
            LOGGER.errorContext(xRequestId, "Confirm Statements request JWT blank or malformed", null, null);
            return ERROR_PAGE;
        }

        String chsUserId = getStringClaim(claims, CLAIM_CHS_USER_ID);
        String formType = getStringClaim(claims, CLAIM_FORM_TYPE);

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

    private JWTClaimsSet readClaims(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        try {
            return JWTParser.parse(token).getJWTClaimsSet();
        } catch (ParseException ex) {
            return null;
        }
    }

    private String getStringClaim(JWTClaimsSet claims, String name) {
        Object value = claims.getClaim(name);
        return value instanceof String string ? string : null;
    }

    @ModelAttribute("templateName")
    private static String getTemplateName() {

        return "confirmStatement";
    }
}