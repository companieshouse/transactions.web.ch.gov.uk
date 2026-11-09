package uk.gov.companieshouse.transactions.web.service.presentertype.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.transactions.web.exception.ServiceException;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;

class MockPresenterTypesServiceImplTests {

    private final MockPresenterTypesServiceImpl service = new MockPresenterTypesServiceImpl();

    @Test
    @DisplayName("Mock service returns the mocked presenter types")
    void getPresenterTypes() throws ServiceException {

        List<PresenterType> presenterTypes = service.getPresenterTypes("1234", "AD01");

        assertEquals(List.of("officer-employee", "corporate-officer-employee", "judicial-factor",
                        "official-receiver", "individual-filing-for-self",
                        "individual-filing-for-someone-else", "ascp"),
                presenterTypes.stream().map(PresenterType::getId).toList());
    }
}
