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

import eu.europeana.core.querymodel.query.BriefDoc;
import eu.europeana.core.querymodel.query.DocType;
import eu.europeana.core.querymodel.query.FullDoc;
import eu.europeana.definitions.annotations.Europeana;
import eu.europeana.definitions.annotations.Solr;
import org.apache.commons.lang.WordUtils;
import org.apache.solr.client.solrj.beans.Field;

import java.util.ArrayList;
import java.util.List;

import static eu.europeana.core.querymodel.beans.BeanUtil.returnArrayOrElse;
import static eu.europeana.core.querymodel.beans.BeanUtil.returnStringOrElse;
import static eu.europeana.definitions.annotations.FieldCategory.ESE;
import static eu.europeana.definitions.annotations.FieldCategory.ESE_PLUS;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class FullBean extends BriefBean implements FullDoc {

    // Europeana namespace
    @Europeana(required = true, type = true)
    @Solr(prefix = "europeana", localName = "type", multivalued = false, fieldType = "string", toCopyField = {"TYPE"})
    @Field("europeana_type")
    String europeanaType;

    @Europeana(category = ESE_PLUS)
    @Solr(prefix = "europeana", localName = "userTag", toCopyField = {"text", "USERTAGS"})
    @Field("europeana_userTag")
    String[] europeanaUserTag;

    @Europeana(category = ESE_PLUS, required = true, constant = true)
    @Solr(prefix = "europeana", localName = "language", fieldType = "string", toCopyField = {"text", "LANGUAGE"})
    @Field("europeana_language")
    String[] europeanaLanguage;

    @Europeana(category = ESE_PLUS, required = true, constant = true)
    @Solr(prefix = "europeana", localName = "country")
    @Field("europeana_country")
    String[] europeanaCountry;

    // todo find out what this field is
    @Europeana(category = ESE_PLUS)
    @Solr(prefix = "europeana", localName = "source")
    @Field("europeana_source")
    String[] europeanaSource;

    @Europeana(required = true, url = true)
    @Solr(prefix = "europeana", localName = "isShownAt", fieldType = "string", toCopyField = {"text"})
    @Field("europeana_isShownAt")
    String[] europeanaisShownAt;

    @Europeana(required = true, url = true)
    @Solr(prefix = "europeana", localName = "isShownBy", fieldType = "string", toCopyField = {"text"})
    @Field("europeana_isShownBy")
    String[] europeanaisShownBy;

    @Europeana(category = ESE_PLUS, constant = true)
    @Solr(prefix = "europeana", localName = "year", fieldType = "string", toCopyField = {"text", "YEAR"})
    @Field("europeana_year")
    String[] europeanaYear;

    @Europeana(category = ESE_PLUS, constant = true)
    @Solr(prefix = "europeana", localName = "hasObject", fieldType = "boolean")
    @Field("europeana_hasObject")
    boolean europeanahasObject;

    @Europeana(category = ESE_PLUS, required = true, constant = true)
    @Solr(prefix = "europeana", localName = "provider", toCopyField = {"PROVIDER"})
    @Field("europeana_provider")
    String[] europeanaProvider;


    // Dublin Core / ESE fields
    @Europeana
    @Solr(prefix = "dc", localName = "coverage", toCopyField = {"text", "what", "subject"})
    @Field("dc_coverage")
    String[] dcCoverage;

    @Europeana
    @Solr(prefix = "dc", localName = "contributor", toCopyField = {"text", "who", "creator"})
    @Field("dc_contributor")
    String[] dcContributor;

    @Europeana
    @Solr(prefix = "dc", localName = "description", toCopyField = {"text", "description"})
    @Field("dc_description")
    String[] dcDescription;

    @Europeana
    @Solr(prefix = "dc", localName = "creator", toCopyField = {"text", "who", "creator"})
    @Field("dc_creator")
    String[] dcCreator;

    @Europeana(category = ESE, converter="extractYear")
    @Solr(prefix = "dc", localName = "date", toCopyField = {"text", "when", "date"})
    @Field("dc_date")
    String[] dcDate;

    @Europeana
    @Solr(prefix = "dc", localName = "format", toCopyField = {"text"})
    @Field("dc_format")
    String[] dcFormat;

    @Europeana
    @Solr(prefix = "dc", localName = "identifier", toCopyField = {"text", "identifier"})
    @Field("dc_identifier")
    String[] dcIdentifier;

    @Europeana
    @Solr(prefix = "dc", localName = "language", toCopyField = {"text"})
    @Field("dc_language")
    String[] dcLanguage;

    @Europeana
    @Solr(prefix = "dc", localName = "publisher", toCopyField = {"text"})
    @Field("dc_publisher")
    String[] dcPublisher;

    @Europeana
    @Solr(prefix = "dc", localName = "relation", toCopyField = {"text", "relation"})
    @Field("dc_relation")
    String[] dcRelation;

    @Europeana
    @Solr(prefix = "dc", localName = "rights", toCopyField = {"text"})
    @Field("dc_rights")
    String[] dcRights;

    @Europeana
    @Solr(prefix = "dc", localName = "source", toCopyField = {"text"})
    @Field("dc_source")
    String[] dcSource;

    @Europeana
    @Solr(prefix = "dc", localName = "subject", toCopyField = {"text", "what", "subject"})
    @Field("dc_subject")
    String[] dcSubject;

    @Europeana
    @Solr(prefix = "dc", localName = "title", toCopyField = {"text"})
    @Field("dc_title")
    String[] dcTitle;

    @Europeana
    @Solr(prefix = "dc", localName = "type", toCopyField = {"text"})
    @Field("dc_type")
    String[] dcType;


    // Dublin Core Terms extended / ESE fields
    @Europeana
    @Solr(prefix = "dcterms", localName = "alternative", toCopyField = {"text"})
    @Field("dcterms_alternative")
    String[] dctermsAlternative;

    @Europeana
    @Solr(prefix = "dcterms", localName = "created", toCopyField = {"text", "when", "date"})
    @Field("dcterms_created")
    String[] dctermsCreated;

    @Europeana
    @Solr(prefix = "dcterms", localName = "conformsTo", toCopyField = {"text"})
    @Field("dcterms_conformsTo")
    String[] dctermsConformsTo;

    @Europeana
    @Solr(prefix = "dcterms", localName = "extent", toCopyField = {"text", "format"})
    @Field("dcterms_extent")
    String[] dctermsExtent;

    @Europeana
    @Solr(prefix = "dcterms", localName = "hasFormat", toCopyField = {"text", "relation"})
    @Field("dcterms_hasFormat")
    String[] dctermsHasFormat;

    @Europeana
    @Solr(prefix = "dcterms", localName = "hasPart", toCopyField = {"text", "relation"})
    @Field("dcterms_hasPart")
    String[] dctermsHasPart;

    @Europeana
    @Solr(prefix = "dcterms", localName = "hasVersion", toCopyField = {"text", "relation"})
    @Field("dcterms_hasVersion")
    String[] dctermsHasVersion;

    @Europeana
    @Solr(prefix = "dcterms", localName = "isFormatOf", toCopyField = {"text"})
    @Field("dcterms_isFormatOf")
    String[] dctermsIsFormatOf;

    @Europeana
    @Solr(prefix = "dcterms", localName = "isPartOf", toCopyField = {"text"})
    @Field("dcterms_isPartOf")
    String[] dctermsIsPartOf;

    @Europeana
    @Solr(prefix = "dcterms", localName = "isReferencedBy", toCopyField = {"text", "relation"})
    @Field("dcterms_isReferencedBy")
    String[] dctermsIsReferencedBy;

    @Europeana
    @Solr(prefix = "dcterms", localName = "isReplacedBy", toCopyField = {"text", "relation"})
    @Field("dcterms_isReplacedBy")
    String[] dctermsIsReplacedBy;

    @Europeana
    @Solr(prefix = "dcterms", localName = "isRequiredBy", toCopyField = {"text", "relation"})
    @Field("dcterms_isRequiredBy")
    String[] dctermsIsRequiredBy;

    @Europeana
    @Solr(prefix = "dcterms", localName = "issued", toCopyField = {"text", "date"})
    @Field("dcterms_issued")
    String[] dctermsIssued;

    @Europeana
    @Solr(prefix = "dcterms", localName = "isVersionOf", toCopyField = {"text"})
    @Field("dcterms_isVersionOf")
    String[] dctermsIsVersionOf;

    @Europeana
    @Solr(prefix = "dcterms", localName = "medium", toCopyField = {"text", "format"})
    @Field("dcterms_medium")
    String[] dctermsMedium;

    @Europeana
    @Solr(prefix = "dcterms", localName = "provenance", toCopyField = {"text"})
    @Field("dcterms_provenance")
    String[] dctermsProvenance;

    @Europeana
    @Solr(prefix = "dcterms", localName = "references", toCopyField = {"text"})
    @Field("dcterms_references")
    String[] dctermsReferences;

    @Europeana
    @Solr(prefix = "dcterms", localName = "replaces", toCopyField = {"text", "relation"})
    @Field("dcterms_replaces")
    String[] dctermsReplaces;

    @Europeana
    @Solr(prefix = "dcterms", localName = "requires", toCopyField = {"text", "relation"})
    @Field("dcterms_requires")
    String[] dctermsRequires;

    @Europeana
    @Solr(prefix = "dcterms", localName = "spatial", toCopyField = {"text", "where", "location", "subject"})
    @Field("dcterms_spatial")
    String[] dctermsSpatial;

    @Europeana
    @Solr(prefix = "dcterms", localName = "tableOfContents", toCopyField = {"text", "description"})
    @Field("dcterms_tableOfContents")
    String[] dctermsTableOfContents;

    @Europeana
    @Solr(prefix = "dcterms", localName = "temporal", toCopyField = {"text", "what", "subject"})
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

