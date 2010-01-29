/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or as soon they
 *  will be approved by the European Commission - subsequent
 *  versions of the EUPL (the "Licence");
 *  you may not use this work except in compliance with the
 *  Licence.
 *  You may obtain a copy of the Licence at:
 *
 *  http://ec.europa.eu/idabc/eupl
 *
 *  Unless required by applicable law or agreed to in
 *  writing, software distributed under the Licence is
 *  distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied.
 *  See the Licence for the specific language governing
 *  permissions and limitations under the Licence.
 */

package eu.europeana.web.util;

import eu.europeana.database.domain.Language;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.Assert.*;

/**
 * Test the utility classes
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestStaticPageCache {
    private static final File root = new File("/tmp/static-page-cache");
    private static final String [] PAGE_NAMES = {
            "gumby",
            "pokey"
    };
    private static final Language[] LANGUAGES = {
            Language.DE,
            Language.SE,
            Language.NL
    };

    private StaticPageCache staticPageCache = new StaticPageCache();

    @Before
    public void setUp() throws FileNotFoundException {
        delete(root);
        assertTrue(root.mkdirs());
        for (String pageName : PAGE_NAMES) {
            for (Language language : LANGUAGES) {
                createFile(pageName, language);
            }
        }
        staticPageCache.setStaticPagePath(root.getAbsolutePath());
    }

    @After
    public void thatsIt() {
//        delete(root);
    }

    @Test
    public void fetches() throws IOException {
        assertNotNull(staticPageCache.getPage(PAGE_NAMES[1], LANGUAGES[1]));
        assertNull(staticPageCache.getPage("booger", LANGUAGES[1]));
    }

    private void createFile(String pageName, Language language) throws FileNotFoundException {
        File languageDir = new File(root, language.getCode());
        languageDir.mkdirs();
        File pageFile = new File(languageDir, pageName+"_"+language.getCode()+".html");
        PrintWriter out = new PrintWriter(pageFile);
        out.println("<html><head><title>"+pageName+" in "+language.getName()+"</title></head><body><h1>"+pageName+"</h1></body></html>");
        out.close();
    }

    private void delete(File file) {
        if (file.isDirectory()) {
            for (File sub : file.listFiles()) {
                delete(sub);
            }
        }
        file.delete();
    }


}