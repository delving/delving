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

package eu.europeana.definitions.beans;

import eu.europeana.definitions.annotations.Europeana;
import eu.europeana.definitions.annotations.Solr;
import eu.europeana.definitions.presentation.BriefDoc;
import eu.europeana.definitions.presentation.DocType;
import eu.europeana.definitions.presentation.FullDoc;

import java.util.ArrayList;
import java.util.List;

import static eu.europeana.definitions.annotations.ValidationLevel.ESE_OPTIONAL;
import static eu.europeana.definitions.annotations.ValidationLevel.ESE_PLUS_OPTIONAL;
import static eu.europeana.definitions.annotations.ValidationLevel.ESE_PLUS_REQUIRED;
import static eu.europeana.definitions.annotations.ValidationLevel.ESE_REQUIRED;

/**
 * todo: note that this is a copy of eu.europeana.core.querymodel.beans.* with SOLR @Field annotation removed
 * 
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class FullBean extends BriefBean implements FullDoc {

    // Europeana namespace
    @Europeana(validation = ESE_REQUIRED, type = true, mappable = true)
    @Solr(prefix = "europeana", localName = "type", multivalued = false, fieldType = "string", toCopyField = {"TYPE"})
    String europeanaType;

    @Europeana(validation = ESE_PLUS_OPTIONAL)
    @Solr(prefix = "europeana", localName = "userTag", toCopyField = {"text", "USERTAGS"})
    String[] europeanaUserTag;

    @Europeana(validation = ESE_PLUS_REQUIRED, importAddition = true)
    @Solr(prefix = "europeana", localName = "language", fieldType = "string", toCopyField = {"text", "LANGUAGE"})
    String[] europeanaLanguage;

    @Europeana(validation = ESE_PLUS_REQUIRED, importAddition = true)
    @Solr(prefix = "europeana", localName = "country")
    String[] europeanaCountry;

    // todo find out what this field is
    @Europeana(validation = ESE_PLUS_OPTIONAL)
    @Solr(prefix = "europeana", localName = "source")
    String[] europeanaSource;

    @Europeana(validation = ESE_REQUIRED, mappable = true)
    @Solr(prefix = "europeana", localName = "isShownAt", fieldType = "string", toCopyField = {"text"})
    String[] europeanaisShownAt;

    @Europeana(validation = ESE_REQUIRED, mappable = true)
    @Solr(prefix = "europeana", localName = "isShownBy", fieldType = "string", toCopyField = {"text"})
    String[] europeanaisShownBy;

    @Europeana(validation = ESE_PLUS_OPTIONAL, importAddition = true)
    @Solr(prefix = "europeana", localName = "year", fieldType = "string", toCopyField = {"text", "YEAR"})
    String[] europeanaYear;

    @Europeana(validation = ESE_PLUS_OPTIONAL, importAddition = true)
    @Solr(prefix = "europeana", localName = "hasObject", fieldType = "boolean")
    boolean europeanahasObject;

    @Europeana(validation = ESE_PLUS_REQUIRED, importAddition = true)
    @Solr(prefix = "europeana", localName = "provider", toCopyField = {"PROVIDER"})
    String[] europeanaProvider;


    // Dublin Core / ESE fields
    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dc", localName = "coverage", toCopyField = {"text", "what", "subject"})
    String[] dcCoverage;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dc", localName = "contributor", toCopyField = {"text", "who", "creator"})
    String[] dcContributor;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dc", localName = "description", toCopyField = {"text", "description"})
    String[] dcDescription;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dc", localName = "creator", toCopyField = {"text", "who", "creator"})
    String[] dcCreator;

    @Europeana(validation = ESE_OPTIONAL, mappable = true, converter="extractYear")
    @Solr(prefix = "dc", localName = "date", toCopyField = {"text", "when", "date"})
    String[] dcDate;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dc", localName = "format", toCopyField = {"text"})
    String[] dcFormat;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dc", localName = "identifier", toCopyField = {"text", "identifier"})
    String[] dcIdentifier;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dc", localName = "language", toCopyField = {"text"})
    String[] dcLanguage;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dc", localName = "publisher", toCopyField = {"text"})
    String[] dcPublisher;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dc", localName = "relation", toCopyField = {"text", "relation"})
    String[] dcRelation;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dc", localName = "rights", toCopyField = {"text"})
    String[] dcRights;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dc", localName = "source", toCopyField = {"text"})
    String[] dcSource;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dc", localName = "subject", toCopyField = {"text", "what", "subject"})
    String[] dcSubject;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dc", localName = "title", toCopyField = {"text"})
    String[] dcTitle;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dc", localName = "type", toCopyField = {"text"})
    String[] dcType;


    // Dublin Core Terms extended / ESE fields
    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "alternative", toCopyField = {"text"})
    String[] dctermsAlternative;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "created", toCopyField = {"text", "when", "date"})
    String[] dctermsCreated;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "conformsTo", toCopyField = {"text"})
    String[] dctermsConformsTo;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "extent", toCopyField = {"text", "format"})
    String[] dctermsExtent;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "hasFormat", toCopyField = {"text", "relation"})
    String[] dctermsHasFormat;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "hasPart", toCopyField = {"text", "relation"})
    String[] dctermsHasPart;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "hasVersion", toCopyField = {"text", "relation"})
    String[] dctermsHasVersion;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "isFormatOf", toCopyField = {"text"})
    String[] dctermsIsFormatOf;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "isPartOf", toCopyField = {"text"})
    String[] dctermsIsPartOf;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "isReferencedBy", toCopyField = {"text", "relation"})
    String[] dctermsIsReferencedBy;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "isReplacedBy", toCopyField = {"text", "relation"})
    String[] dctermsIsReplacedBy;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "isRequiredBy", toCopyField = {"text", "relation"})
    String[] dctermsIsRequiredBy;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "issued", toCopyField = {"text", "date"})
    String[] dctermsIssued;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "isVersionOf", toCopyField = {"text"})
    String[] dctermsIsVersionOf;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "medium", toCopyField = {"text", "format"})
    String[] dctermsMedium;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "provenance", toCopyField = {"text"})
    String[] dctermsProvenance;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "references", toCopyField = {"text"})
    String[] dctermsReferences;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "replaces", toCopyField = {"text", "relation"})
    String[] dctermsReplaces;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "requires", toCopyField = {"text", "relation"})
    String[] dctermsRequires;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "spatial", toCopyField = {"text", "where", "location", "subject"})
    String[] dctermsSpatial;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "tableOfContents", toCopyField = {"text", "description"})
    String[] dctermsTableOfContents;

    @Europeana(validation = ESE_OPTIONAL, mappable = true)
    @Solr(prefix = "dcterms", localName = "temporal", toCopyField = {"text", "what", "subject"})
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
            upperCasedCountries.add(country.toUpperCase());
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

    private static String[] returnArrayOrElse(String[] s) {
        return (s != null) ? s : new String[]{" "};
    }

    private static String[] returnArrayOrElse (String[] ... arrs) {
        for (String[] arr : arrs) {
            if (arr != null) {
                return arr;
            }
        }
        return new String[]{" "};
    }
}

