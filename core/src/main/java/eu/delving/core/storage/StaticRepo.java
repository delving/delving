/*
 * Copyright 2010 DELVING BV
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

package eu.delving.core.storage;

import org.bson.types.ObjectId;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * The repository of static pages and images
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public interface StaticRepo {

    Set<String> getPagePaths();

    Set<String> getImagePaths();

    Page getPage(String path);

    Page getPage(String path, String id);

    void approve(String path, String idString);

    void setHidden(String path, boolean hidden);

    List<Page> getPageVersions(String path);

    byte[] getImage(String path);

    void deleteImage(String path);

    ObjectId putPage(String path, String content, Locale locale);

    boolean setPagePath(String oldPath, String newPath);

    void putImage(String path, byte[] content);

    boolean setImagePath(String oldPath, String newPath);

    public interface Page {

        ObjectId getId();

        String getPath();

        boolean isHidden();

        void setHidden(boolean hidden);

        String getContent(Locale locale);

        Date getDate();

        void setContent(String content, Locale locale);

        void remove();
    }

    String PAGES_COLLECTION = "pages";
    String IMAGES_COLLECTION = "images";
    String PATH = "path";
    String CONTENT = "content";
    String HIDDEN = "hidden";
    String DEFAULT_LANGUAGE = "en";
    String MONGO_ID = "_id";
}
