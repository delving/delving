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

package eu.europeana.database.domain;

import eu.europeana.query.DocType;
import eu.europeana.query.FacetType;
import eu.europeana.query.RecordField;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.hibernate.annotations.Index;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Entity
public class EuropeanaId implements Serializable {
    private static final long serialVersionUID = 7542358284490036076L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "collectionid", nullable = false)
    private EuropeanaCollection collection;

    @Column
    private Integer timesViewed;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastViewed;

    @Column(nullable = false)
    @Index(name = "orphan_index")
    private Boolean orphan = false;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @Index(name = "lastmodified_index")
    private Date lastModified;

    @Column(length = 256, unique = true)
    @Index(name = "europeanauri_index")
    private String europeanaUri;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "europeanaid")
    private List<SocialTag> socialTags;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "europeanaid")
    private List<EditorPick> editorPicks;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "europeanaId")
    @JoinColumn(name = "europeanaid")
    private List<EuropeanaObject> europeanaObjects;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "europeanaId")
    @JoinColumn(name = "europeanaid")
    private List<CarouselItem> carouselItems;


    @Column(nullable = true)
    private Float boostFactor;

    @Deprecated
    @Lob
    private String solrRecords;

    public EuropeanaId() {
        this.orphan = false;
    }

    public EuropeanaId(EuropeanaCollection collection) {
        this.collection = collection;
    }

    public Long getId() {
        return id;
    }

    public EuropeanaCollection getCollection() {
        return collection;
    }

    public String getEuropeanaUri() {
        return europeanaUri;
    }

    public void setEuropeanaUri(String europeanaUri) {
        this.europeanaUri = europeanaUri;
    }

    public Integer getTimesViewed() {
        if (timesViewed == null) {
            timesViewed = 0;
        }
        return timesViewed;
    }

    public void setTimesViewed(Integer timesViewed) {
        this.timesViewed = timesViewed;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getLastViewed() {
        return lastViewed;
    }

    public void setLastViewed(Date lastViewed) {
        this.lastViewed = lastViewed;
    }

    public boolean isOrphan() {
        return orphan == null ? false : orphan;
    }

    public void setOrphan(Boolean orphan) {
        this.orphan = orphan;
    }

    public List<SocialTag> getSocialTags() {
        if (socialTags == null) {
            socialTags = new ArrayList<SocialTag>();
        }
        return socialTags;
    }

    public void setSocialTags(List<SocialTag> socialTags) {
        this.socialTags = socialTags;
    }

    public List<EditorPick> getEditorPicks() {
        if (editorPicks == null) {
            editorPicks = new ArrayList<EditorPick>();
        }
        return editorPicks;
    }

    public void setEditorPicks(List<EditorPick> editorPicks) {
        this.editorPicks = editorPicks;
    }

    public List<EuropeanaObject> getEuropeanaObjects() {
        if (europeanaObjects == null) {
            europeanaObjects = new ArrayList<EuropeanaObject>();
        }
        return europeanaObjects;
    }

    public void setEuropeanaObjects(List<EuropeanaObject> europeanaObjects) {
        this.europeanaObjects = europeanaObjects;
    }

    @Deprecated
    public String getSolrRecords() {
        return solrRecords;
    }

    @Deprecated
    public void setSolrRecords(String solrRecords) {
        this.solrRecords = solrRecords;
    }

    public EuropeanaObject getEuropeanaObject(String url) {
        for (EuropeanaObject object : getEuropeanaObjects()) {
            if (object.getObjectUrl().equals(url)) {
                return object;
            }
        }
        return null;
    }

    public List<CarouselItem> getCarouselItems() {
        if (carouselItems == null) {
            carouselItems = new ArrayList<CarouselItem>();
        }
        return carouselItems;
    }

    public void setCarouselItems(List<CarouselItem> carouselItems) {
        this.carouselItems = carouselItems;
    }

//    @Transactional
//    public boolean hasCarouselItem() {
//        boolean foundCarousel = false;
//        if (carouselItems != null && carouselItems.size() != 0) {
//            foundCarousel = true;
//        }
//        return foundCarousel;
//    }
//
//    public void setCarouselItems(List<CarouselItem> carouselItems) {
//        this.carouselItems = carouselItems;
//    }

    public Float getBoostFactor() {
        return boostFactor;
    }

    public void setBoostFactor(Float boostFactor) {
        this.boostFactor = boostFactor;
    }

    public CarouselItem createCarouselItem() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(new ByteArrayInputStream(getSolrRecords().getBytes("UTF-8")));
            CarouselItem item = new CarouselItem();
            item.setEuropeanaUri(getEuropeanaUri());
            Map<String, String> fieldMap = createMap(document);
            item.setTitle(getString(fieldMap, RecordField.DC_TITLE.toFieldNameString()));
            item.setLanguage(Language.findByName(fieldMap.get(RecordField.EUROPEANA_LANGUAGE.getFacetType().toString())));
            item.setProvider(getString(fieldMap, RecordField.EUROPEANA_PROVIDER.getFacetType().toString()));
            item.setCreator(getString(fieldMap, RecordField.DC_CREATOR.toFieldNameString()));
            String docTypeString = getString(fieldMap, RecordField.EUROPEANA_TYPE.getFacetType().toString());
            item.setType(DocType.get(docTypeString));
            item.setThumbnail(getString(fieldMap, RecordField.EUROPEANA_OBJECT.toFieldNameString()));
            item.setYear(getString(fieldMap, FacetType.YEAR.toString()));
            return item;
        }
        catch (ParserConfigurationException e) {
            throw new RuntimeException("Parser not configured", e);
        }
        catch (IOException e) {
            throw new RuntimeException("Problem reading solr records", e);
        }
        catch (SAXException e) {
            throw new RuntimeException("Parse problem", e);
        }
    }

    public SavedItem createSavedItem() {
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(new ByteArrayInputStream(getSolrRecords().getBytes("UTF-8")));
            SavedItem item = new SavedItem();
            Map<String, String> fieldMap = createMap(document);
            item.setTitle(getString(fieldMap, RecordField.DC_TITLE.toFieldNameString()));
            item.setAuthor(getString(fieldMap, RecordField.DC_CREATOR.toFieldNameString()));
            String docTypeString = getString(fieldMap, RecordField.EUROPEANA_TYPE.getFacetType().toString());
            item.setDocType(DocType.get(docTypeString));
            item.setEuropeanaObject(getString(fieldMap, RecordField.EUROPEANA_OBJECT.toFieldNameString()));
            return item;
        }
        catch (ParserConfigurationException e) {
            throw new RuntimeException("Parser not configured", e);
        }
        catch (IOException e) {
            throw new RuntimeException("Problem reading solr records", e);
        }
        catch (SAXException e) {
            throw new RuntimeException("Parse problem", e);
        }
    }

    private String getString(Map<String, String> fieldMap, String fieldName) {
        String s = fieldMap.get(fieldName);
        if (s == null) {
            s = " ";
        }
        return s;
    }

    private static Map<String, String> createMap(Document document) {
        Map<String, String> map = new TreeMap<String, String>();
        NodeList list = document.getElementsByTagName("field");
        for (int walkField = 0; walkField < list.getLength(); walkField++) {
            Node field = list.item(walkField);
            String name = field.getAttributes().getNamedItem("name").getNodeValue();
            String value = null;
            NodeList childList = field.getChildNodes();
            for (int walk = 0; walk < childList.getLength(); walk++) {
                Node child = childList.item(walk);
                if (child.getNodeType() == Node.TEXT_NODE) {
                    value = child.getNodeValue();
                }
            }
            if (value != null) {
                map.put(name, value);
            }
        }
        return map;
    }

    public String toString() {
        return "EuropeanaId(" + europeanaUri + ")";
    }
}