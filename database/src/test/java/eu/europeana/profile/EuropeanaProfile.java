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

package eu.europeana.profile;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.List;

/**
 * The classes used to read and interpret the europeana-profile.xml using XStream
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@XStreamAlias("europeana-profile")
public class EuropeanaProfile {

    @XStreamAlias("fields")
    public List<Field> fields;

    @XStreamAlias("config")
    public Config config;

    @XStreamAlias("field")
    public static class Field {

        @XStreamAsAttribute
        public String name;

        @XStreamAsAttribute
        public String namespace;
    }

    public static class Config {

        @XStreamAlias("solr-schema")
        public SolrSchema solrSchema;

        @XStreamAlias("freemarker-templates")
        public FreemarkerTemplates freemarkerTemplates;

        public static class SolrSchema {
            @XStreamAsAttribute
            public boolean generate;
            @XStreamAsAttribute
            public String outputPath;
        }

        public static class FreemarkerTemplates {
            @XStreamAsAttribute
            public boolean generate;
            @XStreamAsAttribute
            public String outputPath;
        }
    }

    private static Logger LOG = Logger.getLogger(EuropeanaProfile.class);
    public static void main(String[] args) {
        InputStream inputStream = EuropeanaProfile.class.getResourceAsStream("/europeana-profile.xml");
        XStream stream = new XStream();
        stream.processAnnotations(EuropeanaProfile.class);
        EuropeanaProfile profile = (EuropeanaProfile) stream.fromXML(inputStream);
        for (Field field : profile.fields) {
            LOG.info("Field: "+field.name);
        }
    }
}
