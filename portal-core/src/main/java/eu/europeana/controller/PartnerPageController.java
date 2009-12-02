package eu.europeana.controller;

import eu.europeana.web.util.PartnerListSampler;
import javax.servlet.http.HttpServletRequest;

import java.util.Map;

/**
 * Get partnerList and providerList from the cached version
 *
 * @author Eric van der Meulen <eric.meulen@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class PartnerPageController extends AbstractPortalController {

    private Map config;

    private PartnerListSampler partnerListSampler;

    public void handle(HttpServletRequest request, Model model) throws Exception {
        model.put("partners", partnerListSampler.getPartnerCache());
        model.put("contributors", partnerListSampler.getContributorCache());
        model.put("partnerPagesSource", config.get("message.network"));
        model.setContentType("text/html; charset=utf-8");
        model.setView("partners");
    }

    public void setPartnerListSampler(PartnerListSampler partnerListSampler) {
        this.partnerListSampler = partnerListSampler;
    }

    public void setConfig(Map config) {
        this.config = config;
    }
}