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

package eu.delving.web.controller;

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
import java.util.List;

/**
 * Serve up pages from MongoDB
 *
 * @author Gerald de Jong <gerald@delving.eu>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

@Controller
public class StaticController {

    @Autowired
    private StaticRepo staticRepo;

    @Autowired
    private ClickStreamLogger clickStreamLogger;

    @RequestMapping(value = "/**/*.dml", method = RequestMethod.GET)
    public Object fetchStaticPage(
            @RequestParam(required = false) String version,
            @RequestParam(required = false) boolean embedded,
            @RequestParam(required = false) boolean edit,
            @RequestParam(required = false) boolean approve,
            HttpServletRequest request
    ) {
        ModelAndView mav = ControllerUtil.createModelAndViewPage("static-page");
        String uri = request.getRequestURI();
        if (uri.endsWith("/_.dml") && isEditor()) {
            mav.addObject("pagePathList", staticRepo.getPagePaths());
        }
        else {
            if (isEditor()) {
                if (approve) {
                    staticRepo.approve(uri, version);
                }
                mav.addObject("edit", edit);
                mav.addObject("imagePathList", staticRepo.getImagePaths());
                List<StaticRepo.Page> versionList = staticRepo.getPageVersions(uri);
                if (versionList.size() > 1) {
                    mav.addObject("versionList", staticRepo.getPageVersions(uri));
                }
            }
            StaticRepo.Page page = version != null ? staticRepo.getPage(uri, version) : staticRepo.getPage(uri);
            clickStreamLogger.logCustomUserAction(request, ClickStreamLogger.UserAction.STATICPAGE, "view=" + uri);
            mav.addObject("page", page);
            mav.addObject("embedded", embedded);
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
            if (content != null && content.trim().isEmpty()) {
                content = null;
            }
            staticRepo.putPage(uri, content);
        }
        int slash = uri.indexOf('/', 1);
        String redirect = uri.substring(slash);
        return String.format("redirect:%s?edit=false", redirect);
    }

    @RequestMapping(value = {"/**/*.jpg.img", "/**/*.png.img", "/**/*.gif.img", "/**/_.img"}, method = RequestMethod.GET)
    public Object fetchImagePage(
            @RequestParam(required = false) boolean javascript,
            @RequestParam(required = false) Boolean edit,
            HttpServletRequest request
    ) {
        String uri = request.getRequestURI();
        if (uri.endsWith("/_.img") && isEditor()) {
            ModelAndView mav = ControllerUtil.createModelAndViewPage("static-image");
            mav.addObject("javascript", javascript);
            mav.addObject("imagePathList", staticRepo.getImagePaths());
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
            byte[] image = staticRepo.getImage(uri);
            if (image == null) {
                int slash = uri.indexOf('/', 1);
                String redirect = uri.substring(slash);
                return String.format("redirect:%s?edit=false", redirect);
            }
            return new ResponseEntity<byte[]>(
                    image,
                    headers,
                    HttpStatus.OK
            );
        }
        else {
            ModelAndView mav = ControllerUtil.createModelAndViewPage("static-image");
            mav.addObject("imageExists", staticRepo.getImage(uri) != null);
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
            staticRepo.putImage(uri, file.getBytes());
        }
        int slash = uri.indexOf('/', 1);
        String redirect = uri.substring(slash);
        return String.format("redirect:%s?edit=false", redirect);
    }

    private boolean isEditor() {
        User user = ControllerUtil.getUser();
        return user != null && (user.getRole() == Role.ROLE_ADMINISTRATOR || user.getRole() == Role.ROLE_GOD);
    }

}