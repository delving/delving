package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Mirror the contributor object
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class ContributorX implements IsSerializable {
    private Long id;
    private CountryX country;
    private String providerId;
    private String originalName;
    private String englishName;
    private String acronym;
    private String numberOfPartners;
    private String url;

    public ContributorX(Long id, CountryX country, String providerId, String originalName, String englishName, String acronym, String numberOfPartners, String url) {
        this.id = id;
        this.country = country;
        this.providerId = providerId;
        this.originalName = originalName;
        this.englishName = englishName;
        this.acronym = acronym;
        this.numberOfPartners = numberOfPartners;
        this.url = url;
    }

    public ContributorX() {
    }

    public Long getId() {
        return id;
    }

    public void setCountry(CountryX country) {
        this.country = country;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public void setEnglishName(String englishName) {
        this.englishName = englishName;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public CountryX getCountry() {
        return country;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getEnglishName() {
        return englishName;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setNumberOfPartners(String numberOfPartners) {
        this.numberOfPartners = numberOfPartners;
    }

    public String getNumberOfPartners() {
        return numberOfPartners;
    }

    public String getUrl() {
        return url;
    }
}
