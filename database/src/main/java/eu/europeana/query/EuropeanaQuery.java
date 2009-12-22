/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.query;

import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public interface EuropeanaQuery {

    // from QueryExpression

    public interface QueryExpression {
        String getQueryString();

        String getBackendQueryString();

        QueryType getType();

        boolean isMoreLikeThis();

        public enum QueryType {
            SIMPLE_QUERY("europeana"),
            ADVANCED_QUERY("standard"),
            MORE_LIKE_THIS_QUERY("moreLikeThis");

            private String appearance;

            QueryType(String appearance) {
                this.appearance = appearance;
            }

            public String toString() {
                return appearance;
            }
        }
    }



    // from BriefDocWindow

    public interface BriefDocWindow extends PagingWindow {
        List<BriefDoc> getDocs();
    }

    // from DocIdWindow
    public interface DocIdWindow extends eu.europeana.query.PagingWindow {
        List<String> getIds();
    }

    // from Facet

    public interface Facet {
        FacetType getType();

        List<FacetCount> getCounts();
    }

    // from FacetCount
    public interface FacetCount {
        String getValue();

        Integer getCount();
    }

    // pagingWindow

    public interface PagingWindow {
        Integer getOffset();

        Integer getHitCount();
    }


    // from QueryModel

    public interface QueryModel {

        QueryExpression setQueryString(String queryString) throws EuropeanaQueryException;

        void setQueryExpression(QueryExpression queryExpression);

        void setQueryConstraints(Constraints constraints);

        void setResponseType(ResponseType responseType);

        void setStartRow(int startRow);

        int getStartRow();

        void setRows(int rows);

        int getRows();

        ResponseType getResponseType();

        Constraints getConstraints();

        String getQueryString();

        QueryExpression.QueryType getQueryType();

        RecordFieldChoice getRecordFieldChoice();

        eu.europeana.query.ResultModel fetchResult() throws EuropeanaQueryException;

        public interface Constraints {

            public interface Entry {
                FacetType getFacetType();

                String getValue();
            }

            List<FacetType> getFacetTypes();

            List<String> getConstraint(FacetType type);

            List<? extends Entry> getEntries();
        }
    }


    // from QueryModelFactory
    public interface QueryModelFactory {
        enum SearchType {
            SIMPLE,
            ADVANCED
        }

        eu.europeana.query.QueryModel createQueryModel(SearchType searchType);
    }

    // From result Model

    public interface ResultModel {

        Integer getQueryDuration();

        FullDoc getFullDoc();

        BriefDoc getMatchDoc();

        eu.europeana.query.BriefDocWindow getBriefDocWindow();

        DocIdWindow getDocIdWindow();

        List<eu.europeana.query.Facet> getFacets();

        boolean isBadRequest();

        String getErrorMessage();

        boolean isMissingFullDoc();
    }

}