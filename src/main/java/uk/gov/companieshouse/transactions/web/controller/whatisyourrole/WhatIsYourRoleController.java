package uk.gov.companieshouse.transactions.web.controller.whatisyourrole;

import static uk.gov.companieshouse.transactions.web.utils.constants.TokenClaims.FORM_TYPE;
import static uk.gov.companieshouse.transactions.web.utils.constants.UrlConstants.CONFIRM_STATEMENTS_PATH;
import static uk.gov.companieshouse.transactions.web.utils.constants.UrlConstants.WHAT_IS_YOUR_ROLE_PATH;

import com.nimbusds.jwt.JWTClaimsSet;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.logging.util.DataMap;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;
import uk.gov.companieshouse.transactions.web.model.whatisyourrole.Role;
import uk.gov.companieshouse.transactions.web.model.whatisyourrole.WhatIsYourRole;
import uk.gov.companieshouse.transactions.web.service.presentertype.PresenterTypesService;
import uk.gov.companieshouse.transactions.web.service.token.TokenService;
import uk.gov.companieshouse.transactions.web.session.SessionService;

@Controller
@RequestMapping(WHAT_IS_YOUR_ROLE_PATH)
public class WhatIsYourRoleController {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    private static final String ERROR_PAGE = "error";

    private static final String TEMPLATE_NAME = "whatIsYourRole";

    private static final String WHAT_IS_YOUR_ROLE_ATTRIBUTE = "whatIsYourRole";

    private static final String TOKEN_PARAM = "token";

    private static final String ROLE_PARAM = "role";

    private static final String TRANSACTION_ID_PLACEHOLDER = "{transactionId}";

    private final PresenterTypesService presenterTypesService;

    private final SessionService sessionService;

    private final TokenService tokenService;

    public WhatIsYourRoleController(PresenterTypesService presenterTypesService,
            SessionService sessionService, TokenService tokenService) {
        this.presenterTypesService = presenterTypesService;
        this.sessionService = sessionService;
        this.tokenService = tokenService;
    }

    @GetMapping
    public String getWhatIsYourRole(@PathVariable("transactionId") String transactionId,
            @RequestParam(value = TOKEN_PARAM, required = true) String token,
            @RequestHeader(value = "X-Request-Id", required = false) String xRequestId,
            Model model) {

        JWTClaimsSet claims = tokenService.verifyChsToken(token, transactionId);

        if (claims == null) {
            LOG.errorContext(xRequestId, "What is your role request received with an invalid token", null, null);
            return ERROR_PAGE;
        }

        String chsUserId = sessionService.getUserId();

        if (isBlank(chsUserId) || isBlank(transactionId)) {
            LOG.errorContext(xRequestId,
                    "What is your role request received without a user id in the session or a transaction id", null,
                    null);
            return ERROR_PAGE;
        }

        String formType = TokenService.getStringClaim(claims, FORM_TYPE);
        DataMap dataMap = new DataMap.Builder().chsUserId(chsUserId).transactionId(transactionId).build();

        LOG.infoContext(xRequestId,
                "What is your role request received for transaction " + transactionId + " and form type " + formType,
                dataMap.getLogMap());

        try {
            List<PresenterType> presenterTypes = presenterTypesService.getPresenterTypes(chsUserId, formType);

            model.addAttribute(WHAT_IS_YOUR_ROLE_ATTRIBUTE, toWhatIsYourRole(presenterTypes));
            model.addAttribute(TOKEN_PARAM, token);
            model.addAttribute("formType", formType);
            model.addAttribute("transactionId", transactionId);
            model.addAttribute("formAction", WHAT_IS_YOUR_ROLE_PATH.replace(TRANSACTION_ID_PLACEHOLDER, transactionId));

            return getTemplateName();

        } catch (ServiceException ex) {

            LOG.errorContext(xRequestId, ex.getMessage(), ex, dataMap.getLogMap());
            return ERROR_PAGE;
        }
    }

    @PostMapping
    public String postWhatIsYourRole(@PathVariable("transactionId") String transactionId,
            @RequestParam(value = TOKEN_PARAM, required = true) String token,
            @RequestParam(value = ROLE_PARAM, required = false) String role,
            @RequestHeader(value = "X-Request-Id", required = false) String xRequestId) {

        JWTClaimsSet claims = tokenService.verifyChsToken(token, transactionId);

        if (claims == null) {
            LOG.errorContext(xRequestId, "What is your role submission received with an invalid token", null, null);
            return ERROR_PAGE;
        }

        String chsUserId = sessionService.getUserId();

        if (isBlank(chsUserId) || isBlank(transactionId)) {
            LOG.errorContext(xRequestId,
                    "What is your role submission received without a user id in the session or a transaction id",
                    null, null);
            return ERROR_PAGE;
        }

        DataMap dataMap = new DataMap.Builder().chsUserId(chsUserId).transactionId(transactionId).build();

        try {
            List<PresenterType> presenterTypes =
                    presenterTypesService.getPresenterTypes(chsUserId, TokenService.getStringClaim(claims, FORM_TYPE));

            if (isBlank(role) || presenterTypes.stream().noneMatch(type -> role.equals(type.getId()))) {
                LOG.errorContext(xRequestId, "What is your role submission received with an invalid role", null,
                        dataMap.getLogMap());
                return ERROR_PAGE;
            }

            String newToken = tokenService.createTransactionsWebToken(claims, role);

            if (newToken == null) {
                LOG.errorContext(xRequestId, "Unable to create the token for the next page", null,
                        dataMap.getLogMap());
                return ERROR_PAGE;
            }

            LOG.infoContext(xRequestId, "Role chosen for transaction " + transactionId, dataMap.getLogMap());

            return UrlBasedViewResolver.REDIRECT_URL_PREFIX + UriComponentsBuilder
                    .fromPath(CONFIRM_STATEMENTS_PATH.replace(TRANSACTION_ID_PLACEHOLDER, transactionId))
                    .queryParam(TOKEN_PARAM, newToken)
                    .build()
                    .encode()
                    .toUriString();

        } catch (ServiceException ex) {

            LOG.errorContext(xRequestId, ex.getMessage(), ex, dataMap.getLogMap());
            return ERROR_PAGE;
        }
    }

    private WhatIsYourRole toWhatIsYourRole(List<PresenterType> presenterTypes) {
        WhatIsYourRole result = new WhatIsYourRole();
        result.setRoles(presenterTypes.stream()
                .map(type -> new Role(type.getId(), type.getDescription()))
                .toList());
        return result;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    @ModelAttribute("templateName")
    private static String getTemplateName() {

        return TEMPLATE_NAME;
    }
}
