package uk.gov.companieshouse.transactions.web.transformer.whatisyourrole.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;
import uk.gov.companieshouse.transactions.web.model.whatisyourrole.Role;
import uk.gov.companieshouse.transactions.web.model.whatisyourrole.WhatIsYourRole;

class WhatIsYourRoleTransformerImplTests {

    private final WhatIsYourRoleTransformerImpl transformer = new WhatIsYourRoleTransformerImpl();

    @Test
    @DisplayName("Maps each presenter type to a role, preserving order, id and description")
    void getWhatIsYourRole() {

        WhatIsYourRole result = transformer.getWhatIsYourRole(List.of(
                new PresenterType("id1", "Description 1"),
                new PresenterType("id2", "Description 2")));

        List<Role> roles = result.getRoles();
        assertEquals(2, roles.size());
        assertEquals("id1", roles.get(0).getId());
        assertEquals("Description 1", roles.get(0).getDescription());
        assertEquals("id2", roles.get(1).getId());
        assertEquals("Description 2", roles.get(1).getDescription());
    }

    @Test
    @DisplayName("Maps an empty list of presenter types to an empty list of roles")
    void getWhatIsYourRoleEmpty() {

        assertTrue(transformer.getWhatIsYourRole(List.of()).getRoles().isEmpty());
    }
}
