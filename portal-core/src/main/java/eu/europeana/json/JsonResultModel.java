/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
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

package eu.europeana.json;

import eu.europeana.query.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * This class consumes some JSON and produces a model for the use of FreeMarker templates.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class JsonResultModel implements ResultModel {

    private Integer queryDuration;
    private FullDoc fullDoc;
    private BriefDoc matchDoc;
    private BriefDocWindow briefDocWindow;
    private DocIdWindow docIdWindow;
    private List<Facet> facets = new ArrayList<Facet>();
    private boolean badRequest = false;
    private boolean missingFullDoc = false;
    private String errorMessage;

    private static final String BAD_REQUEST_RESULT =
            "{\n" +
            "  \"responseHeader\":{\n" +
            "    \"status\":400,\n" + // No idea if this is a valide status. It's meant to remind of 400 HTTP status code
            "    \"QTime\":0,\n" +
            "    \"params\":{\n" +
            "      \"wt\":\"json\"}},\n" +
            "  \"response\":{\"numFound\":0,\"start\":0,\"docs\":[]\n" +
            " }}";

    public JsonResultModel(String jsonString, ResponseType responseType) throws JSONException {
        this(jsonString, responseType, false, null);
    }
    public JsonResultModel(String jsonString, ResponseType responseType, boolean badRequest, String errorMessage) throws JSONException {
        if (jsonString == null) {
            jsonString = BAD_REQUEST_RESULT;
        }
        this.badRequest = badRequest;
        this.errorMessage = errorMessage;
        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject responseHeader = jsonObject.getJSONObject("responseHeader");
        this.queryDuration = JsonUtil.getInteger(responseHeader, "QTime");
        JSONObject match = null;
        if (jsonObject.has("match")) {
            match = jsonObject.getJSONObject("match");
            if (match.getJSONArray("docs").length() == 0) {
                jsonObject = new JSONObject(BAD_REQUEST_RESULT);
                match = null;
                missingFullDoc = true;
            }
        }
        JSONObject response = jsonObject.getJSONObject("response");
        Integer hitCount = JsonUtil.getInteger(response, "numFound");
//        todo: add rows to model. check if change in hitcount from rows to numFound
//        JSONObject params = responseHeader.getJSONObject("params");
//        Integer rows = new Integer(JsonUtil.getString(params, "rows"));
        Integer offset = JsonUtil.getInteger(response, "start") + 1; // solr starts record count at zero
        JSONArray docsArray = response.getJSONArray("docs");
        switch (responseType) {
            case SINGLE_FULL_DOC:
                if (match != null) {
                    fullDoc = new FullDocImpl(match.getJSONArray("docs").getJSONObject(0));
                    briefDocWindow = new BriefDocWindowImpl(hitCount, offset);
                    for (int walk = 0; walk < docsArray.length(); walk++) {
                        BriefDocImpl briefDocImpl = new BriefDocImpl((JSONObject) docsArray.get(walk), offset + walk);
                        briefDocWindow.getDocs().add(briefDocImpl);
                    }
                }
                else if (docsArray.length() > 0) {
                    JSONObject docObject = (JSONObject) docsArray.get(0);
                    fullDoc = new FullDocImpl(docObject);
                }
                break;
            case FACETS_ONLY:
                break;
            case SMALL_BRIEF_DOC_WINDOW:
            case LARGE_BRIEF_DOC_WINDOW:
                if (match != null) {
                    matchDoc = new BriefDocImpl((JSONObject) match.getJSONArray("docs").get(0), offset);
                }
                briefDocWindow = new BriefDocWindowImpl(hitCount, offset);
                List<BriefDocImpl> docs = new ArrayList<BriefDocImpl>();
                for (int walk = 0; walk < docsArray.length(); walk++) {
                    BriefDocImpl briefDocImpl = new BriefDocImpl((JSONObject) docsArray.get(walk), offset + walk);
                    docs.add(briefDocImpl);
                    briefDocWindow.getDocs().add(briefDocImpl);
                }
                if (jsonObject.has(QueryExpression.Type.MORE_LIKE_THIS_QUERY.toString())) {
                    JSONObject moreLikeThis = (JSONObject) jsonObject.get(QueryExpression.Type.MORE_LIKE_THIS_QUERY.toString());
                    for (BriefDocImpl doc : docs) {
                        if (moreLikeThis.has(doc.id)) {
                            response = moreLikeThis.getJSONObject(doc.id);
                            hitCount = JsonUtil.getInteger(response, "numFound");
                            offset = JsonUtil.getInteger(response, "start") + 1; // solr starts record count at zero
                            docsArray = response.getJSONArray("docs");
                            doc.moreLikeThisDocWindow = new BriefDocWindowImpl(hitCount, offset);
                            for (int walk = 0; walk < docsArray.length(); walk++) {
                                BriefDocImpl briefDocImpl = new BriefDocImpl((JSONObject) docsArray.get(walk), offset + walk);
                                doc.moreLikeThisDocWindow.getDocs().add(briefDocImpl);
                            }
                        }
                    }
                }
                break;
            case DOC_ID_WINDOW:
                docIdWindow = new DocIdWindowImpl(hitCount, offset);
                for (int walk = 0; walk < docsArray.length(); walk++) {
                    JSONObject doc = (JSONObject) docsArray.get(walk);
                    String id = (String) doc.get(RecordField.EUROPEANA_URI.toFieldNameString());
                    docIdWindow.getIds().add(id);
                }
                break;
            default:
                throw new RuntimeException("Unhandled response type!");
        }
        if (jsonObject.has("facet_counts")) {
            JSONObject facetCounts = jsonObject.getJSONObject("facet_counts");
            JSONObject facetFields = (JSONObject) facetCounts.get("facet_fields");
            JSONArray names = facetFields.names();
            for (int walk = 0; walk < names.length(); walk++) {
                String name = names.get(walk).toString();
                FacetType facetType = FacetType.valueOf(name);
                FacetImpl facet = new FacetImpl(facetType);
                JSONArray facetFieldsArray = (JSONArray) facetFields.get(name);
                for (int gather = 0; gather < facetFieldsArray.length(); gather += 2) {
                    String value = facetFieldsArray.get(gather).toString();
                    String count = facetFieldsArray.get(gather + 1).toString();
                    facet.getCounts().add(new FacetCountImpl(value, new Integer(count)));
                }
                facets.add(facet);
            }
        }
    }

    public String toString() {
        IndentWriter writer = new IndentWriter();
        writer.print("ResultModel", this, 0);
        return writer.toString();
    }

    public Integer getQueryDuration() {
        return queryDuration;
    }

    public FullDoc getFullDoc() {
        return fullDoc;
    }

    public BriefDoc getMatchDoc() {
        return matchDoc;
    }

    public BriefDocWindow getBriefDocWindow() {
        return briefDocWindow;
    }

    public DocIdWindow getDocIdWindow() {
        return docIdWindow;
    }

    public List<Facet> getFacets() {
        return facets;
    }

    public boolean isBadRequest() {
        return badRequest;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isMissingFullDoc() {
        return missingFullDoc;
    }

    private static class FullDocImpl implements FullDoc {
        // europeana elements
        private String id;
        private String[] thumbnail;
        private String[] europeanaIsShownAt;
        private String[] europeanaIsShownBy;
        private String[] europeanaUserTag;
        private Boolean europeanaHasObject;
        private String[] europeanaCountry;
        private DocType europeanaType;
        private String[] europeanaLanguage;
        private String[] europeanaYear;
        private String[] europeanaSource;
        private String[] europeanaProvider;
        private String[] europeanaCollectionName;

        // here the dcterms namespaces starts
        private String[] dcTermsAlternative;
        private String[] dcTermsConformsTo;
        private String[] dcTermsCreated;
        private String[] dcTermsExtent;
        private String[] dcTermsHasFormat;
        private String[] dcTermsHasPart;
        private String[] dcTermsHasVersion;
        private String[] dcTermsIsFormatOf;
        private String[] dcTermsIsPartOf;
        private String[] dcTermsIsReferencedBy;
        private String[] dcTermsIsReplacedBy;
        private String[] dcTermsIsRequiredBy;
        private String[] dcTermsIssued;
        private String[] dcTermsIsVersionOf;
        private String[] dcTermsMedium;
        private String[] dcTermsProvenance;
        private String[] dcTermsReferences;
        private String[] dcTermsReplaces;
        private String[] dcTermsRequires;
        private String[] dcTermsSpatial;
        private String[] dcTermsTableOfContents;
        private String[] dcTermsTemporal;
        // here the dc namespace starts
        private String[] dcContributor;
        private String[] dcCoverage;
        private String[] dcCreator;
        private String[] dcDate;
        private String[] dcDescription;
        private String[] dcFormat;
        private String[] dcIdentifier;
        private String[] dcLanguage;
        private String[] dcPublisher;
        private String[] dcRelation;
        private String[] dcRights;
        private String[] dcSource;
        private String[] dcSubject;
        private String[] dcTitle;
        private String[] dcType;



        public FullDocImpl(JSONObject jsonObject) throws JSONException {
            id = JsonUtil.getString(jsonObject, RecordField.EUROPEANA_URI.toFieldNameString());
            thumbnail = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.EUROPEANA_OBJECT.toFieldNameString());
            europeanaType = DocType.get(JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, FacetType.TYPE.toString()));
            europeanaYear = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.DATE_DEFAULT, false, FacetType.YEAR.toString());
            europeanaLanguage =  JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, FacetType.LANGUAGE.toString());
            europeanaIsShownBy = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.EUROPEANA_IS_SHOWN_BY.toFieldNameString());
            europeanaIsShownAt = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.EUROPEANA_IS_SHOWN_AT.toFieldNameString());
            europeanaHasObject = JsonUtil.getBoolean(jsonObject, JsonUtil.Default.FALSE, RecordField.EUROPEANA_HAS_OBJECT.toFieldNameString());
            europeanaUserTag = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.EUROPEANA_USER_TAG.toFieldNameString());
            europeanaProvider = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, FacetType.PROVIDER.toString());
            europeanaCountry = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, FacetType.COUNTRY.toString());
            europeanaSource = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.EUROPEANA_SOURCE.toFieldNameString());
            europeanaCollectionName = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.EUROPEANA_COLLECTION_NAME.toFieldNameString());
            // here the dcterms namespaces starts
            dcTermsAlternative = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_ALTERNATIVE.toFieldNameString());
            dcTermsConformsTo = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_CONFORMS_TO.toFieldNameString());
            dcTermsCreated = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.DATE_DEFAULT, false, RecordField.DCTERMS_CREATED.toFieldNameString());
            dcTermsExtent = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_EXTENT.toFieldNameString());
            dcTermsHasFormat = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_HAS_FORMAT.toFieldNameString());
            dcTermsHasPart = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_HAS_PART.toFieldNameString());
            dcTermsHasVersion = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_HAS_VERSION.toFieldNameString());
            dcTermsIsFormatOf = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_IS_FORMAT_OF.toFieldNameString());
            dcTermsIsPartOf = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_IS_PART_OF.toFieldNameString());
            dcTermsIsReferencedBy = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_IS_REFERENCED_BY.toFieldNameString());
            dcTermsIsReplacedBy = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_IS_REPLACED_BY.toFieldNameString());
            dcTermsIsRequiredBy = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_IS_REQUIRED_BY.toFieldNameString());
            dcTermsIssued = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.DATE_DEFAULT, false, RecordField.DCTERMS_ISSUED.toFieldNameString());
            dcTermsIsVersionOf = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_IS_VERSION_OF.toFieldNameString());
            dcTermsMedium = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_MEDIUM.toFieldNameString());
            dcTermsProvenance = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_PROVENANCE.toFieldNameString());
            dcTermsReferences = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_REFERENCES.toFieldNameString());
            dcTermsReplaces = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_REPLACES.toFieldNameString());
            dcTermsRequires = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_REQUIRES.toFieldNameString());
            dcTermsSpatial = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_SPATIAL.toFieldNameString());
            dcTermsTableOfContents = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_TABLE_OF_CONTENTS.toFieldNameString());
            dcTermsTemporal = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DCTERMS_TEMPORAL.toFieldNameString());
            // here the dc namespace starts
            dcContributor = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DC_CONTRIBUTOR.toFieldNameString());
            dcCoverage = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DC_COVERAGE.toFieldNameString());
            dcCreator = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DC_CREATOR.toFieldNameString());
            dcDate = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.DATE_DEFAULT, false, RecordField.DC_DATE.toFieldNameString());
            dcDescription = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DC_DESCRIPTION.toFieldNameString());
            dcFormat = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DC_FORMAT.toFieldNameString());
            dcIdentifier = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DC_IDENTIFIER.toFieldNameString());
            dcLanguage = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DC_LANGUAGE.toFieldNameString());
            dcPublisher = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DC_PUBLISHER.toFieldNameString());
            dcRelation = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DC_RELATION.toFieldNameString());
            dcRights = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DC_RIGHTS.toFieldNameString());
            dcSource = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DC_SOURCE.toFieldNameString());
            dcSubject = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DC_SUBJECT.toFieldNameString());
            dcTitle = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DC_TITLE.toFieldNameString());
            dcType = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.DC_TYPE.toFieldNameString());
        }


        public String getId() {
            return id;
        }

        public String[] getThumbnail() {
            return thumbnail;
        }

        public String[] getEuropeanaIsShownAt() {
            return europeanaIsShownAt;
        }

        public String[] getEuropeanaIsShownBy() {
            return europeanaIsShownBy;
        }

        public String[] getEuropeanaUserTag() {
            return europeanaUserTag;
        }

        public Boolean getEuropeanaHasObject() {
            return europeanaHasObject;
        }

        public String[] getEuropeanaCountry() {
            return europeanaCountry;
        }

        public String[] getEuropeanaProvider() {
            return europeanaProvider;
        }

        public String[] getEuropeanaCollectionName() {
            return europeanaCollectionName;
        }

        public String[] getEuropeanaSource() {
            return europeanaSource;
        }

        public DocType getEuropeanaType() {
            return europeanaType;
        }

        public String[] getEuropeanaLanguage() {
            return europeanaLanguage;
        }

        public String[] getEuropeanaYear() {
            return europeanaYear;
        }

        public String[] getDcTermsAlternative() {
            return dcTermsAlternative;
        }

        public String[] getDcTermsConformsTo() {
            return dcTermsConformsTo;
        }

        public String[] getDcTermsCreated() {
            return dcTermsCreated;
        }

        public String[] getDcTermsExtent() {
            return dcTermsExtent;
        }

        public String[] getDcTermsHasFormat() {
            return dcTermsHasFormat;
        }

        public String[] getDcTermsHasPart() {
            return dcTermsHasPart;
        }

        public String[] getDcTermsHasVersion() {
            return dcTermsHasVersion;
        }

        public String[] getDcTermsIsFormatOf() {
            return dcTermsIsFormatOf;
        }

        public String[] getDcTermsIsPartOf() {
            return dcTermsIsPartOf;
        }

        public String[] getDcTermsIsReferencedBy() {
            return dcTermsIsReferencedBy;
        }

        public String[] getDcTermsIsReplacedBy() {
            return dcTermsIsReplacedBy;
        }

        public String[] getDcTermsIsRequiredBy() {
            return dcTermsIsRequiredBy;
        }

        public String[] getDcTermsIssued() {
            return dcTermsIssued;
        }

        public String[] getDcTermsIsVersionOf() {
            return dcTermsIsVersionOf;
        }

        public String[] getDcTermsMedium() {
            return dcTermsMedium;
        }

        public String[] getDcTermsProvenance() {
            return dcTermsProvenance;
        }

        public String[] getDcTermsReferences() {
            return dcTermsReferences;
        }

        public String[] getDcTermsReplaces() {
            return dcTermsReplaces;
        }

        public String[] getDcTermsRequires() {
            return dcTermsRequires;
        }

        public String[] getDcTermsSpatial() {
            return dcTermsSpatial;
        }

        public String[] getDcTermsTableOfContents() {
            return dcTermsTableOfContents;
        }

        public String[] getDcTermsTemporal() {
            return dcTermsTemporal;
        }

        public String[] getDcContributor() {
            return dcContributor;
        }

        public String[] getDcCoverage() {
            return dcCoverage;
        }

        public String[] getDcCreator() {
            return dcCreator;
        }

        public String[] getDcDate() {
            return dcDate;
        }

        public String[] getDcDescription() {
            return dcDescription;
        }

        public String[] getDcFormat() {
            return dcFormat;
        }

        public String[] getDcIdentifier() {
            return dcIdentifier;
        }

        public String[] getDcLanguage() {
            return dcLanguage;
        }

        public String[] getDcPublisher() {
            return dcPublisher;
        }

        public String[] getDcRelation() {
            return dcRelation;
        }

        public String[] getDcRights() {
            return dcRights;
        }

        public String[] getDcSource() {
            return dcSource;
        }

        public String[] getDcSubject() {
            return dcSubject;
        }

        public String[] getDcTitle() {
            return dcTitle;
        }

        public String[] getDcType() {
            return dcType;
        }

        public BriefDoc getBriefDoc() {
            return new BriefDocImpl(
                    dcCreator[0], id, europeanaLanguage[0], thumbnail[0], dcTitle[0], europeanaType, europeanaYear[0], europeanaProvider[0]
            );
        }

    }

    private static class BriefDocWindowImpl implements BriefDocWindow {
        private List<BriefDoc> docs = new ArrayList<BriefDoc>();
        private Integer offset;
        private Integer hitCount;

        private BriefDocWindowImpl(Integer hitCount, Integer offset) {
            this.hitCount = hitCount;
            this.offset = offset;
        }

        public List<BriefDoc> getDocs() {
            return docs;
        }

        public Integer getOffset() {
            return offset;
        }

        public Integer getHitCount() {
            return hitCount;
        }
    }

    private static class BriefDocImpl implements BriefDoc {
        private int index;
        private String id;
        private String title;
        private String thumbnail;
        private String creator;
        private String year;
        private String provider;
        private String language;
        private DocType type;
        private BriefDocWindowImpl moreLikeThisDocWindow;

        public BriefDocImpl(JSONObject jsonObject, int index) throws JSONException {
            this.index = index;
            id = JsonUtil.getString(jsonObject, RecordField.EUROPEANA_URI.toFieldNameString());
            type = DocType.get(JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, FacetType.TYPE.toString()));
            title = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN,
                    false, RecordField.DC_TITLE.toFieldNameString(),
                    RecordField.DCTERMS_ALTERNATIVE.toFieldNameString(),
                    RecordField.DC_DESCRIPTION.toFieldNameString())[0];
            thumbnail = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, RecordField.EUROPEANA_OBJECT.toFieldNameString())[0];
            year = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.DATE_DEFAULT, false, FacetType.YEAR.toString())[0];
            language = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, FacetType.LANGUAGE.toString())[0];
            creator = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN,
                    false, RecordField.DC_CREATOR.toFieldNameString(),
                    RecordField.DC_CONTRIBUTOR.toFieldNameString())[0];
            provider = JsonUtil.getStringArray(jsonObject, JsonUtil.Default.UNKNOWN, false, FacetType.PROVIDER.toString())[0];
        }

        private BriefDocImpl(String creator, String id, String language, String thumbnail, String title, DocType type, String year, String provider) {
            this.creator = creator;
            this.id = id;
            this.language = language;
            this.thumbnail = thumbnail;
            this.title = title;
            this.type = type;
            this.year = year;
            this.provider = provider;
        }

        public int getIndex() {
            return index;
        }

        public String getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getThumbnail() {
            return thumbnail;
        }

        public String getCreator() {
            return creator;
        }

        public String getYear() {
            return year;
        }

        public String getProvider() {
            return provider;
        }

        public String getLanguage() {
            return language;
        }

        public DocType getType() {
            return type;
        }

        public BriefDocWindow getMoreLikeThis() {
            return moreLikeThisDocWindow;
        }
    }

    private static class DocIdWindowImpl implements DocIdWindow {
        private List<String> ids = new ArrayList<String>();
        private Integer offset;
        private Integer hitCount;

        private DocIdWindowImpl(Integer hitCount, Integer offset) {
            this.hitCount = hitCount;
            this.offset = offset;
        }

        public List<String> getIds() {
            return ids;
        }

        public Integer getOffset() {
            return offset;
        }

        public Integer getHitCount() {
            return hitCount;
        }
    }

    private static class FacetImpl implements Facet {
        private FacetType facetType;
        private List<FacetCount> counts = new ArrayList<FacetCount>();

        private FacetImpl(FacetType facetType) {
            this.facetType = facetType;
        }

        public FacetType getType() {
            return facetType;
        }

        public List<FacetCount> getCounts() {
            return counts;
        }
    }

    private static class FacetCountImpl implements FacetCount {
        private String value;
        private Integer count;

        private FacetCountImpl(String value, Integer count) {
            this.count = count;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public Integer getCount() {
            return count;
        }
    }

    private class IndentWriter {
        private StringWriter writer = new StringWriter();

        public void print(String prompt, Object object, int indent) {
            int tab = indent;
            while (tab-- > 0) {
                writer.write('\t');
            }
            writer.write(prompt);
            writer.write(": ");
            if (object == null) {
                writer.write("null");
                writer.write('\n');
            }
            else if (object instanceof List) {
                writer.write('\n');
                for (Object element : ((List) object)) {
                    print("element", element, indent + 1);
                }
            }
            else {
                boolean found = false;
                for (Class<?> iface : object.getClass().getInterfaces()) {
                    if (iface.getPackage() != ResultModel.class.getPackage()) {
                        continue;
                    }
                    if (!found) {
                        found = true;
                        writer.write('\n');
                    }
                    for (Method method : iface.getMethods()) {
                        if (method.getName().startsWith("get")) {
                            try {
                                Object got = method.invoke(object);
                                String attribute = method.getName().substring(3);
                                print(attribute, got, indent + 1);
                            }
                            catch (Exception e) {
                                throw new RuntimeException("Cannot execute: " + method.getName(), e);
                            }
                        }
                    }
                }
                if (!found) {
                    writer.write(object.toString());
                    writer.write('\n');
                }
            }
        }

        public String toString() {
            return writer.toString();
        }
    }

}