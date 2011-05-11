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

package eu.delving.core.storage.impl;

import com.mongodb.*;
import eu.delving.core.storage.StaticRepo;
import eu.delving.core.util.MongoFactory;
import eu.delving.core.util.PortalTheme;
import eu.delving.core.util.ThemeInterceptor;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

import static eu.delving.core.util.MongoObject.mob;

/**
 * The repository of static pages and images
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class StaticRepoImpl implements StaticRepo {

    @Autowired
    private MongoFactory mongoFactory;

    public void setMongoFactory(MongoFactory mongoFactory) {
        this.mongoFactory = mongoFactory;
    }

    @Value("#{launchProperties['portal.name']}")
    private String portalName = "";

    @Value("#{launchProperties['portal.mongo.dbName']}")
    private String databaseName;


    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setPortalName(String portalName) {
        this.portalName = portalName;
    }

    @Override
    public Map<String, List<MenuItem>> getMenus(Locale locale) {
        Map<String, Page> latestPages = getLatestPages();
        Map<String, List<MenuItem>> menus = new TreeMap<String, List<MenuItem>>();
        for (Page page : latestPages.values()) {
            String menuName = page.getMenuName();
            if (menuName != null) {
                MenuItem menuItem = new MenuItem(menuName, page.getMenuPriority(), page.getTitle(locale), page.getPath());
                List<MenuItem> list = menus.get(menuName);
                if (list == null) {
                    menus.put(menuName, list = new ArrayList<MenuItem>());
                }
                list.add(menuItem);
            }
        }
        for (List<MenuItem> menu : menus.values()) {
            Collections.sort(menu);
        }
        return menus;
    }

    @Override
    public Set<String> getPagePaths() {
        return getPathSet(PAGES_COLLECTION);
    }

    @Override
    public Set<String> getImagePaths() {
        return getPathSet(IMAGES_COLLECTION);
    }

    @Override
    public Page getPage(String path) {
        Page page = getLatestPage(path);
        if (page != null) {
            return page;
        }
        else {
            return new PageImpl(mob(
                    Page.PATH, path,
                    Page.CONTENT, String.format("<a href=\"/%s/%s\">%s</a>", portalName, path, path)
            ));
        }
    }

    @Override
    public Page getPage(String path, String id) {
        Page page = getPageVersion(new ObjectId(id));
        if (page != null) {
            return page;
        }
        else {
            return new PageImpl(mob(
                    Page.PATH, path,
                    Page.CONTENT, String.format("<a href=\"%s\">%s</a>", path, path)
            ));
        }
    }

    @Override
    public void approve(String path, String idString) {
        ObjectId id = new ObjectId(idString);
        Page approved = getPageVersion(id);
        if (approved != null && approved.getId().equals(id)) {
            for (Page page : getVersionPages(path)) {
                if (!page.getId().equals(id)) {
                    page.remove();
                }
            }
        }
    }

    @Override
    public void setHidden(String path, boolean hidden) {
        for (Page page : getVersionPages(path)) {
            page.setHidden(hidden);
        }
    }

    @Override
    public List<Page> getPageVersions(String path) {
        return getVersionPages(path);
    }

    @Override
    public byte[] getImage(String path) {
        DBObject object = images().findOne(mob(Page.PATH, path));
        if (object != null) {
            return (byte[]) object.get(Page.CONTENT);
        }
        else {
            return null;
        }
    }

    @Override
    public void deleteImage(String path) {
        DBObject object = images().findOne(mob(Page.PATH, path));
        if (object != null) {
            images().remove(object);
        }
    }

    @Override
    public Page createPage(String path) {
        Page page = getLatestPage(path);
        if (page == null) {
            page = new PageImpl(mob(Page.PATH, path));
        }
        return page;
    }

    @Override
    public boolean setPagePath(String oldPath, String newPath) {
        DBObject object = pages().findOne(mob(Page.PATH, oldPath));
        if (object != null) {
            object.put(Page.PATH, newPath);
            pages().save(object);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public void putImage(String path, byte[] content) {
        DBObject object = images().findOne(mob(Page.PATH, path));
        if (object != null) {
            object.put(Page.CONTENT, content);
            images().save(object);
        }
        else {
            images().insert(mob(
                    Page.PATH, path,
                    Page.CONTENT, content
            ));
        }
    }

    @Override
    public boolean setImagePath(String oldPath, String newPath) {
        DBObject object = images().findOne(mob(Page.PATH, oldPath));
        if (object != null) {
            object.put(Page.PATH, newPath);
            images().save(object);
            return true;
        }
        else {
            return false;
        }
    }

    public class PageImpl implements Page {
        private DBObject object;

        public PageImpl(DBObject object) {
            this.object = object;
        }

        @Override
        public ObjectId getId() {
            return (ObjectId) object.get(MONGO_ID);
        }

        @Override
        public String getPath() {
            return "/" + portalName + "/" + object.get(PATH);
        }

        @Override
        public boolean isHidden() {
            Boolean hidden = (Boolean) object.get(HIDDEN);
            return hidden != null && hidden;
        }

        @Override
        public void setHidden(boolean hidden) {
            object.put(HIDDEN, hidden);
            pages().save(object);
        }

        @Override
        public String getMenuName() {
            return (String) object.get(MENU_NAME);
        }

        @Override
        public int getMenuPriority() {
            return (Integer) object.get(MENU_PRIORITY);
        }

        @Override
        public String getTitle(Locale locale) {
            String title = (String) object.get(localeTitle(locale));
            if (title == null) {
                title = (String) object.get(TITLE);
            }
            if (title == null) {
                title = "";
            }
            return title;
        }

        @Override
        public String getContent(Locale locale) {
            String content = (String) object.get(localeContent(locale));
            if (content == null) {
                content = (String) object.get(CONTENT);
            }
            if (content == null) {
                content = "";
            }
            return content;
        }

        @Override
        public Date getDate() {
            ObjectId id = (ObjectId) object.get(MONGO_ID);
            if (id == null) {
                throw new IllegalStateException("Object has no _id field!");
            }
            return new Date(getId().getTime());
        }

        @Override
        public void setContent(String title, String content, Locale locale) {
            BasicDBObject fresh = copyObject();
            if (locale != null) {
                fresh.put(localeTitle(locale), title);
                fresh.put(localeContent(locale), content);
                if (DEFAULT_LANGUAGE.equals(locale.getLanguage())) {
                    fresh.put(TITLE, title);
                    fresh.put(CONTENT, content);
                }
            }
            else {
                fresh.put(TITLE, title);
                fresh.put(CONTENT, content);
            }
            pages().insert(fresh);
            this.object = fresh;
        }

        @Override
        public void setMenu(String menuName, int menuPriority) {
            object.put(MENU_NAME, menuName);
            object.put(MENU_PRIORITY, menuPriority);
            pages().save(object);
        }

        public void remove() {
            pages().remove(object);
        }

        private BasicDBObject copyObject() {
            BasicDBObject fresh = mob();
            for (String key : object.keySet()) {
                if (!key.equals(MONGO_ID)) {
                    fresh.put(key, object.get(key));
                }
            }
            return fresh;
        }

    }

    // === private

    private Page getPageVersion(ObjectId id) {
        DBObject object = pages().findOne(mob(MONGO_ID, id));
        if (object != null) {
            return new PageImpl(object);
        }
        else {
            return null;
        }
    }

    private Page getLatestPage(String path) {
        DBCursor cursor = pages().find(mob(Page.PATH, path)).sort(mob(MONGO_ID, -1)).limit(1);
        if (cursor.hasNext()) {
            return new PageImpl(cursor.next());
        }
        else {
            return null;
        }
    }

    private List<Page> getVersionPages(String path) {
        DBCursor cursor = pages().find(mob(Page.PATH, path)).sort(mob(MONGO_ID, -1));
        List<Page> list = new ArrayList<Page>();
        while (cursor.hasNext()) {
            list.add(new PageImpl(cursor.next()));
        }
        return list;
    }

    private Set<String> getPathSet(String collection) {
        PortalTheme portalTheme = ThemeInterceptor.getTheme();
        String collectionName = portalTheme == null ? collection : String.format("%s_%s",collection, portalTheme.getName());
        DBCollection coll = database().getCollection(collectionName);
        coll.ensureIndex(mob(Page.PATH, 1));
        DBCursor cursor = coll.find();
        Set<String> set = new TreeSet<String>();
        while (cursor.hasNext()) {
            DBObject pageObject = cursor.next();
            set.add("/" + portalName + "/" + pageObject.get(Page.PATH));
        }
        return set;
    }

    private DBCollection pages() {
        PortalTheme portalTheme = ThemeInterceptor.getTheme();
        String collectionName = portalTheme == null ? PAGES_COLLECTION : String.format("%s_%s",PAGES_COLLECTION, portalTheme.getName());
        return database().getCollection(collectionName);
    }

    private DBCollection images() {
        PortalTheme portalTheme = ThemeInterceptor.getTheme();
        String collectionName = portalTheme == null ? IMAGES_COLLECTION : String.format("%s_%s",IMAGES_COLLECTION, portalTheme.getName());
        return database().getCollection(collectionName);
    }

    private DB database() {
        return mongoFactory.getMongo().getDB(databaseName);
    }

    private Map<String, Page> getLatestPages() {
        DBCursor cursor = pages().find();
        Map<String, Page> latestPages = new TreeMap<String, Page>();
        while (cursor.hasNext()) {
            DBObject next = cursor.next();
            Page page = new PageImpl(next);
            if (!page.isHidden()) {
                Page existing = latestPages.get(page.getPath());
                if (existing == null || existing.getDate().compareTo(page.getDate()) < 0) {
                    latestPages.put(page.getPath(), page);
                }
            }
        }
        return latestPages;
    }

    private String localeContent(Locale locale) {
        if (locale == null) {
            return Page.CONTENT;
        }
        else {
            return String.format("%s_%s", Page.CONTENT, locale.getLanguage());
        }
    }

    private String localeTitle(Locale locale) {
        if (locale == null) {
            return Page.TITLE;
        }
        else {
            return String.format("%s_%s", Page.TITLE, locale.getLanguage());
        }
    }
}
