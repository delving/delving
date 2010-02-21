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

package eu.europeana.core.querymodel.query;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public interface FullDoc {

    // Europeana elements
    String getId(); // this is europeanaId

    String[] getThumbnails();  // this is europeanaObject

    String[] getEuropeanaIsShownAt();

    String[] getEuropeanaIsShownBy();

    String[] getEuropeanaUserTag();

    Boolean getEuropeanaHasObject();

//    Boolean getEuropeanaShowFull();
//
    String[] getEuropeanaCountry();

    String[] getEuropeanaProvider();

    String[] getEuropeanaSource();

    DocType getEuropeanaType();

    String[] getEuropeanaLanguage(); // used to be Language

    String[] getEuropeanaYear();

    // here the dcterms namespaces starts
    String[] getDcTermsAlternative();

    String[] getDcTermsConformsTo();

    String[] getDcTermsCreated();

    String[] getDcTermsExtent();

    String[] getDcTermsHasFormat();

    String[] getDcTermsHasPart();

    String[] getDcTermsHasVersion();

    String[] getDcTermsIsFormatOf();

    String[] getDcTermsIsPartOf();

    String[] getDcTermsIsReferencedBy();

    String[] getDcTermsIsReplacedBy();

    String[] getDcTermsIsRequiredBy();

    String[] getDcTermsIssued();

    String[] getDcTermsIsVersionOf();

    String[] getDcTermsMedium();

    String[] getDcTermsProvenance();

    String[] getDcTermsReferences();

    String[] getDcTermsReplaces();

    String[] getDcTermsRequires();

    String[] getDcTermsSpatial();

    String[] getDcTermsTableOfContents();

    String[] getDcTermsTemporal();

    // here the dc namespace starts
    String[] getDcContributor();

    String[] getDcCoverage();

    String[] getDcCreator();

    String[] getDcDate();

    String[] getDcDescription();

    String[] getDcFormat();

    String[] getDcIdentifier();

    String[] getDcLanguage();

    String[] getDcPublisher();

    String[] getDcRelation();

    String[] getDcRights();

    String[] getDcSource();

    String[] getDcSubject();

    String[] getDcTitle();

    String[] getDcType();

    BriefDoc getBriefDoc();

    String getEuropeanaCollectionName();
}
