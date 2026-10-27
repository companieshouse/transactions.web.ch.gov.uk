package uk.gov.companieshouse.transactions.web.controller.presenter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;
import uk.gov.companieshouse.transactions.web.exception.AcspNotLinkedException;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.exception.UnverifiedException;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterApiResponse;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterType;
import uk.gov.companieshouse.transactions.web.service.presenter.PresenterService;
import uk.gov.companieshouse.transactions.web.service.transaction.TransactionsService;
import uk.gov.companieshouse.transactions.web.session.SessionService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static uk.gov.companieshouse.transactions.web.controller.presenter.JwtUtils.getStringClaim;

/**
 * Controller for the presenter identity journey.
 *
 * <p>Entry point: {@code GET /transaction/{transactionId}/presenter?token=...}
 * <p>Flow:
 * <ol>
 *   <li>Verify the {@code token} (HS256, {@code CHS_JWT_SECRET}); add its claims to the model;
 *       render filer-type selection screen.</li>
 *   <li>{@code POST} validates selected type; routes to stop screen or calls Presenter API.
 *       The {@code token} is submitted as a hidden form field and kept on every redirect so
 *       each page can show its data.</li>
 *   <li>On success, stores response in session and redirects to statements screen.</li>
 *   <li>User confirms applicable statements; data is PATCHed onto the transaction.</li>
 *   <li>Redirect to the token's {@code return_url}.</li>
 * </ol>
 */
@Controller
@RequestMapping("/transaction/{transactionId}/presenter")
public class PresenterController {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    private static final String ERROR_PAGE = "error";

    /** Session keys — namespaced to avoid collision with other journeys. */
    static final String SESSION_API_RESPONSE = "presenter.api_response";

    /**
     * Query parameter carrying the presenter JWT. The token is passed from page to page
     * (rather than held in session) so every page can show the calling service's data.
     */
    static final String TOKEN_PARAM = "token";

    private static final String PRESENTER_PATH = "/transaction/{transactionId}/presenter";

    /** Claim names in the presenter redirect JWT issued by the calling service. */
    private static final String CLAIM_FORM_TYPE = "form_type";
    private static final String CLAIM_TRANSACTION_ID = "transaction_id";
    private static final String CLAIM_RETURN_URL = "return_url";
    private static final String CLAIM_BACK_URL = "back_url";
    private static final String CLAIM_LANG = "lang";

    /** Template names. */
    private static final String TEMPLATE_PRESENTER_TYPE = "presenter/presenterType";
    private static final String TEMPLATE_STATEMENTS = "presenter/presenterStatements";
    private static final String TEMPLATE_STOP_NOT_ELIGIBLE = "presenter/stop/notEligible";
    private static final String TEMPLATE_STOP_ACSP_NOT_LINKED = "presenter/stop/acspNotLinked";
    private static final String TEMPLATE_STOP_NOT_VERIFIED = "presenter/stop/notVerified";

    /** Form-type lookup: maps transaction description keywords to form type codes. */
    private static final Map<String, String> FORM_TYPE_MAP;

    static {
        FORM_TYPE_MAP = new HashMap<>();
        FORM_TYPE_MAP.put("PSC Verification Transaction", "VS01");
        // Add additional mappings here as more form types are supported.
    }

    private final TransactionsService transactionsService;
    private final PresenterService presenterService;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;

    @Value("${acsp.sign-in.url}")
    private String acspSignInUrl;

    @Value("${identity.verification.url}")
    private String identityVerificationUrl;

    @Value("${chs.jwt.secret}")
    private String chsJwtSecret;

    public PresenterController(TransactionsService transactionsService,
                               PresenterService presenterService,
            SessionService sessionService,
            ObjectMapper objectMapper) {
        this.transactionsService = transactionsService;
        this.presenterService = presenterService;
        this.sessionService = sessionService;
        this.objectMapper = objectMapper;
    }

    // -----------------------------------------------------------------------
    // Step 1 — filer type selection
    // -----------------------------------------------------------------------

    /**
     * Entry point. Verifies the supplied {@code token}, adds its claims to the model
     * and renders the filer-type selection screen. The token is ignored if its {@code transaction_id}
     * does not match the transaction in the path.
     */
    @GetMapping
    public String getPresenterType(@PathVariable String transactionId,
            @RequestParam(value = TOKEN_PARAM, required = false) String jwt,
            HttpServletRequest request,
            Model model) {

        LOGGER.infoRequest(request, "Presenter type page requested for transaction " + transactionId, new HashMap<>());

        addTokenToModel(jwt, model);

        // Retrieve transaction to get company number and derive form type.
        Transaction transaction;
        try {
            transaction = transactionsService.getTransaction(transactionId);
        } catch (ServiceException e) {
            LOGGER.errorRequest(request, "Failed to retrieve transaction: " + e.getMessage(), e);
            return ERROR_PAGE;
        }

        String formType = FORM_TYPE_MAP.getOrDefault(transaction.getDescription(), "UNKNOWN");
        String chsUserId = getUserIdFromSession();

        List<String> presenterTypes = presenterService.getPresenterTypes(chsUserId, formType);

        model.addAttribute("presenterTypes", presenterTypes);
        model.addAttribute("transactionId", transactionId);
        return TEMPLATE_PRESENTER_TYPE;
    }

    /**
     * Builds a redirect to a page in this journey, keeping the token on the URL when present.
     * The token is not re-verified here; the receiving page verifies it before using it.
     */
    private static String redirectToPresenterPage(String transactionId, String subPath, String token) {
        return redirectToPresenterPage(UriComponentsBuilder.fromPath(PRESENTER_PATH + subPath), transactionId, token);
    }

    private static String redirectToPresenterPage(UriComponentsBuilder uri, String transactionId, String token) {
        if (token != null && !token.isBlank()) {
            uri.queryParam(TOKEN_PARAM, token);
        }
        return UrlBasedViewResolver.REDIRECT_URL_PREFIX + uri.buildAndExpand(transactionId).encode().toUriString();
    }

    /**
     * Handles the filer-type form submission.
     */
    @PostMapping
    public String postPresenterType(@PathVariable String transactionId,
            @RequestParam(value = "presenterType", required = false) String presenterTypeParam,
            @RequestParam(value = TOKEN_PARAM, required = false) String token,
            HttpServletRequest request) {

        LOGGER.infoRequest(request, "Presenter type submitted for transaction " + transactionId, new HashMap<>());

        if (presenterTypeParam == null || presenterTypeParam.isBlank()) {
            return redirectToPresenterPage(transactionId, "", token);
        }

        PresenterType presenterType;
        try {
            presenterType = PresenterType.valueOf(presenterTypeParam);
        } catch (IllegalArgumentException e) {
            LOGGER.errorRequest(request, "Unknown presenter type: " + presenterTypeParam);
            return ERROR_PAGE;
        }

        if (presenterType == PresenterType.NONE_OF_THE_ABOVE) {
            return redirectToPresenterPage(transactionId, "/stop/not-eligible", token);
        }

        // Retrieve transaction to get company number and derive form type.
        Transaction transaction;
        try {
            transaction = transactionsService.getTransaction(transactionId);
        } catch (ServiceException e) {
            LOGGER.errorRequest(request, "Failed to retrieve transaction: " + e.getMessage(), e);
            return ERROR_PAGE;
        }

        String companyNumber = transaction.getCompanyNumber();
        String formType = FORM_TYPE_MAP.getOrDefault(transaction.getDescription(), "UNKNOWN");
        String chsUserId = getUserIdFromSession();

        try {
            PresenterApiResponse response = presenterService.getStatements(transactionId, companyNumber, formType, chsUserId, presenterType);
            Map<String, Object> session = sessionService.getSessionDataFromContext();
            session.put(SESSION_API_RESPONSE, response);
            return redirectToPresenterPage(transactionId, "/statements", token);
        } catch (AcspNotLinkedException e) {
            return redirectToPresenterPage(transactionId, "/stop/acsp-not-linked", token);
        } catch (UnverifiedException e) {
            return redirectToPresenterPage(transactionId, "/stop/not-verified", token);
        }
    }

    // -----------------------------------------------------------------------
    // Step 2 — statements confirmation
    // -----------------------------------------------------------------------

    @GetMapping("/statements")
    public String getStatements(@PathVariable String transactionId,
            @RequestParam(value = TOKEN_PARAM, required = false) String token,
            HttpServletRequest request,
            Model model) {

        Map<String, Object> session = sessionService.getSessionDataFromContext();
        PresenterApiResponse apiResponse = objectMapper.convertValue(session.get(SESSION_API_RESPONSE), PresenterApiResponse.class);

        if (apiResponse == null) {
            LOGGER.errorRequest(request,
                    "No presenter API response in session for transaction " + transactionId);
            return ERROR_PAGE;
        }

        model.addAttribute("statements", apiResponse.getStatements());
        model.addAttribute("individualUser", apiResponse.getIndividualUser());
        model.addAttribute("transactionId", transactionId);
        addTokenToModel(token, model);
        return TEMPLATE_STATEMENTS;
    }

    @PostMapping("/statements")
    public String postStatements(@PathVariable String transactionId,
            @RequestParam(value = "selectedStatements", required = false) List<String> selectedStatements,
            @RequestParam(value = TOKEN_PARAM, required = false) String token,
            HttpServletRequest request) {

        Map<String, Object> session = sessionService.getSessionDataFromContext();
        PresenterApiResponse apiResponse = objectMapper.convertValue(session.get(SESSION_API_RESPONSE), PresenterApiResponse.class);

        if (apiResponse == null) {
            LOGGER.errorRequest(request,
                    "No presenter API response in session for transaction " + transactionId);
            return ERROR_PAGE;
        }

        if (selectedStatements == null || selectedStatements.isEmpty()) {
            // Re-render the statements page with an error — at least one statement must be confirmed.
            return redirectToPresenterPage(
                    UriComponentsBuilder.fromPath(PRESENTER_PATH + "/statements").queryParam("error", "noStatements"),
                    transactionId, token);
        }

        // Checked before patching so the transaction is not updated when we cannot send the user back.
        JWTClaimsSet claims = JwtUtils.verifyToken(token, chsJwtSecret);

        if (claims == null) {
            LOGGER.errorRequest(request, "Null presenter token for transaction " + transactionId);
            return ERROR_PAGE;
        }

        // Stops a token issued for one transaction being replayed against another
        if (!transactionId.equals(getStringClaim(claims, CLAIM_TRANSACTION_ID))) {
            LOGGER.errorRequest(request, "Presenter JWT rejected: transaction_id does not match transaction "
                    + transactionId);
            return null;
        }
        String returnUrl = getStringClaim(claims, CLAIM_RETURN_URL);

        if (returnUrl == null || returnUrl.isBlank()) {
            LOGGER.errorRequest(request, "No valid return_url in presenter token for transaction " + transactionId);
            return ERROR_PAGE;
        }

        // Replace the full statement list with only the selected ones before persisting.
        apiResponse.setStatements(selectedStatements);

        try {
            transactionsService.patchPresenterData(transactionId, apiResponse);
        } catch (ServiceException e) {
            LOGGER.errorRequest(request, "Failed to patch presenter data: " + e.getMessage(), e);
            return ERROR_PAGE;
        }

        session.remove(SESSION_API_RESPONSE);

        return UrlBasedViewResolver.REDIRECT_URL_PREFIX + returnUrl;
    }

    // -----------------------------------------------------------------------
    // Stop screens
    // -----------------------------------------------------------------------

    @GetMapping("/stop/not-eligible")
    public String stopNotEligible(HttpServletRequest request, Model model) {
        LOGGER.infoRequest(request, "Presenter stop: not eligible", new HashMap<>());
        model.addAttribute("eligibleTypes", PresenterType.values());
        return TEMPLATE_STOP_NOT_ELIGIBLE;
    }

    @GetMapping("/stop/acsp-not-linked")
    public String stopAcspNotLinked(HttpServletRequest request, Model model) {
        LOGGER.infoRequest(request, "Presenter stop: ACSP not linked", new HashMap<>());
        model.addAttribute("acspSignInUrl", acspSignInUrl);
        return TEMPLATE_STOP_ACSP_NOT_LINKED;
    }

    @GetMapping("/stop/not-verified")
    public String stopNotVerified(HttpServletRequest request, Model model) {
        LOGGER.infoRequest(request, "Presenter stop: identity not verified", new HashMap<>());
        model.addAttribute("identityVerificationUrl", identityVerificationUrl);
        return TEMPLATE_STOP_NOT_VERIFIED;
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private String getUserIdFromSession() {
        Map<String, Object> sessionData = sessionService.getSessionDataFromContext();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> signInInfo = (Map<String, Object>) sessionData.get("signin_info");
            if (signInInfo != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> userProfile = (Map<String, Object>) signInInfo.get("user_profile");
                if (userProfile != null) {
                    return (String) userProfile.get("id");
                }
            }
        } catch (ClassCastException e) {
            LOGGER.error("Unable to extract user ID from session", e);
        }
        return null;
    }

    /**
     * Verifies the token and, if valid, adds it and its claims to the model so the page can
     * display them and pass the token on. Returns the claims, or {@code null} if the token
     * was not accepted.
     */
    private void addTokenToModel(String jwt, Model model) {
        JWTClaimsSet claims = JwtUtils.verifyToken(jwt, chsJwtSecret);

        if (claims != null) {
            model.addAttribute(TOKEN_PARAM, jwt);
            model.addAttribute("formType", getStringClaim(claims, CLAIM_FORM_TYPE));
            model.addAttribute("returnUrl", getStringClaim(claims, CLAIM_RETURN_URL));
            model.addAttribute("backUrl", getStringClaim(claims, CLAIM_BACK_URL));
            model.addAttribute("lang", getStringClaim(claims, CLAIM_LANG));
        }
    }
}
