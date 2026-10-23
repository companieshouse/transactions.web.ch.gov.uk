package uk.gov.companieshouse.transactions.web.transformer.whatisyourrole.impl;

import java.util.List;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.transactions.web.model.presentertype.PresenterType;
import uk.gov.companieshouse.transactions.web.model.whatisyourrole.Role;
import uk.gov.companieshouse.transactions.web.model.whatisyourrole.WhatIsYourRole;
import uk.gov.companieshouse.transactions.web.transformer.whatisyourrole.WhatIsYourRoleTransformer;

@Component
public class WhatIsYourRoleTransformerImpl implements WhatIsYourRoleTransformer {

    /**
     * {@inheritDoc}
     */
    @Override
    public WhatIsYourRole getWhatIsYourRole(List<PresenterType> presenterTypes) {

        WhatIsYourRole whatIsYourRole = new WhatIsYourRole();

        whatIsYourRole.setRoles(presenterTypes.stream()
                .map(p -> new Role(p.getId(), p.getDescription()))
                .toList());

        return whatIsYourRole;
    }
}
