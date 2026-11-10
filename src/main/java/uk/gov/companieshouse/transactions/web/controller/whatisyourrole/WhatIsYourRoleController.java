package uk.gov.companieshouse.transactions.web.controller.whatisyourrole;

import static uk.gov.companieshouse.transactions.web.utils.constants.UrlConstants.CONFIRM_STATEMENTS_PATH;
import static uk.gov.companieshouse.transactions.web.utils.constants.UrlConstants.WHAT_IS_YOUR_ROLE_PATH;

import com.nimbusds.jwt.JWTClaimsSet;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.util.DataMap;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;
import uk.gov.companieshouse.transactions.web.model.whatisyourrole.Role;
import uk.gov.companieshouse.transactions.web.model.whatisyourrole.WhatIsYourRole;
import uk.gov.companieshouse.transactions.web.service.presentertype.PresenterTypesService;
import uk.gov.companieshouse.transactions.web.session.SessionService;
import uk.gov.companieshouse.transactions.web.utils.jwthelpers.JwtHelpers;

@Controller
@RequestMapping(WHAT_IS_YOUR_ROLE_PATH)
public class WhatIsYourRoleController {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    private static final String ERROR_PAGE = "error";

    private static final String TEMPLATE_NAME = "whatIsYourRole";

    private static final String WHAT_IS_YOUR_ROLE_ATTRIBUTE = "whatIsYourRole";

    private static final String TOKEN_PARAM = "token";

    private static final String CLAIM_FORM_TYPE = "form-type";

    private final PresenterTypesService presenterTypesService;

    private final SessionService sessionService;

    public WhatIsYourRoleController(PresenterTypesService presenterTypesService,
            SessionService sessionService) {
        this.presenterTypesService = presenterTypesService;
        this.sessionService = sessionService;
    }

    @GetMapping
    public String getWhatIsYourRole(@PathVariable("transactionId") String transactionId,
            @RequestParam(value = TOKEN_PARAM, required = true) String token,
            @RequestHeader(value = "X-Request-Id", required = false) String xRequestId,
            Model model) {

        JWTClaimsSet claims = addTokenToModel(token, model);

        if (claims == null) {
            LOG.errorContext(xRequestId, "What is your role request received with a missing or malformed token", null,
                    null);
            return ERROR_PAGE;
        }

        String chsUserId = sessionService.getUserId();
        String formType = JwtHelpers.getStringClaim(claims, CLAIM_FORM_TYPE);

        if (chsUserId == null || chsUserId.isBlank() || transactionId == null || transactionId.isBlank()) {
            LOG.errorContext(xRequestId,
                    "What is your role request received without a user id in the session or a transaction id", null,
                    null);
            return ERROR_PAGE;
        }

        DataMap whatIsYourRoleDataMap = new DataMap.Builder().chsUserId(chsUserId).transactionId(transactionId).build();

        LOG.infoContext(xRequestId,
                "What is your role request received for transaction " + transactionId + " and form type " + formType,
                whatIsYourRoleDataMap.getLogMap());

        try {
            List<PresenterType> presenterTypes = presenterTypesService.getPresenterTypes(chsUserId, formType);

            model.addAttribute(WHAT_IS_YOUR_ROLE_ATTRIBUTE, toWhatIsYourRole(presenterTypes));
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("nextPagePath",
                    CONFIRM_STATEMENTS_PATH.replace("{transactionId}", transactionId));

            return getTemplateName();

        } catch (ServiceException ex) {

            LOG.errorContext(xRequestId, ex.getMessage(), ex, whatIsYourRoleDataMap.getLogMap());
            return ERROR_PAGE;
        }
    }

    /**
     * Reads the token and, if it can be read, adds it and its claims to the model
     * so the page can
     * display them and pass the token on. Returns the claims, or {@code null} if
     * the token
     * could not be read. The token is not verified here.
     */
    private JWTClaimsSet addTokenToModel(String token, Model model) {
        JWTClaimsSet claims = JwtHelpers.readClaims(token);

        if (claims != null) {
            model.addAttribute(TOKEN_PARAM, token);
            model.addAttribute("formType", JwtHelpers.getStringClaim(claims, CLAIM_FORM_TYPE));
        }

        return claims;
    }

    private WhatIsYourRole toWhatIsYourRole(List<PresenterType> presenterTypes) {
        WhatIsYourRole result = new WhatIsYourRole();
        result.setRoles(presenterTypes.stream()
                .map(type -> new Role(type.getId(), type.getDescription()))
                .toList());
        return result;
    }

    @ModelAttribute("templateName")
    private static String getTemplateName() {

        return TEMPLATE_NAME;
    }
}
