/*
 * Copyright 2010 DELVING BV
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
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

package eu.delving.core.util;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

/**
 * Make sure the multilingual information access works
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestMultilingualAccessTranslator {

    private ExposedKeysMessageSource exposedKeysMessageSource = new ExposedKeysMessageSource();
    private MultilingualAccessTranslator multilingualAccessTranslator = new MultilingualAccessTranslator();

    @Before
    public void before() throws Exception {
        exposedKeysMessageSource.setUseCodeAsDefaultMessage(true);
        exposedKeysMessageSource.setBasename("classpath:/i18n/messages");
        exposedKeysMessageSource.setDefaultEncoding("UTF-8");
        multilingualAccessTranslator.setExposedKeysMessageSource(exposedKeysMessageSource);
    }

    @Test
    public void backAndForth() {
        String title = multilingualAccessTranslator.toLocalizedName("dc_title", Locale.ENGLISH);
        Assert.assertEquals("Title", title);
        String fieldName = multilingualAccessTranslator.toFieldName(title, Locale.ENGLISH);
        Assert.assertEquals("dc_title", fieldName);
    }

    @Test
    public void multiWord() {
        String fieldName = multilingualAccessTranslator.toFieldName("isreferENCedby", Locale.ENGLISH);
        Assert.assertEquals("dcterms_isReferencedBy", fieldName);
    }
}
