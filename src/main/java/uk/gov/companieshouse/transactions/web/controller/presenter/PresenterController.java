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
import uk.gov.companieshouse.api.model.transaction.Transaction;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.transactions.web.TransactionsWebApplication;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterApiError;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterApiResponse;
import uk.gov.companieshouse.transactions.web.model.presenter.PresenterType;
import uk.gov.companieshouse.transactions.web.service.presenter.PresenterApiService;
import uk.gov.companieshouse.transactions.web.service.transaction.TransactionsService;
import uk.gov.companieshouse.transactions.web.session.SessionService;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for the presenter identity journey.
 *
 * <p>Entry point: {@code GET /transaction/{transactionId}/presenter?returnUrl=...}
 * <p>Flow:
 * <ol>
 *   <li>Store {@code returnUrl} in session; render filer-type selection screen.</li>
 *   <li>{@code POST} validates selected type; routes to stop screen or calls Presenter API.</li>
 *   <li>On success, stores response in session and redirects to statements screen.</li>
 *   <li>User confirms applicable statements; data is PATCHed onto the transaction.</li>
 *   <li>Redirect to the stored {@code returnUrl}.</li>
 * </ol>
 */
@Controller
@RequestMapping("/transaction/{transactionId}/presenter")
public class PresenterController {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(TransactionsWebApplication.APPLICATION_NAME_SPACE);

    private static final String ERROR_PAGE = "error";

    /** Session keys — namespaced to avoid collision with other journeys. */
    static final String SESSION_RETURN_URL = "presenter.return_url";
    static final String SESSION_API_RESPONSE = "presenter.api_response";

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
    private final PresenterApiService presenterApiService;
    private final SessionService sessionService;
    private final ObjectMapper objectMapper;

    @Value("${acsp.sign-in.url}")
    private String acspSignInUrl;

    @Value("${identity.verification.url}")
    private String identityVerificationUrl;

    public PresenterController(TransactionsService transactionsService,
            PresenterApiService presenterApiService,
            SessionService sessionService,
            ObjectMapper objectMapper) {
        this.transactionsService = transactionsService;
        this.presenterApiService = presenterApiService;
        this.sessionService = sessionService;
        this.objectMapper = objectMapper;
    }

    // -----------------------------------------------------------------------
    // Step 1 — filer type selection
    // -----------------------------------------------------------------------

    /**
     * Entry point. Stores the {@code returnUrl} in the session and renders the
     * filer-type selection screen.
     */
    @GetMapping
    public String getPresenterType(@PathVariable String transactionId,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            HttpServletRequest request,
            Model model) {

        LOGGER.infoRequest(request, "Presenter type page requested for transaction " + transactionId, new HashMap<>());

        if (returnUrl != null && !returnUrl.isBlank()) {
            Map<String, Object> session = sessionService.getSessionDataFromContext();
            session.put(SESSION_RETURN_URL, returnUrl);
        }

        model.addAttribute("presenterTypes", PresenterType.values());
        model.addAttribute("transactionId", transactionId);
        return TEMPLATE_PRESENTER_TYPE;
    }

    /**
     * Handles the filer-type form submission.
     */
    @PostMapping
    public String postPresenterType(@PathVariable String transactionId,
            @RequestParam(value = "presenterType", required = false) String presenterTypeParam,
            HttpServletRequest request) {

        LOGGER.infoRequest(request, "Presenter type submitted for transaction " + transactionId, new HashMap<>());

        if (presenterTypeParam == null || presenterTypeParam.isBlank()) {
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX
                    + "/transaction/" + transactionId + "/presenter";
        }

        PresenterType presenterType;
        try {
            presenterType = PresenterType.valueOf(presenterTypeParam);
        } catch (IllegalArgumentException e) {
            LOGGER.errorRequest(request, "Unknown presenter type: " + presenterTypeParam);
            return ERROR_PAGE;
        }

        if (presenterType == PresenterType.NONE_OF_THE_ABOVE) {
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX
                    + "/transaction/" + transactionId + "/presenter/stop/not-eligible";
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

        PresenterApiService.PresenterApiResult result =
                presenterApiService.call(transactionId, companyNumber, formType, chsUserId, presenterType);

        if (result instanceof PresenterApiService.Success success) {
            Map<String, Object> session = sessionService.getSessionDataFromContext();
            session.put(SESSION_API_RESPONSE, success.response());
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX
                    + "/transaction/" + transactionId + "/presenter/statements";
        }

        if (result instanceof PresenterApiService.Failure failure) {
            if (failure.error() == PresenterApiError.ACSP_NOT_LINKED) {
                return UrlBasedViewResolver.REDIRECT_URL_PREFIX
                        + "/transaction/" + transactionId + "/presenter/stop/acsp-not-linked";
            }
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX
                    + "/transaction/" + transactionId + "/presenter/stop/not-verified";
        }

        return ERROR_PAGE;
    }

    // -----------------------------------------------------------------------
    // Step 2 — statements confirmation
    // -----------------------------------------------------------------------

    @GetMapping("/statements")
    public String getStatements(@PathVariable String transactionId,
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
        return TEMPLATE_STATEMENTS;
    }

    @PostMapping("/statements")
    public String postStatements(@PathVariable String transactionId,
            @RequestParam(value = "selectedStatements", required = false) List<String> selectedStatements,
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
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX
                    + "/transaction/" + transactionId + "/presenter/statements?error=noStatements";
        }

        // Replace the full statement list with only the selected ones before persisting.
        apiResponse.setStatements(selectedStatements);

        try {
            transactionsService.patchPresenterData(transactionId, apiResponse);
        } catch (ServiceException e) {
            LOGGER.errorRequest(request, "Failed to patch presenter data: " + e.getMessage(), e);
            return ERROR_PAGE;
        }

        String returnUrl = (String) session.get(SESSION_RETURN_URL);

        // Clean up session state.
        session.remove(SESSION_API_RESPONSE);
        session.remove(SESSION_RETURN_URL);

        if (returnUrl != null && !returnUrl.isBlank()) {
            return UrlBasedViewResolver.REDIRECT_URL_PREFIX + returnUrl;
        }

        // Fallback — should not normally be reached.
        LOGGER.errorRequest(request, "No returnUrl in session after completing presenter journey for " + transactionId);
        return ERROR_PAGE;
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
}
