package uk.gov.companieshouse.transactions.web.service.presentertype.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.presenters.PrivatePresentersResourceHandler;
import uk.gov.companieshouse.api.handler.presenters.request.PrivatePresenterTypesGet;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.presenters.model.PresenterTypes;
import uk.gov.companieshouse.transactions.web.api.ApiClientService;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;

@ExtendWith(MockitoExtension.class)
class PresenterTypesServiceImplTests {

    private static final String USER_ID = "1234";
    private static final String FORM_TYPE = "AD01";

    @Mock
    private ApiClientService apiClientService;

    @Mock
    private InternalApiClient internalApiClient;

    @Mock
    private PrivatePresentersResourceHandler presentersResourceHandler;

    @Mock
    private PrivatePresenterTypesGet presenterTypesGet;

    @Mock
    private ApiResponse<PresenterTypes> apiResponse;

    @InjectMocks
    private PresenterTypesServiceImpl service;

    @BeforeEach
    void setUp() {
        when(apiClientService.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privatePresentersResourceHandler()).thenReturn(presentersResourceHandler);
        when(presentersResourceHandler.getPresenterTypes(FORM_TYPE)).thenReturn(presenterTypesGet);
    }

    @Test
    @DisplayName("Presenter types from the API are returned with their descriptions")
    void getPresenterTypes() throws Exception {
        when(presenterTypesGet.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(new PresenterTypes(List.of("officer-employee", "ascp")));

        List<PresenterType> result = service.getPresenterTypes(USER_ID, FORM_TYPE);

        assertEquals(List.of("officer-employee", "ascp"), result.stream().map(PresenterType::getId).toList());
        assertEquals(List.of("Officer employee", "ASCP sole trader"),
                result.stream().map(PresenterType::getDescription).toList());
    }

    @Test
    @DisplayName("An unknown presenter type uses its id as the description")
    void getPresenterTypesUnknownType() throws Exception {
        when(presenterTypesGet.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(new PresenterTypes(List.of("new-type")));

        List<PresenterType> result = service.getPresenterTypes(USER_ID, FORM_TYPE);

        assertEquals("new-type", result.get(0).getDescription());
    }

    @Test
    @DisplayName("A ServiceException is thrown when the API returns no data")
    void getPresenterTypesNoData() throws Exception {
        when(presenterTypesGet.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(null);

        assertThrows(ServiceException.class, () -> service.getPresenterTypes(USER_ID, FORM_TYPE));
    }

    @Test
    @DisplayName("A ServiceException is thrown when the API call fails")
    void getPresenterTypesApiError() throws Exception {
        when(presenterTypesGet.execute()).thenThrow(ApiErrorResponseException.class);

        assertThrows(ServiceException.class, () -> service.getPresenterTypes(USER_ID, FORM_TYPE));
    }

    @Test
    @DisplayName("A ServiceException is thrown when the URI is invalid")
    void getPresenterTypesInvalidUri() throws Exception {
        when(presenterTypesGet.execute()).thenThrow(URIValidationException.class);

        assertThrows(ServiceException.class, () -> service.getPresenterTypes(USER_ID, FORM_TYPE));
    }
}
