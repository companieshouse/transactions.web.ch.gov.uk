package uk.gov.companieshouse.transactions.web.controller.whatisyourrole;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static uk.gov.companieshouse.transactions.web.utils.constants.UrlConstants.NEXT_PAGE_PATH;
import static uk.gov.companieshouse.transactions.web.utils.constants.UrlConstants.WHAT_IS_YOUR_ROLE_PATH;

import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import uk.gov.companieshouse.transactions.web.session.SessionService;

@ExtendWith(MockitoExtension.class)
class WhatIsYourRoleControllerTests {

    private static final String USER_ID = "1234";

    private static final String FORM_TYPE = "AD01";

    private static final String TRANSACTION_ID = "123456-654321-123456";

    private static final String TOKEN = new PlainJWT(claims()).serialize();

    private MockMvc mockMvc;

    @Mock
    private PresenterTypesService presenterTypesService;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private WhatIsYourRoleController controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET returns the what is your role view with the model")
    void getWhatIsYourRole() throws Exception {

        givenSessionUserId(USER_ID);
        List<PresenterType> presenterTypes = List.of(new PresenterType("id", "Description"));
        when(presenterTypesService.getPresenterTypes(USER_ID, FORM_TYPE)).thenReturn(presenterTypes);

        MvcResult result = mockMvc.perform(get(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("whatIsYourRole"))
                .andExpect(model().attribute("token", TOKEN))
                .andExpect(model().attribute("formType", FORM_TYPE))
                .andExpect(model().attribute("transactionId", TRANSACTION_ID))
                .andExpect(model().attribute("nextPagePath", NEXT_PAGE_PATH))
                .andExpect(model().attribute("templateName", "whatIsYourRole"))
                .andReturn();

        WhatIsYourRole whatIsYourRole =
                (WhatIsYourRole) result.getModelAndView().getModel().get("whatIsYourRole");
        assertEquals(1, whatIsYourRole.getRoles().size());
        assertEquals("id", whatIsYourRole.getRoles().get(0).getId());
        assertEquals("Description", whatIsYourRole.getRoles().get(0).getDescription());
    }

    @Test
    @DisplayName("GET returns the error view if the presenter types cannot be retrieved")
    void getWhatIsYourRoleServiceException() throws Exception {

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
    @DisplayName("GET with an unreadable token returns the error view")
    void getWithUnreadableToken() throws Exception {

        mockMvc.perform(get(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("token", "not-a-jwt"))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verifyNoInteractions(presenterTypesService);
    }

    @Test
    @DisplayName("GET with no signed-in user in the session returns the error view")
    void getWithNoUserInSession() throws Exception {

        when(sessionService.getSessionDataFromContext()).thenReturn(new HashMap<>());

        mockMvc.perform(get(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verifyNoInteractions(presenterTypesService);
    }

    @Test
    @DisplayName("GET with malformed session data returns the error view")
    void getWithMalformedSession() throws Exception {

        Map<String, Object> sessionData = new HashMap<>();
        sessionData.put("signin_info", "not-a-map");
        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);

        mockMvc.perform(get(WHAT_IS_YOUR_ROLE_PATH, TRANSACTION_ID).param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verifyNoInteractions(presenterTypesService);
    }

    @Test
    @DisplayName("GET with a blank transaction id returns the error view")
    void getWithBlankTransactionId() throws Exception {

        givenSessionUserId(USER_ID);

        mockMvc.perform(get(WHAT_IS_YOUR_ROLE_PATH, " ").param("token", TOKEN))
                .andExpect(status().isOk())
                .andExpect(view().name("error"));

        verifyNoInteractions(presenterTypesService);
    }

    private void givenSessionUserId(String userId) {
        Map<String, Object> sessionData = Map.of("signin_info",
                Map.of("user_profile", Map.of("id", userId)));
        when(sessionService.getSessionDataFromContext()).thenReturn(sessionData);
    }

    private static JWTClaimsSet claims() {
        return new JWTClaimsSet.Builder().claim("form-type", FORM_TYPE).build();
    }
}
