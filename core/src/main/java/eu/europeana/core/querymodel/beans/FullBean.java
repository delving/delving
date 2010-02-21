/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
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

package eu.europeana.core.querymodel.beans;

import eu.europeana.core.querymodel.annotation.Europeana;
import eu.europeana.core.querymodel.annotation.Solr;
import eu.europeana.core.querymodel.query.BriefDoc;
import eu.europeana.core.querymodel.query.DocType;
import eu.europeana.core.querymodel.query.FullDoc;
import org.apache.commons.lang.WordUtils;
import org.apache.solr.client.solrj.beans.Field;

import java.util.ArrayList;
import java.util.List;

import static eu.europeana.core.querymodel.beans.BeanUtil.returnArrayOrElse;
import static eu.europeana.core.querymodel.beans.BeanUtil.returnStringOrElse;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class FullBean extends BriefBean implements FullDoc {

    // Europeana namespace
    @Europeana(type = true)
    @Solr(namespace = "europeana", name = "type", multivalued = false, fieldType = "string", toCopyField = {"TYPE"})
    @Field("europeana_type")
    String europeanaType;

    @Europeana()
    @Solr(namespace = "europeana", name = "userTag", toCopyField = {"text", "USERTAGS"})
    @Field("europeana_userTag")
    String[] europeanaUserTag;

    @Europeana()
    @Solr(namespace = "europeana", name = "language", fieldType = "string", toCopyField = {"text", "LANGUAGE"})
    @Field("europeana_language")
    String[] europeanaLanguage;

    @Europeana()
    @Solr(namespace = "europeana", name = "country")
    @Field("europeana_country")
    String[] europeanaCountry;

    @Europeana()
    @Solr(namespace = "europeana", name = "source")
    @Field("europeana_source")
    String[] europeanaSource;

    @Europeana()
    @Solr(namespace = "europeana", name = "isShownAt", fieldType = "string", toCopyField = {"text"})
    @Field("europeana_isShownAt")
    String[] europeanaisShownAt;

    @Europeana()
    @Solr(namespace = "europeana", name = "isShownBy", fieldType = "string", toCopyField = {"text"})
    @Field("europeana_isShownBy")
    String[] europeanaisShownBy;

    @Europeana()
    @Solr(namespace = "europeana", name = "year", fieldType = "string", toCopyField = {"text", "YEAR"})
    @Field("europeana_year")
    String[] europeanaYear;

    @Europeana()
    @Solr(namespace = "europeana", name = "hasObject", fieldType = "boolean")
    @Field("europeana_hasObject")
    boolean europeanahasObject;

    @Europeana()
    @Solr(namespace = "europeana", name = "provider", toCopyField = {"PROVIDER"})
    @Field("europeana_provider")
    String[] europeanaProvider;


    // Dublin Core / ESE fields
    @Europeana()
    @Solr(namespace = "dc", name = "coverage", toCopyField = {"text", "what", "subject"})
    @Field("dc_coverage")
    String[] dcCoverage;

    @Europeana()
    @Solr(namespace = "dc", name = "contributor", toCopyField = {"text", "who", "creator"})
    @Field("dc_contributor")
    String[] dcContributor;

    @Europeana()
    @Solr(namespace = "dc", name = "description", toCopyField = {"text", "description"})
    @Field("dc_description")
    String[] dcDescription;

    @Europeana()
    @Solr(namespace = "dc", name = "creator", toCopyField = {"text", "who", "creator"})
    @Field("dc_creator")
    String[] dcCreator;

    @Europeana()
    @Solr(namespace = "dc", name = "date", toCopyField = {"text", "when", "date"})
    @Field("dc_date")
    String[] dcDate;

    @Europeana()
    @Solr(namespace = "dc", name = "format", toCopyField = {"text"})
    @Field("dc_format")
    String[] dcFormat;

    @Europeana()
    @Solr(namespace = "dc", name = "identifier", toCopyField = {"text", "identifier"})
    @Field("dc_identifier")
    String[] dcIdentifier;

    @Europeana()
    @Solr(namespace = "dc", name = "language", toCopyField = {"text"})
    @Field("dc_language")
    String[] dcLanguage;

    @Europeana()
    @Solr(namespace = "dc", name = "publisher", toCopyField = {"text"})
    @Field("dc_publisher")
    String[] dcPublisher;

    @Europeana()
    @Solr(namespace = "dc", name = "relation", toCopyField = {"text", "relation"})
    @Field("dc_relation")
    String[] dcRelation;

    @Europeana()
    @Solr(namespace = "dc", name = "rights", toCopyField = {"text"})
    @Field("dc_rights")
    String[] dcRights;

    @Europeana()
    @Solr(namespace = "dc", name = "source", toCopyField = {"text"})
    @Field("dc_source")
    String[] dcSource;

    @Europeana()
    @Solr(namespace = "dc", name = "subject", toCopyField = {"text", "what", "subject"})
    @Field("dc_subject")
    String[] dcSubject;

    @Europeana()
    @Solr(namespace = "dc", name = "title", toCopyField = {"text"})
    @Field("dc_title")
    String[] dcTitle;

    @Europeana()
    @Solr(namespace = "dc", name = "type", toCopyField = {"text"})
    @Field("dc_type")
    String[] dcType;


    // Dublin Core Terms extended / ESE fields
    @Europeana()
    @Solr(namespace = "dcterms", name = "alternative", toCopyField = {"text"})
    @Field("dcterms_alternative")
    String[] dctermsAlternative;

    @Europeana()
    @Solr(namespace = "dcterms", name = "created", toCopyField = {"text", "when", "date"})
    @Field("dcterms_created")
    String[] dctermsCreated;

    @Europeana()
    @Solr(namespace = "dcterms", name = "conformsTo", toCopyField = {"text"})
    @Field("dcterms_conformsTo")
    String[] dctermsConformsTo;

    @Europeana()
    @Solr(namespace = "dcterms", name = "extent", toCopyField = {"text", "format"})
    @Field("dcterms_extent")
    String[] dctermsExtent;

    @Europeana()
    @Solr(namespace = "dcterms", name = "hasFormat", toCopyField = {"text", "relation"})
    @Field("dcterms_hasFormat")
    String[] dctermsHasFormat;

    @Europeana()
    @Solr(namespace = "dcterms", name = "hasPart", toCopyField = {"text", "relation"})
    @Field("dcterms_hasPart")
    String[] dctermsHasPart;

    @Europeana()
    @Solr(namespace = "dcterms", name = "hasVersion", toCopyField = {"text", "relation"})
    @Field("dcterms_hasVersion")
    String[] dctermsHasVersion;

    @Europeana()
    @Solr(namespace = "dcterms", name = "isFormatOf", toCopyField = {"text"})
    @Field("dcterms_isFormatOf")
    String[] dctermsIsFormatOf;

    @Europeana()
    @Solr(namespace = "dcterms", name = "isPartOf", toCopyField = {"text"})
    @Field("dcterms_isPartOf")
    String[] dctermsIsPartOf;

    @Europeana()
    @Solr(namespace = "dcterms", name = "isReferencedBy", toCopyField = {"text", "relation"})
    @Field("dcterms_isReferencedBy")
    String[] dctermsIsReferencedBy;

    @Europeana()
    @Solr(namespace = "dcterms", name = "isReplacedBy", toCopyField = {"text", "relation"})
    @Field("dcterms_isReplacedBy")
    String[] dctermsIsReplacedBy;

    @Europeana()
    @Solr(namespace = "dcterms", name = "isRequiredBy", toCopyField = {"text", "relation"})
    @Field("dcterms_isRequiredBy")
    String[] dctermsIsRequiredBy;

    @Europeana()
    @Solr(namespace = "dcterms", name = "issued", toCopyField = {"text", "date"})
    @Field("dcterms_issued")
    String[] dctermsIssued;

    @Europeana()
    @Solr(namespace = "dcterms", name = "isVersionOf", toCopyField = {"text"})
    @Field("dcterms_isVersionOf")
    String[] dctermsIsVersionOf;

    @Europeana()
    @Solr(namespace = "dcterms", name = "medium", toCopyField = {"text", "format"})
    @Field("dcterms_medium")
    String[] dctermsMedium;

    @Europeana()
    @Solr(namespace = "dcterms", name = "provenance", toCopyField = {"text"})
    @Field("dcterms_provenance")
    String[] dctermsProvenance;

    @Europeana()
    @Solr(namespace = "dcterms", name = "references", toCopyField = {"text"})
    @Field("dcterms_references")
    String[] dctermsReferences;

    @Europeana()
    @Solr(namespace = "dcterms", name = "replaces", toCopyField = {"text", "relation"})
    @Field("dcterms_replaces")
    String[] dctermsReplaces;

    @Europeana()
    @Solr(namespace = "dcterms", name = "requires", toCopyField = {"text", "relation"})
    @Field("dcterms_requires")
    String[] dctermsRequires;

    @Europeana()
    @Solr(namespace = "dcterms", name = "spatial", toCopyField = {"text", "where", "location", "subject"})
    @Field("dcterms_spatial")
    String[] dctermsSpatial;

    @Europeana()
    @Solr(namespace = "dcterms", name = "tableOfContents", toCopyField = {"text", "description"})
    @Field("dcterms_tableOfContents")
    String[] dctermsTableOfContents;

    @Europeana()
    @Solr(namespace = "dcterms", name = "temporal", toCopyField = {"text", "what", "subject"})
    @Field("dcterms_temporal")
    String[] dctermsTemporal;

    @Override
    public String getId() {
        return europeanaUri;
    }

    @Override
    public String[] getThumbnails() {
        return returnArrayOrElse(europeanaObject);
    }

    @Override
    public String[] getEuropeanaIsShownAt() {
        return returnArrayOrElse(europeanaisShownAt);
    }

    @Override
    public String[] getEuropeanaIsShownBy() {
        return returnArrayOrElse(europeanaisShownBy);
    }

    @Override
    public String[] getEuropeanaUserTag() {
        return returnArrayOrElse(europeanaUserTag);
    }

    @Override
    public Boolean getEuropeanaHasObject() {
        return europeanahasObject;
    }

    @Override
    public String[] getEuropeanaCountry() {
        final String[] countryArr = returnArrayOrElse(europeanaCountry, country);
        List<String> upperCasedCountries = new ArrayList<String>();
        for (String country : countryArr) {
            upperCasedCountries.add(WordUtils.capitalizeFully(country));
        }
        return upperCasedCountries.toArray(new String[upperCasedCountries.size()]);
    }

    @Override
    public String[] getEuropeanaProvider() {
        return returnArrayOrElse(europeanaProvider, provider);
    }

    @Override
    public String[] getEuropeanaSource() {
        return returnArrayOrElse(europeanaSource);
    }

    @Override
    public DocType getEuropeanaType() {
        return DocType.get(docType);
    }

    @Override
    public String[] getEuropeanaLanguage() {
        return returnArrayOrElse(europeanaLanguage, language);
    }

    @Override
    public String[] getEuropeanaYear() {
        return returnArrayOrElse(europeanaYear);
    }

    // DCTERMS fields

    @Override
    public String[] getDcTermsAlternative() {
        return returnArrayOrElse(dctermsAlternative);
    }

    @Override
    public String[] getDcTermsConformsTo() {
        return returnArrayOrElse(dctermsConformsTo);
    }

    @Override
    public String[] getDcTermsCreated() {
        return returnArrayOrElse(dctermsCreated);
    }

    @Override
    public String[] getDcTermsExtent() {
        return returnArrayOrElse(dctermsExtent);
    }

    @Override
    public String[] getDcTermsHasFormat() {
        return returnArrayOrElse(dctermsHasFormat);
    }

    @Override
    public String[] getDcTermsHasPart() {
        return returnArrayOrElse(dctermsHasPart);
    }

    @Override
    public String[] getDcTermsHasVersion() {
        return returnArrayOrElse(dctermsHasVersion);
    }

    @Override
    public String[] getDcTermsIsFormatOf() {
        return returnArrayOrElse(dctermsIsFormatOf);
    }

    @Override
    public String[] getDcTermsIsPartOf() {
        return returnArrayOrElse(dctermsIsPartOf);
    }

    @Override
    public String[] getDcTermsIsReferencedBy() {
        return returnArrayOrElse(dctermsIsReferencedBy);
    }

    @Override
    public String[] getDcTermsIsReplacedBy() {
        return returnArrayOrElse(dctermsIsReplacedBy);
    }

    @Override
    public String[] getDcTermsIsRequiredBy() {
        return returnArrayOrElse(dctermsIsRequiredBy);
    }

    @Override
    public String[] getDcTermsIssued() {
        return returnArrayOrElse(dctermsIssued);
    }

    @Override
    public String[] getDcTermsIsVersionOf() {
        return returnArrayOrElse(dctermsIsVersionOf);
    }

    @Override
    public String[] getDcTermsMedium() {
        return returnArrayOrElse(dctermsMedium);
    }

    @Override
    public String[] getDcTermsProvenance() {
        return returnArrayOrElse(dctermsProvenance);
    }

    @Override
    public String[] getDcTermsReferences() {
        return returnArrayOrElse(dctermsReferences);
    }

    @Override
    public String[] getDcTermsReplaces() {
        return returnArrayOrElse(dctermsReplaces);
    }

    @Override
    public String[] getDcTermsRequires() {
        return returnArrayOrElse(dctermsRequires);
    }

    @Override
    public String[] getDcTermsSpatial() {
        return returnArrayOrElse(dctermsSpatial);
    }

    @Override
    public String[] getDcTermsTableOfContents() {
        return returnArrayOrElse(dctermsTableOfContents);
    }

    @Override
    public String[] getDcTermsTemporal() {
        return returnArrayOrElse(dctermsTemporal);
    }

    @Override
    public String[] getDcContributor() {
        return returnArrayOrElse(dcContributor);
    }

    @Override
    public String[] getDcCoverage() {
        return returnArrayOrElse(dcCoverage);
    }

    @Override
    public String[] getDcCreator() {
        return returnArrayOrElse(dcCreator);
    }

    @Override
    public String[] getDcDate() {
        return returnArrayOrElse(dcDate);
    }

    @Override
    public String[] getDcDescription() {
        return returnArrayOrElse(dcDescription);
    }

    @Override
    public String[] getDcFormat() {
        return returnArrayOrElse(dcFormat);
    }

    @Override
    public String[] getDcIdentifier() {
        return returnArrayOrElse(dcIdentifier);
    }

    @Override
    public String[] getDcLanguage() {
        return returnArrayOrElse(dcLanguage);
    }

    @Override
    public String[] getDcPublisher() {
        return returnArrayOrElse(dcPublisher);
    }

    @Override
    public String[] getDcRelation() {
        return returnArrayOrElse(dcRelation);
    }

    @Override
    public String[] getDcRights() {
        return returnArrayOrElse(dcRights);
    }

    @Override
    public String[] getDcSource() {
        return returnArrayOrElse(dcSource);
    }

    @Override
    public String[] getDcSubject() {
        return returnArrayOrElse(dcSubject);
    }

    @Override
    public String[] getDcTitle() {
        return returnArrayOrElse(dcTitle);
    }

    @Override
    public String[] getDcType() {
        return returnArrayOrElse(dcType);
    }

    @Override
    public BriefDoc getBriefDoc() {
        return null;
    }

    @Override
    public String getEuropeanaCollectionName() {
        return returnStringOrElse(europeanaCollectionName);
    }
}

