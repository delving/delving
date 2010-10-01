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
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import eu.europeana.core.database.domain.Role;
import eu.europeana.core.database.domain.User;
import eu.europeana.core.util.web.ClickStreamLogger;
import eu.europeana.core.util.web.ControllerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Serve up pages from MongoDB
 *
 * @author Gerald de Jong <gerald@delving.eu>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Controller
public class StaticPageController {
    private static final String DB_NAME = "StaticPages";
    private static final String PAGES_COLLECTION = "pages";
    private static final String IMAGES_COLLECTION = "images";
    private static final String CONTENT = "content";

    @Autowired
    private Mongo mongo;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @RequestMapping(value = "/**/*.dml", method = RequestMethod.GET)
    public ModelAndView fetchStaticPage(
            @RequestParam(required = false) Boolean edit,
            HttpServletRequest request
    ) {
        ModelAndView mav = ControllerUtil.createModelAndViewPage("static-page");
        String uri = request.getRequestURI();
        if (uri.endsWith("/_.dml")) {
            mav.addObject("pagePathList", getList(PAGES_COLLECTION));
        }
        else {
            String content = getString(uri);
            clickStreamLogger.logCustomUserAction(request, ClickStreamLogger.UserAction.STATICPAGE, "view=" + uri);
            mav.addObject("content", content == null ? "This page does not exist." : content);
            mav.addObject("pagePath", uri);
            if (isEditor()) {
                mav.addObject("edit", edit);
                mav.addObject("imagePathList", getList(IMAGES_COLLECTION));
            }
        }
        return mav;
    }

    @RequestMapping(value = "/**/*.dml", method = RequestMethod.POST)
    public String createStaticPage(
            String content,
            HttpServletRequest request
    ) {
        String uri = request.getRequestURI();
        if (isEditor()) {
            putString(uri, content);
        }
        int slash = uri.indexOf('/', 1);
        String redirect = uri.substring(slash);
        return String.format("redirect:%s?edit=false", redirect);
    }

    @RequestMapping(value = {"/**/*.jpg.img", "/**/*.png.img", "/**/*.gif.img", "/**/_.img"}, method = RequestMethod.GET)
    public Object fetchImagePage(
            @RequestParam(required = false) Boolean edit,
            HttpServletRequest request
    ) {
        String uri = request.getRequestURI();
        if (uri.endsWith("/_.img")) {
            ModelAndView mav = ControllerUtil.createModelAndViewPage("static-image");
            mav.addObject("imagePathList", getList(IMAGES_COLLECTION));
            return mav;
        }
        else if (edit == null) {
            MediaType mediaType;
            if (uri.endsWith(".jpg.img")) {
                mediaType = MediaType.IMAGE_JPEG;
            }
            else if (uri.endsWith(".png.img")) {
                mediaType = MediaType.IMAGE_PNG;
            }
            else if (uri.endsWith(".gif.img")) {
                mediaType = MediaType.IMAGE_GIF;
            }
            else {
                throw new RuntimeException();
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            byte [] image = getImage(uri);
            if (image != null) {
                return new ResponseEntity<byte[]>(
                        image,
                        headers,
                        HttpStatus.OK
                );
            }
            else {
                return new ResponseEntity<byte[]>(
                        new byte[] {},
                        headers,
                        HttpStatus.NOT_FOUND
                );
            }
        }
        else {
            ModelAndView mav = ControllerUtil.createModelAndViewPage("static-image");
            mav.addObject("imageExists", getImage(uri) != null);
            mav.addObject("imagePath", uri);
            if (isEditor()) {
                mav.addObject("edit", edit);
            }
            return mav;
        }
    }

    @RequestMapping(value = {"/**/*.jpg.img", "/**/*.png.img", "/**/*.gif.img"}, method = RequestMethod.POST)
    public String createImage(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request
    ) throws IOException {
        String uri = request.getRequestURI();
        if (!file.isEmpty() && isEditor()) {
            putImage(uri, file.getBytes());
        }
        int slash = uri.indexOf('/', 1);
        String redirect = uri.substring(slash);
        return String.format("redirect:%s?edit=false", redirect);
    }

    private boolean isEditor() {
        User user = ControllerUtil.getUser();
        return user != null && (user.getRole() == Role.ROLE_ADMINISTRATOR || user.getRole() == Role.ROLE_GOD);
    }

    private List<String> getList(String collection) {
        DBCursor cursor = db().getCollection(collection).find();
        List<String> list = new ArrayList<String>();
        while (cursor.hasNext()) {
            DBObject pageObject = cursor.next();
            list.add((String) pageObject.get("_id"));
        }
        return list;
    }

    private String getString(String path) {
        DBObject object = pages().findOne(new BasicDBObject("_id", path));
        return object == null ? null : (String) object.get(CONTENT);
    }

    private byte[] getImage(String path) {
        DBObject object = images().findOne(new BasicDBObject("_id", path));
        if (object != null) {
            return (byte []) object.get(CONTENT);
        }
        else {
            return null;
        }
    }

    private void putString(String path, String content) {
        DBObject object = pages().findOne(new BasicDBObject("_id", path));
        if (object != null) {
            object.put(CONTENT, content);
            pages().save(object);
        }
        else {
            object = new BasicDBObject("_id", path);
            object.put(CONTENT, content);
            pages().insert(object);
        }
    }

    private void putImage(String path, byte[] content) {
        DBObject object = images().findOne(new BasicDBObject("_id", path));
        if (object != null) {
            object.put(CONTENT, content);
            images().save(object);
        }
        else {
            object = new BasicDBObject("_id", path);
            object.put(CONTENT, content);
            images().insert(object);
        }
    }

    private DBCollection pages() {
        return db().getCollection(PAGES_COLLECTION);
    }

    private DBCollection images() {
        return db().getCollection(IMAGES_COLLECTION);
    }

    private DB db() {
        return mongo.getDB(DB_NAME);
    }

}