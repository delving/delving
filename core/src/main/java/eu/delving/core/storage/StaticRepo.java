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
import java.util.Map;
import java.util.Set;

/**
 * The repository of static pages and images
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public interface StaticRepo {

    Map<String, List<MenuItem>> getMenus(Locale locale);

    Set<String> getPagePaths();

    Set<String> getImagePaths();

    Page getPage(String path);

    Page getPage(String path, String id);

    void approve(String path, String idString);

    void setHidden(String path, boolean hidden);

    List<Page> getPageVersions(String path);

    byte[] getImage(String path);

    void deleteImage(String path);

    Page createPage(String path);

    boolean setPagePath(String oldPath, String newPath);

    void putImage(String path, byte[] content);

    boolean setImagePath(String oldPath, String newPath);

    public interface Page {

        ObjectId getId();

        String getPath();

        boolean isHidden();

        void setHidden(boolean hidden);

        String getMenuName();

        int getMenuPriority();

        String getTitle(Locale locale);

        String getContent(Locale locale);

        Date getDate();

        void setContent(String title, String content, Locale locale);

        void setMenu(String menuName, int menuPriority);

        void remove();

        String PATH = "path";
        String TITLE = "title";
        String CONTENT = "content";
        String MENU_NAME = "menuName";
        String MENU_PRIORITY = "menuPriority";
        String HIDDEN = "hidden";
    }

    public class MenuItem implements Comparable<MenuItem>{
        private String menuName;
        private int menuPriority;
        private String title;
        private String path;

        public MenuItem(String menuName, int menuPriority, String title, String path) {
            this.menuName = menuName;
            this.menuPriority = menuPriority;
            this.title = title;
            this.path = path;
        }

        public String getMenuName() {
            return menuName;
        }

        public int getMenuPriority() {
            return menuPriority;
        }

        public String getTitle() {
            return title;
        }

        public String getPath() {
            return path;
        }

        @Override
        public int compareTo(StaticRepo.MenuItem menuItem) {
            if (menuPriority > menuItem.menuPriority) {
                return 11;
            }
            else if (menuPriority < menuItem.menuPriority) {
                return -1;
            }
            else {
                return 0;
            }
        }
    }

    String PAGES_COLLECTION = "pages";
    String IMAGES_COLLECTION = "images";
    String DEFAULT_LANGUAGE = "en";
    String MONGO_ID = "_id";
}
