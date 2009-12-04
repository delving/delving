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

package eu.europeana.cache;

import eu.europeana.query.EuropeanaProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Dec 8, 2008: 2:37:48 PM
 */
// todo: extent with check for imagemagick and network failure
public class DigitalObjectCacheTest {

    private DigitalObjectCacheImpl cache;
    private LinkSample[] initLinks = new LinkSample[]{
            new LinkSample("http://ogimages.bl.uk/images/019/019ADDOR0000002U00000000[SVC1].jpg", true),
            new LinkSample("http://www.landesarchiv-bw.de/plink/?f=1-358099-1&amp;ext=1", true),
            new LinkSample("http://mara.kbr.be/KBR_DL/print/01/SV 65095.jpg", true),
//            new LinkSample("http://mara.kbr.be/KBR_DL/print//SV 81721'.jpg", true),
            new LinkSample("http://www.bla.com/bla.jpg", false)
    };

    @Before
    public void setUp() throws IOException {
        cache = new DigitalObjectCacheImpl();
        cache.setRoot(new File("/tmp/test-cache/repository"));
        cache.setImageMagickPath(new EuropeanaProperties().getProperty("cache.imageMagickPath"));
    }

    @After
    public void tearDown() {
        deleteDirectory(new File("/tmp/test-cache"));
    }

    @Test
    public void testCache() throws IOException {
        for (LinkSample initLink : initLinks) {
            assertEquals(cache.cache(initLink.getLink()), initLink.isSuccess());
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                }
                else {
                    file.delete();
                }
            }
        }
        return (path.delete());
    }


    private class LinkSample {
        private String link;
        private boolean success;

        public LinkSample(String link, boolean success) {
            this.link = link;
            this.success = success;
        }

        public String getLink() {
            return link;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}

