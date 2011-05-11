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
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

import java.util.Arrays;
import java.util.Locale;

/**
 * Make sure the multilingual information access works
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestLocalizedFieldNames {

    private ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    private LocalizedFieldNames localizedFieldNames = new LocalizedFieldNames();

    @Before
    public void before() throws Exception {
        messageSource.setUseCodeAsDefaultMessage(true);
        messageSource.setBasename("classpath:/i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        localizedFieldNames.setMessageSource(messageSource);
    }

    @Test
    public void backAndForth() {
        LocalizedFieldNames.Lookup lookup = localizedFieldNames.createLookup(Arrays.asList("dc.title"));
        final Locale english = new Locale("en");
        String title = lookup.toLocalizedName("dc_title", english);
        Assert.assertEquals("Title", title);
        String fieldName = lookup.toFieldName(title, english);
        Assert.assertEquals("dc_title", fieldName);
    }

    @Test
    public void multiWord() {
        LocalizedFieldNames.Lookup lookup = localizedFieldNames.createLookup(Arrays.asList("dcterms.isReferencedBy"));
        final Locale english = new Locale("en");
        String fieldName = lookup.toFieldName("isreferENCedby", english);
        Assert.assertEquals("dcterms_isReferencedBy", fieldName);
    }
}
