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
package eu.delving.core.storage;

import com.mongodb.Mongo;
import eu.delving.core.storage.impl.StaticRepoImpl;
import eu.delving.core.util.MongoFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Make sure the page repo is working
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TestStaticRepo {
    private static final String TEST_DB_NAME = "test-static";

    private Mongo mongo;

    private StaticRepoImpl repo;

    @Before
    public void before() throws Exception {
        repo = new StaticRepoImpl();
        final MongoFactory mongoFactory = new MongoFactory();
        mongoFactory.setTestContext("true");
        mongoFactory.afterPropertiesSet();
        repo.setMongoFactory(mongoFactory);
        mongo = mongoFactory.getMongo();
        repo.setDatabaseName(TEST_DB_NAME);
        repo.setPortalName(TEST_DB_NAME);
        mongo.dropDatabase(TEST_DB_NAME);
    }

    @After
    public void after() {
        mongo.dropDatabase(TEST_DB_NAME);
    }

    @Test
    public void putGet() {
        String path = "/test/path/gumby.dml";
        // insert a doc
        StaticRepo.Page page = repo.createPage(path);
        page.setContent("Gumby", "Gumby Rulez", null);
        Assert.assertEquals("Unable to fetch", "Gumby", repo.getPage(path).getTitle(null));
        Assert.assertEquals("Unable to fetch", "Gumby Rulez", repo.getPage(path).getContent(null));
        // insert a second for that path
        page.setContent(page.getTitle(null), "Gumby Really Rulez", null);
        Assert.assertEquals("Unable to fetch", "Gumby Really Rulez", repo.getPage(path).getContent(null));
        Assert.assertEquals("Should be 2 documents", 2, repo.getPageVersions(path).size());
        // insert a third for that path
        page.setContent(page.getTitle(null), "Gumby Really Truly Rulez", null);
        Assert.assertEquals("Unable to fetch", "Gumby Really Truly Rulez", repo.getPage(path).getContent(null));
        Assert.assertEquals("Should be 3 documents", 3, repo.getPageVersions(path).size());
        // remove the latest
        repo.getPage(path).remove();
        repo.getPage(path).remove();
        Assert.assertEquals("Should be 1 document", 1, repo.getPageVersions(path).size());
        // find the original
        Assert.assertEquals("Unable to fetch", "Gumby Rulez", repo.getPage(path).getContent(null));
    }

    @Test
    public void putGetLocale() {
        String path = "/test/path/gumby.dml";
        Locale locale = new Locale("no");
        // insert a doc
        StaticRepo.Page page = repo.createPage(path);
        page.setContent("Goober", "Gumby No Locale", null);
        page = repo.createPage(path);
        page.setContent("", "Gumby Rulez", locale);
        page = repo.getPage(path);
        repo.approve(path, page.getId().toString());
        Assert.assertEquals("Unable to fetch", "Gumby Rulez", repo.getPage(path).getContent(locale));
        Assert.assertEquals("Unable to fetch", "Gumby No Locale", repo.getPage(path).getContent(null));
        Assert.assertEquals("Should be 1 document", 1, repo.getPageVersions(path).size());
    }

    @Test
    public void menus() {
        for (int menuIndex = 0; menuIndex < 3; menuIndex++) {
            String menuName = "menu" + menuIndex;
            int priority = 10;
            for (int pageIndex = 0; pageIndex <= menuIndex + 5; pageIndex++) {
                String pageName = "page" + pageIndex + ".dml";
                String path = "path" + ((char) ('A' + menuIndex)) + "/" + pageName;
                StaticRepo.Page page = repo.createPage(path);
                page.setContent(pageName, "Booger", null);
                if (pageIndex % 2 == 0) {
                    page.setMenu(menuName, priority--);
                }
            }
        }
        Map<String, List<StaticRepo.MenuItem>> menus = repo.getMenus(null);
        Assert.assertEquals("Should be three", 3, menus.size());
        Assert.assertTrue("priorities wrong", menus.get("menu0").get(0).getMenuPriority() < menus.get("menu0").get(1).getMenuPriority());
        for (List<StaticRepo.MenuItem> menu : menus.values()) {
            System.out.println("menu " + menu.get(0).getMenuName());
            for (StaticRepo.MenuItem item : menu) {
                System.out.println(item.getPath() + " " + item.getMenuPriority());
            }
        }
    }
}
