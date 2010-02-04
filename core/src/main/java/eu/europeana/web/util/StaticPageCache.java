/*
 * Copyright 2007 EDL FOUNDATION
 *
 *  Licensed under the EUPL, Version 1.0 or? as soon they
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
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class pays attention to a file system directory and delivers pages if they are present.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class StaticPageCache {
    private static final String HTML_EXTENSION = ".html";
    private Map<String, Page> pageMapCache = new ConcurrentHashMap<String, Page>();
    private String staticPagePath;

    @Value("#{europeanaProperties['static.page.path']}")
    public void setStaticPagePath(String staticPagePath) {
        this.staticPagePath = staticPagePath;
    }

    public String getPage(String pageName, Language language) throws IOException {
        String fileName = pageName+"_"+language.getCode();
        Page page = pageMap().get(fileName);
        if (page == null) {
            fileName = pageName + "_" + Language.EN.getCode();
            page = pageMap().get(fileName);
            if (page == null) {
                return null;
            }
        }
        return page.getContent();
    }

    public void invalidate() {
        pageMapCache.clear();
    }

    private Map<String, Page> pageMap() {
        if (pageMapCache.isEmpty()) {
            File root = new File(staticPagePath);
            if (!root.isDirectory()) {
                throw new RuntimeException(staticPagePath+" is not a directory!");
            }
            addToPageMap(root);
        }
        return pageMapCache;
    }

    private void addToPageMap(File directory) {
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                addToPageMap(file);
            }
            else if (file.getName().endsWith(HTML_EXTENSION)) {
                String baseFileName = file.getName().substring(0, file.getName().length() - HTML_EXTENSION.length());
                pageMapCache.put(baseFileName, new Page(file));
            }
        }
    }

    private class Page {
        private File file;
        private String content;

        private Page(File file) {
            this.file = file;
        }

        private synchronized String getContent() throws IOException {
            if (content == null) {
                Reader fileReader = new FileReader(file);
                char [] characters = new char[(int) file.length()];
                if (fileReader.read(characters) != characters.length) {
                    throw new IOException("Unable to read "+characters.length+" characters from "+file.getAbsolutePath());
                }
                fileReader.close();
                content = new String(characters);
            }
            return content;
        }
    }
}
