package uk.gov.companieshouse.transactions.web.service.whatisyourrole.impl;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;
import uk.gov.companieshouse.transactions.web.model.whatisyourrole.WhatIsYourRole;
import uk.gov.companieshouse.transactions.web.transformer.whatisyourrole.WhatIsYourRoleTransformer;

@ExtendWith(MockitoExtension.class)
class WhatIsYourRoleServiceImplTests {

    @Mock
    private WhatIsYourRoleTransformer transformer;

    @InjectMocks
    private WhatIsYourRoleServiceImpl service;

    @Test
    @DisplayName("Delegates to the transformer and returns its result")
    void getWhatIsYourRole() {

        List<PresenterType> presenterTypes = List.of(new PresenterType("id", "Description"));
        WhatIsYourRole expected = new WhatIsYourRole();
        when(transformer.getWhatIsYourRole(presenterTypes)).thenReturn(expected);

        assertSame(expected, service.getWhatIsYourRole(presenterTypes));
    }
}
