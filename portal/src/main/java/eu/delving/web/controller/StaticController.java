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
import org.springframework.beans.factory.annotation.Value;
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
import java.net.URLEncoder;
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

    @Value("#{launchProperties['portal.name']}")
    private String portalName;

    @RequestMapping(value = "/**/*.dml", method = RequestMethod.GET)
    public Object fetchStaticPage(
            @RequestParam(required = false) String version,
            @RequestParam(required = false) boolean embedded,
            @RequestParam(required = false) boolean edit,
            @RequestParam(required = false) boolean delete,
            @RequestParam(required = false) boolean approve,
            @RequestParam(required = false) String newPath,
            HttpServletRequest request
    ) throws IOException {
        ModelAndView mav = ControllerUtil.createModelAndViewPage("static-page");
        String path = getPath(request);
        if (path.endsWith("_.dml")) {
            if (isEditor()) {
                mav.addObject("pagePathList", staticRepo.getPagePaths());
            }
            else {
                return getRedirect("");
            }
        }
        else {
            if (isEditor()) {
                if (newPath != null) {
                    int extensionLength = ".dml".length();
                    if (newPath.length() < extensionLength + 1) {
                        throw new IOException("new path too short: " + newPath);
                    }
                    String newExtension = newPath.substring(newPath.length() - extensionLength);
                    String oldExtension = path.substring(path.length() - extensionLength);
                    if (!oldExtension.equals(newExtension)) {
                        throw new IOException("Attempt to change extension to " + newExtension);
                    }
                    if (isEditor()) {
                        staticRepo.setPagePath(path, newPath);
                    }
                    return getRedirect(newPath);
                }
                else if (approve) {
                    staticRepo.approve(path, version);
                }
                else if (delete) {
                    staticRepo.putPage(path, null);
                }
                mav.addObject("edit", edit);
                mav.addObject("imagePathList", staticRepo.getImagePaths());
                List<StaticRepo.Page> versionList = staticRepo.getPageVersions(path);
                if (versionList.size() > 1) {
                    mav.addObject("versionList", staticRepo.getPageVersions(path));
                }
            }
            StaticRepo.Page page = version != null ? staticRepo.getPage(path, version) : staticRepo.getPage(path);
            clickStreamLogger.logCustomUserAction(request, ClickStreamLogger.UserAction.STATICPAGE, "view=" + path);
            mav.addObject("page", page);
            mav.addObject("embedded", embedded);
        }
        return mav;
    }

    @RequestMapping(value = "/**/*.dml", method = RequestMethod.POST)
    public String createStaticPage(
            String content,
            HttpServletRequest request
    ) throws IOException {
        String path = getPath(request);
        if (isEditor()) {
            if (content != null && content.trim().isEmpty()) {
                content = null;
            }
            staticRepo.putPage(path, content);
        }
        return getRedirect(path);
    }

    @RequestMapping(value = {"/**/*.jpg.img", "/**/*.png.img", "/**/*.gif.img", "/**/_.img"}, method = RequestMethod.GET)
    public Object fetchImagePage(
            @RequestParam(required = false) boolean javascript,
            @RequestParam(required = false) Boolean edit,
            @RequestParam(required = false) boolean delete,
            HttpServletRequest request
    ) throws IOException {
        String path = getPath(request);
        if (path.endsWith("_.img")) {
            if (isEditor()) {
                ModelAndView mav = ControllerUtil.createModelAndViewPage("static-image");
                mav.addObject("javascript", javascript);
                mav.addObject("imagePathList", staticRepo.getImagePaths());
                return mav;
            }
            else {
                return getRedirect("");
            }
        }
        else if (edit == null) {
            MediaType mediaType;
            if (path.endsWith(".jpg.img")) {
                mediaType = MediaType.IMAGE_JPEG;
            }
            else if (path.endsWith(".png.img")) {
                mediaType = MediaType.IMAGE_PNG;
            }
            else if (path.endsWith(".gif.img")) {
                mediaType = MediaType.IMAGE_GIF;
            }
            else {
                return getRedirect("_.img");
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            byte[] image = staticRepo.getImage(path);
            if (image == null) {
                return getRedirect(path);
            }
            return new ResponseEntity<byte[]>(
                    image,
                    headers,
                    HttpStatus.OK
            );
        }
        else {
            ModelAndView mav = ControllerUtil.createModelAndViewPage("static-image");
            mav.addObject("imagePath", path);
            if (isEditor()) {
                mav.addObject("edit", edit);
                if (delete) {
                    staticRepo.deleteImage(path);
                    mav.addObject("imageExists", false);
                }
                else {
                    mav.addObject("imageExists", staticRepo.getImage(path) != null);
                }
            }
            else {
                mav.addObject("imageExists", staticRepo.getImage(path) != null);
            }
            return mav;
        }
    }

    @RequestMapping(value = {"/**/*.jpg.img", "/**/*.png.img", "/**/*.gif.img", "/**/_.img"}, method = RequestMethod.POST)
    public String renameImage(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "newPath", required = false) String newPath,
            HttpServletRequest request
    ) throws IOException {
        String path = getPath(request);
        if (!isEditor()) {
            return getRedirect("");
        }
        if (newPath != null) {
            int extensionLength = ".???.img".length();
            if (newPath.length() < extensionLength + 1) {
                throw new IOException("new path too short: " + newPath);
            }
            String newExtension = newPath.substring(newPath.length() - extensionLength);
            String oldExtension = path.substring(path.length() - extensionLength);
            if (!oldExtension.equals(newExtension)) {
                throw new IOException("Attempt to change extension to " + newExtension);
            }
            if (!newPath.isEmpty() && isEditor()) {
                staticRepo.setImagePath(path, newPath);
            }
            return getRedirect(newPath) + "?edit=false";
        }
        else if (!file.isEmpty()) {
            MediaType mediaType = MediaType.parseMediaType(file.getContentType());
            String extension;
            if (MediaType.IMAGE_JPEG.isCompatibleWith(mediaType)) {
                extension = ".jpg.img";
            }
            else if (MediaType.IMAGE_PNG.isCompatibleWith(mediaType)) {
                extension = ".png.img";
            }
            else if (MediaType.IMAGE_GIF.isCompatibleWith(mediaType)) {
                extension = ".gif.img";
            }
            else {
                throw new IOException("Image not compatible with JPG, PNG, or GIF");
            }
            if (path.endsWith("_.img")) {
                String fileName = URLEncoder.encode(file.getOriginalFilename(),"utf-8");
                int dot = fileName.lastIndexOf(".");
                if (dot < 0) {
                    throw new IOException("File name must have extension: " + fileName);
                }
                fileName = fileName.substring(0, dot) + extension;
                int slash = path.lastIndexOf("/");
                String imagePath = path.substring(0, slash + 1) + fileName;
                staticRepo.putImage(imagePath, file.getBytes());
                return getRedirect(imagePath) + "?edit=true";
            }
            else {
                staticRepo.putImage(path, file.getBytes());
                return getRedirect(path) + "?edit=true";
            }
        }
        else {
            return getRedirect("_.img");
        }

    }

    private boolean isEditor() {
        User user = ControllerUtil.getUser();
        return user != null && (user.getRole() == Role.ROLE_ADMINISTRATOR || user.getRole() == Role.ROLE_GOD);
    }

    private String getRedirect(String path) {
        return String.format("redirect:/%s", path);
    }

    private String getPath(HttpServletRequest request) throws IOException {
        String uri = request.getRequestURI();
        String start = String.format("/%s/", portalName);
        if (!uri.startsWith(start)) {
            throw new IOException("URI expected to begin with " + start);
        }
        return uri.substring(start.length());
    }

}