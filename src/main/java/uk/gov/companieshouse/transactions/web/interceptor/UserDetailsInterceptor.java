package uk.gov.companieshouse.transactions.web.interceptor;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.UrlBasedViewResolver;
import uk.gov.companieshouse.transactions.web.session.SessionService;

@Component
public class UserDetailsInterceptor extends HandlerInterceptorAdapter {

    private static final String USER_EMAIL = "userEmail";

    private static final String SIGN_IN_KEY = "signin_info";
    private static final String USER_PROFILE_KEY = "user_profile";
    private static final String EMAIL_KEY = "email";

    @Autowired
    private SessionService sessionService;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {

        if (modelAndView != null && (request.getMethod().equalsIgnoreCase("GET") ||
                (request.getMethod().equalsIgnoreCase("POST") &&
                     !modelAndView.getViewName().startsWith(UrlBasedViewResolver.REDIRECT_URL_PREFIX)))) {

            Map<String, Object> sessionData = sessionService.getSessionDataFromContext();
            Map<String, Object> signInInfo = (Map<String, Object>) sessionData.get(SIGN_IN_KEY);
            if (signInInfo != null) {
                Map<String, Object> userProfile = (Map<String, Object>) signInInfo
                        .get(USER_PROFILE_KEY);
                modelAndView.addObject(USER_EMAIL, userProfile.get(EMAIL_KEY));
            }
        }
    }
}
