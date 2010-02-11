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

package eu.europeana.beans;

import eu.europeana.beans.annotation.AnnotationProcessorImpl;
import eu.europeana.beans.annotation.EuropeanaBean;
import eu.europeana.beans.annotation.EuropeanaField;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestAnnotationProcessor {

    private Logger log = Logger.getLogger(getClass());

    @Test
    public void processThem() throws Exception {
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(IdBean.class);
        list.add(BriefBean.class);
        list.add(FullBean.class);
        list.add(AllFieldBean.class);
        AnnotationProcessorImpl annotationProcessor = new AnnotationProcessorImpl();
        annotationProcessor.setClasses(list);
        EuropeanaBean bean = annotationProcessor.getEuropeanaBean(BriefBean.class);
        assertEquals(11, bean.getFields().size());
        for (EuropeanaField field : bean.getFields()) {
            log.info("BriefBean field "+field.getName());
        }
        assertEquals(5, annotationProcessor.getFacetFields().size());
        for (EuropeanaField ff : annotationProcessor.getFacetFields()) {
            log.info("facet " + ff.getFieldNameString());
        }

    }

}