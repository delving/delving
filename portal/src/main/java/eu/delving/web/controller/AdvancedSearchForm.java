package eu.delving.web.controller;

import org.apache.solr.client.solrj.SolrQuery;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Sep 30, 2010 2:23:50 PM
 */

public class AdvancedSearchForm {
    private String facet0 = "";
    private String value0 = "";
    private String operator1= "";
    private String facet1= "";
    private String value1= "";
    private String operator2 = "";
    private String facet2= "";
    private String value2= "";
    private int creationFrom = 0;
    private int creationTo = 0;
    private int birthFrom = 0;
    private int birthTo = 0;
    private int acquisitionFrom = 0;
    private int acquisitionTo = 0;
    private int purchasePrice = 0;
    private boolean allProvinces = true;
    private String province = "";
    private String allCollections = "";
    private String collection = "";
    private String sortBy = "";

    public String getFacet0() {
        return facet0;
    }

    public void setFacet0(String facet0) {
        this.facet0 = facet0;
    }

    public String getValue0() {
        return value0;
    }

    public void setValue0(String value0) {
        this.value0 = value0;
    }

    public String getOperator1() {
        return operator1;
    }

    public void setOperator1(String operator1) {
        this.operator1 = operator1;
    }

    public String getFacet1() {
        return facet1;
    }

    public void setFacet1(String facet1) {
        this.facet1 = facet1;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public String getOperator2() {
        return operator2;
    }

    public void setOperator2(String operator2) {
        this.operator2 = operator2;
    }

    public String getFacet2() {
        return facet2;
    }

    public void setFacet2(String facet2) {
        this.facet2 = facet2;
    }

    public String getValue2() {
        return value2;
    }

    public void setValue2(String value2) {
        this.value2 = value2;
    }

    public int getCreationFrom() {
        return creationFrom;
    }

    public void setCreationFrom(int creationFrom) {
        this.creationFrom = creationFrom;
    }

    public int getCreationTo() {
        return creationTo;
    }

    public void setCreationTo(int creationTo) {
        this.creationTo = creationTo;
    }

    public int getBirthFrom() {
        return birthFrom;
    }

    public void setBirthFrom(int birthFrom) {
        this.birthFrom = birthFrom;
    }

    public int getBirthTo() {
        return birthTo;
    }

    public void setBirthTo(int birthTo) {
        this.birthTo = birthTo;
    }

    public int getAcquisitionFrom() {
        return acquisitionFrom;
    }

    public void setAcquisitionFrom(int acquisitionFrom) {
        this.acquisitionFrom = acquisitionFrom;
    }

    public int getAcquisitionTo() {
        return acquisitionTo;
    }

    public void setAcquisitionTo(int acquisitionTo) {
        this.acquisitionTo = acquisitionTo;
    }

    public int getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(int purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public boolean isAllProvinces() {
        return allProvinces;
    }

    public void setAllProvinces(boolean allProvinces) {
        this.allProvinces = allProvinces;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getAllCollections() {
        return allCollections;
    }

    public void setAllCollections(String allCollections) {
        this.allCollections = allCollections;
    }

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public SolrQuery toSolrQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append(makeQueryString(value0, facet0, operator1));
        builder.append(makeQueryString(value1, facet1, operator2));
        builder.append(makeQueryString(value2, facet2, ""));
        SolrQuery query = new SolrQuery(builder.toString());
        return query;
    }

    private String makeQueryString(String value, String facet, String operator) {
        StringBuilder builder = new StringBuilder();
        if (isValid(value)) {
            if (isValid(facet)) {
                builder.append(facet);
            } else {
                builder.append("text");
            }
            builder.append(":").append(value);
            if (isValid(operator)) {
                builder.append(operator);
            }
        }
        return builder.toString();
    }

    private boolean isValid(String field) {
        boolean valid = false;
        if (field != null && !field.isEmpty()) {
            valid = true;
        }
        return valid;
    }

    @Override
    public String toString() {
        return "eu.delving.web.controller.AdvancedSearchForm{" +
                "facet0='" + facet0 + '\'' +
                ", value0='" + value0 + '\'' +
                ", operator1='" + operator1 + '\'' +
                ", facet1='" + facet1 + '\'' +
                ", value1='" + value1 + '\'' +
                ", operator2='" + operator2 + '\'' +
                ", facet2='" + facet2 + '\'' +
                ", value2='" + value2 + '\'' +
                ", creationFrom=" + creationFrom +
                ", creationTo=" + creationTo +
                ", birthFrom=" + birthFrom +
                ", birthTo=" + birthTo +
                ", acquisitionFrom=" + acquisitionFrom +
                ", acquisitionTo=" + acquisitionTo +
                ", purchasePrice=" + purchasePrice +
                ", allProvinces=" + allProvinces +
                ", province='" + province + '\'' +
                ", allCollections='" + allCollections + '\'' +
                ", collection='" + collection + '\'' +
                ", sortBy='" + sortBy + '\'' +
                '}';
    }
}
