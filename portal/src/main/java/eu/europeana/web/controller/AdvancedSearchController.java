package eu.europeana.web.controller;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * todo: javadoc
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Controller
@RequestMapping("/advancedsearch.html")
public class AdvancedSearchController {
    private Logger log = Logger.getLogger(getClass());

    @RequestMapping(method = RequestMethod.GET)
    public String get(
    ) {
        return "advancedsearch";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String post(
            Form form
    ) {
        log.info(form);
        return "advancedsearch";
    }

    public static class Form {
        private String facet0;
        private String value0;
        private String operator1;
        private String facet1;
        private String value1;
        private String operator2;
        private String facet2;
        private String value2;
        private int creationFrom;
        private int creationTo;
        private int birthFrom;
        private int birthTo;
        private int acquisitionFrom;
        private int acquisitionTo;
        private int purchasePrice;
        private boolean allProvinces;
        private String province;
        private String allCollections;
        private String collection;
        private String sortBy;

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

        @Override
        public String toString() {
            return "Form{" +
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
}
