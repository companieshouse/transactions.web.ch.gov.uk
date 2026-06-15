package uk.gov.companieshouse.transactions.web.controller.confirmation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GlobalModelAdviceTests {

    private GlobalModelAdvice advice;

    @BeforeEach
    void setup() {
        advice = new GlobalModelAdvice();
        ReflectionTestUtils.setField(advice, "contactUsUrl", "https://www.gov.uk/help/contact-us");
        ReflectionTestUtils.setField(advice, "feedbackUrl", "https://example.com/feedback");
        ReflectionTestUtils.setField(advice, "chsUrl", "https://www.gov.uk");
        ReflectionTestUtils.setField(advice, "developerUrl", "https://developer.company-information.service.gov.uk");
        ReflectionTestUtils.setField(advice, "accountUrl", "https://account.company-information.service.gov.uk");
        ReflectionTestUtils.setField(advice, "monitorGuiUrl", "https://monitor.company-information.service.gov.uk");
        ReflectionTestUtils.setField(advice, "cdnUrl", "//cdn.ch.gov.uk");
        ReflectionTestUtils.setField(advice, "enquiries", "enquiries@companieshouse.gov.uk");
        ReflectionTestUtils.setField(advice, "policiesUrl", "https://resources.companieshouse.gov.uk/serviceInformation.shtml");
        ReflectionTestUtils.setField(advice, "piwikUrl", "https://analytics.example.com");
        ReflectionTestUtils.setField(advice, "piwikSiteId", "42");
    }

    @Test
    @DisplayName("Global links include configured and derived values")
    void getGlobalLinksIncludesConfiguredAndDerivedValues() {
        Map<String, String> links = advice.getGlobalLinks();

        assertEquals("https://example.com/feedback", links.get("feedbackUrl"));
        assertEquals("https://www.gov.uk/help/contact-us", links.get("contactUsUrl"));
        assertEquals("https://resources.companieshouse.gov.uk/serviceInformation.shtml", links.get("policiesUrl"));
        assertEquals("https://www.gov.uk/help/cookies", links.get("cookiesUrl"));
        assertEquals("https://developer.company-information.service.gov.uk", links.get("developerUrl"));
        assertEquals("https://account.company-information.service.gov.uk/user/account", links.get("chsAccountUrl"));
        assertEquals("https://monitor.company-information.service.gov.uk/admin/monitor", links.get("monitorGuiUrl"));
        assertEquals("//cdn.ch.gov.uk", links.get("cdnUrl"));
        assertEquals("mailto:enquiries@companieshouse.gov.uk", links.get("enquiries"));
    }

    @Test
    @DisplayName("Global links enquiries value is always prefixed with mailto")
    void getGlobalLinksAlwaysPrefixesEnquiriesWithMailto() {
        ReflectionTestUtils.setField(advice, "enquiries", "mailto:enquiries@companieshouse.gov.uk");

        Map<String, String> links = advice.getGlobalLinks();

        assertEquals("mailto:mailto:enquiries@companieshouse.gov.uk", links.get("enquiries"));
    }

    @Test
    @DisplayName("Global links creation fails when any configured value is null")
    void getGlobalLinksThrowsWhenAnyConfiguredValueIsNull() {
        ReflectionTestUtils.setField(advice, "feedbackUrl", null);

        assertThrows(NullPointerException.class, () -> advice.getGlobalLinks());
    }

    @Test
    @DisplayName("Piwik properties include configured url and site id")
    void getPiwikPropertiesIncludesConfiguredValues() {
        Map<String, String> piwikProperties = advice.getPiwikProperties();

        assertEquals("https://analytics.example.com", piwikProperties.get("url"));
        assertEquals("42", piwikProperties.get("siteId"));
    }

    @Test
    @DisplayName("Piwik properties creation fails when url is null")
    void getPiwikPropertiesThrowsWhenUrlIsNull() {
        ReflectionTestUtils.setField(advice, "piwikUrl", null);

        assertThrows(NullPointerException.class, () -> advice.getPiwikProperties());
    }
}

