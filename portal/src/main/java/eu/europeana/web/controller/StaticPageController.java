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

package eu.europeana.web.controller;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import eu.europeana.core.database.domain.Role;
import eu.europeana.core.database.domain.User;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

/**
 * Serve up pages from MongoDB
 *
 * @author Gerald de Jong <gerald@delving.eu>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Controller
@RequestMapping("_{pageName}.html")
public class StaticPageController {
    private static final String DB_NAME = "StaticPages";
    private static final String COLLECTION_NAME = "pages";
    private static final String CONTENT = "content";

    @Autowired
    private Mongo mongo;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView fetchStaticPage(
            @PathVariable String pageName,
            @RequestParam(required = false) boolean edit,
            @RequestParam(required = false) boolean onlyContent,
            HttpServletRequest request
    ) {
        String content = getPage(pageName);
        clickStreamLogger.logCustomUserAction(request, ClickStreamLogger.UserAction.STATICPAGE, "view=" + pageName);
        ModelAndView mav = new ModelAndView("static-page", "content", content == null ? "This page does not exist." : content);
        mav.addObject("pageName", pageName);
        if (isEditor()) {
            mav.addObject("edit", edit);
        }
        if (onlyContent) {
            mav.addObject("onlyContent", true);
        }
        return mav;
    }

    @RequestMapping(method = RequestMethod.POST)
    public String createStaticPage(
            @PathVariable String pageName,
            String content
    ) {
        if (isEditor()) {
            putPage(pageName, content);
        }
        return String.format("redirect:_%s.html", pageName);
    }

    private boolean isEditor() {
        User user = ControllerUtil.getUser();
        return user != null && (user.getRole() == Role.ROLE_ADMINISTRATOR || user.getRole() == Role.ROLE_GOD);
    }


    private void putPage(String pageName, String content) {
        DBObject object = db().findOne(new BasicDBObject("_id", pageName));
        if (object != null) {
            object.put(CONTENT, content);
            db().save(object);
        }
        else {
            object = new BasicDBObject("_id", pageName);
            object.put(CONTENT, content);
            db().insert(object);
        }
    }

    private String getPage(String pageName) {
        DBObject object = db().findOne(new BasicDBObject("_id", pageName));
        return object == null ? null : (String) object.get(CONTENT);
    }

    private DBCollection db() {
        return mongo.getDB(DB_NAME).getCollection(COLLECTION_NAME);
    }

}