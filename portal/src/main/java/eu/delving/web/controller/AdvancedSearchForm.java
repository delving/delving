package eu.delving.web.controller;

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
    private String creationFrom = "";
    private String creationTo = "";
    private String birthFrom = "";
    private String birthTo = "";
    private String acquisitionFrom = "";
    private String acquisitionTo = "";
    private String purchasePrice = "";
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

    public String getCreationFrom() {
        return creationFrom;
    }

    public void setCreationFrom(String creationFrom) {
        this.creationFrom = creationFrom;
    }

    public String getCreationTo() {
        return creationTo;
    }

    public void setCreationTo(String creationTo) {
        this.creationTo = creationTo;
    }

    public String getBirthFrom() {
        return birthFrom;
    }

    public void setBirthFrom(String birthFrom) {
        this.birthFrom = birthFrom;
    }

    public String getBirthTo() {
        return birthTo;
    }

    public void setBirthTo(String birthTo) {
        this.birthTo = birthTo;
    }

    public String getAcquisitionFrom() {
        return acquisitionFrom;
    }

    public void setAcquisitionFrom(String acquisitionFrom) {
        this.acquisitionFrom = acquisitionFrom;
    }

    public String getAcquisitionTo() {
        return acquisitionTo;
    }

    public void setAcquisitionTo(String acquisitionTo) {
        this.acquisitionTo = acquisitionTo;
    }

    public String getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(String purchasePrice) {
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

    public String toSolrQuery() {
        StringBuilder builder = new StringBuilder();
        builder.append(makeQueryString(value0, facet0, operator1, value1));
        builder.append(makeQueryString(value1, facet1, operator2, value2));
        builder.append(makeQueryString(value2, facet2, "", ""));
        if (isValid(collection) && !collection.equalsIgnoreCase("all_collections")) {
            builder.append("&qf=COLLECTION:").append(collection).append("");
        }
        if (isValid(getSortBy())) {
            builder.append("&sortBy=").append(getSortBy());
        }
        return builder.toString().trim();
    }

    private String makeQueryString(String value, String facet, String operator, String nextValue) {
        StringBuilder builder = new StringBuilder();
        if (isValid(value)) {
            if (isValid(facet)) {
                builder.append(facet);
            } else {
                builder.append("text");
            }
            builder.append(":").append(value);
            if (isValid(operator) && isValid(nextValue)) {
                if (!operator.equalsIgnoreCase("and")) {
                    builder.append(" ").append(operator).append(" ");
                } else {
                    builder.append(" ");
                }
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
        return "AdvancedSearchForm={" +
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
