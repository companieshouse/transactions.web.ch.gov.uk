package uk.gov.companieshouse.transactions.web.interceptor;

import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.transactions.web.session.SessionService;

@Component
public class UserDetailsInterceptor implements HandlerInterceptor {

    private static final String USER_EMAIL = "userEmail";
    private static final String SIGN_IN_KEY = "signin_info";
    private static final String USER_PROFILE_KEY = "user_profile";
    private static final String EMAIL_KEY = "email";

    @Autowired
    private SessionService sessionService;

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                           @NonNull Object handler, @Nullable ModelAndView modelAndView) {

        if (modelAndView != null && shouldAttributeBeAdded(request, modelAndView)) {

            Map<String, Object> sessionData = sessionService.getSessionDataFromContext();
            Map<String, Object> signInInfo = (Map<String, Object>) sessionData.get(SIGN_IN_KEY);
            if (signInInfo != null) {
                Map<String, Object> userProfile = (Map<String, Object>) signInInfo
                        .get(USER_PROFILE_KEY);
                modelAndView.addObject(USER_EMAIL, userProfile.get(EMAIL_KEY));
            }
        }
    }

    /**
     * Determines whether to add a model attribute; returns true for all {@code GET} requests, and for
     * {@code POST} requests which don't force a redirect (to cater for form submissions with errors)
     *
     * @param request The HttpServletRequest
     * @param modelAndView The returned ModelAndView
     * @return true or false
     */
    private boolean shouldAttributeBeAdded(HttpServletRequest request, ModelAndView modelAndView) {
        return (request.getMethod().equalsIgnoreCase("GET") ||
               (request.getMethod().equalsIgnoreCase("POST") &&
                !Objects.requireNonNull(modelAndView.getViewName()).startsWith(UrlBasedViewResolver.REDIRECT_URL_PREFIX)));
    }
}
