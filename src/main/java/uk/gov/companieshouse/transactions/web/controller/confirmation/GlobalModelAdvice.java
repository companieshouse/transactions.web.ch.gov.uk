package uk.gov.companieshouse.transactions.web.controller.confirmation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Map;

@ControllerAdvice
public class GlobalModelAdvice {

    @Value("${contactUs.url}")
    private String contactUsUrl;

    @Value("${feedback.url}")
    private String feedbackUrl;

    @Value("${chs.url}")
    private String chsUrl;

    @Value("${developer.url}")
    private String developerUrl;

    @Value("${account.url}")
    private String accountUrl;

    @Value("${monitorGui.url}")
    private String monitorGuiUrl;

    @Value("${cdn.url}")
    private String cdnUrl;

    @Value("${enquiries}")
    private String enquiries;

    @Value("${policies.url}")
    private String policiesUrl;

    @Value("${piwik.url}")
    private String piwikUrl;

    @Value("${piwik.siteId}")
    private String piwikSiteId;

    @ModelAttribute("globalLinks")
    public Map<String, String> getGlobalLinks() {
        return Map.of(
                "feedbackUrl", feedbackUrl,
                "contactUsUrl", contactUsUrl,
                "policiesUrl", policiesUrl,
                "cookiesUrl", chsUrl + "/help/cookies",
                "developerUrl", developerUrl,
                "chsAccountUrl", accountUrl + "/user/account",
                "monitorGuiUrl", monitorGuiUrl + "/admin/monitor",
                "cdnUrl", cdnUrl,
                "enquiries", "mailto:" + enquiries
        );
    }

    @ModelAttribute("piwik")
    public Map<String, String> getPiwikProperties() {
        return Map.of(
                "url", piwikUrl,
                "siteId", piwikSiteId
        );
    }

}
