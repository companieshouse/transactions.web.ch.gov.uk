package uk.gov.companieshouse.transactions.web.controller.whatisyourrole;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.companieshouse.transactions.web.utils.constants.UrlConstants.WHAT_IS_YOUR_ROLE_PATH;

import com.nimbusds.jwt.JWTClaimsSet;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;
import uk.gov.companieshouse.transactions.web.model.whatisyourrole.WhatIsYourRole;
import uk.gov.companieshouse.transactions.web.service.presentertype.PresenterTypesService;
import uk.gov.companieshouse.transactions.web.service.token.TokenService;
import uk.gov.companieshouse.transactions.web.session.SessionService;

@ExtendWith(MockitoExtension.class)
class WhatIsYourRoleControllerTests {

    private static final String USER_ID = "1234";

    private static final String FORM_TYPE = "AD01";

    private static final String TRANSACTION_ID = "123456-654321-123456";

    private static final String TOKEN = "incoming.chs.token";

    private static final String NEW_TOKEN = "new.transactions-web.token";

    private static final String ROLE = "id";

    private MockMvc mockMvc;

    @Mock
    private PresenterTypesService presenterTypesService;

    @Mock
    private SessionService sessionService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private WhatIsYourRoleController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET returns the what is your role view with the model")
    void getWhatIsYourRole() throws Exception {

        givenValidToken();
        givenSessionUserId(USER_ID);
        List<PresenterType> presenterTypes = List.of(new PresenterType(ROLE, "Description"));
        when(presenterTypesService.getPresenterTypes(USER_ID, FORM_TYPE)).thenReturn(presenterTypes);

        MvcResult result = mockMvc.perform(get(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("whatIsYourRole"))
                .andExpect(model().attribute("token", TOKEN))
                .andExpect(model().attribute("formType", FORM_TYPE))
                .andExpect(model().attribute("transactionId", TRANSACTION_ID))
                .andExpect(model().attribute("formAction",
                        WHAT_IS_YOUR_ROLE_PATH.replace("{transactionId}", TRANSACTION_ID)))
                .andExpect(model().attribute("templateName", "whatIsYourRole"))
                .andReturn();

        WhatIsYourRole whatIsYourRole =
                (WhatIsYourRole) result.getModelAndView().getModel().get("whatIsYourRole");
        assertEquals(1, whatIsYourRole.getRoles().size());
        assertEquals(ROLE, whatIsYourRole.getRoles().get(0).getId());
        assertEquals("Description", whatIsYourRole.getRoles().get(0).getDescription());
    }

    @Test
    @DisplayName("GET returns the error view if the presenter types cannot be retrieved")
    void getWhatIsYourRoleServiceException() throws Exception {

        givenValidToken();
        givenSessionUserId(USER_ID);
        when(presenterTypesService.getPresenterTypes(USER_ID, FORM_TYPE))
                .thenThrow(new ServiceException("error"));

        mockMvc.perform(get(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    @DisplayName("GET without a token returns bad request")
    void getWithoutToken() throws Exception {

        mockMvc.perform(get(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET with a token that fails verification returns the error view")
    void getWithInvalidToken() throws Exception {

        mockMvc.perform(get(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verifyNoInteractions(presenterTypesService);
    }

    @Test
    @DisplayName("GET with no signed-in user in the session returns the error view")
    void getWithNoUserInSession() throws Exception {

        givenValidToken();
        when(sessionService.getUserId()).thenReturn(null);

        mockMvc.perform(get(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verifyNoInteractions(presenterTypesService);
    }

    @Test
    @DisplayName("POST redirects to confirm statements with a new token containing the role")
    void postRedirectsWithNewToken() throws Exception {

        JWTClaimsSet claims = givenValidToken();
        givenSessionUserId(USER_ID);
        givenPresenterTypes();
        when(tokenService.createTransactionsWebToken(claims, ROLE)).thenReturn(NEW_TOKEN);

        mockMvc.perform(post(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("token", TOKEN).param("role", ROLE))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(
                        "/transaction/" + TRANSACTION_ID + "/presenter/confirmstatements?token=" + NEW_TOKEN));

        verify(tokenService).createTransactionsWebToken(claims, ROLE);
    }

    @Test
    @DisplayName("POST without a token returns bad request")
    void postWithoutToken() throws Exception {

        mockMvc.perform(post(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("role", ROLE))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST with a token that fails verification returns the error view")
    void postWithInvalidToken() throws Exception {

        mockMvc.perform(post(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("token", TOKEN).param("role", ROLE))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verifyNoInteractions(presenterTypesService);
    }

    @Test
    @DisplayName("POST with no signed-in user in the session returns the error view")
    void postWithNoUserInSession() throws Exception {

        givenValidToken();
        when(sessionService.getUserId()).thenReturn(null);

        mockMvc.perform(post(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("token", TOKEN).param("role", ROLE))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verifyNoInteractions(presenterTypesService);
    }

    @Test
    @DisplayName("POST with no role returns the error view")
    void postWithoutRole() throws Exception {

        givenValidToken();
        givenSessionUserId(USER_ID);
        givenPresenterTypes();

        mockMvc.perform(post(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    @DisplayName("POST with a role that is not one of the presenter types returns the error view")
    void postWithUnknownRole() throws Exception {

        givenValidToken();
        givenSessionUserId(USER_ID);
        givenPresenterTypes();

        mockMvc.perform(post(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("token", TOKEN).param("role", "other"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    @DisplayName("POST returns the error view if the new token cannot be created")
    void postWhenTokenCannotBeCreated() throws Exception {

        JWTClaimsSet claims = givenValidToken();
        givenSessionUserId(USER_ID);
        givenPresenterTypes();
        when(tokenService.createTransactionsWebToken(claims, ROLE)).thenReturn(null);

        mockMvc.perform(post(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("token", TOKEN).param("role", ROLE))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    @Test
    @DisplayName("POST returns the error view if the presenter types cannot be retrieved")
    void postServiceException() throws Exception {

        givenValidToken();
        givenSessionUserId(USER_ID);
        when(presenterTypesService.getPresenterTypes(USER_ID, FORM_TYPE))
                .thenThrow(new ServiceException("error"));

        mockMvc.perform(post(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("token", TOKEN).param("role", ROLE))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));
    }

    private JWTClaimsSet givenValidToken() {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .claim("form_type", FORM_TYPE)
                .claim("transaction_id", TRANSACTION_ID)
                .build();
        when(tokenService.verifyChsToken(TOKEN, TRANSACTION_ID)).thenReturn(claims);
        return claims;
    }

    private void givenPresenterTypes() throws ServiceException {
        when(presenterTypesService.getPresenterTypes(USER_ID, FORM_TYPE))
                .thenReturn(List.of(new PresenterType(ROLE, "Description")));
    }

    private void givenSessionUserId(String userId) {
        when(sessionService.getUserId()).thenReturn(userId);
    }
}
