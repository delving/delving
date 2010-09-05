/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
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

package eu.europeana.api.controller;

import eu.europeana.core.database.AnnotationDao;
import eu.europeana.core.database.domain.Annotation;
import eu.europeana.core.database.domain.AnnotationType;
import eu.europeana.core.database.domain.User;
import eu.europeana.core.database.exception.AnnotationHasBeenModifiedException;
import eu.europeana.core.database.exception.AnnotationNotFoundException;
import eu.europeana.core.database.exception.AnnotationNotOwnedException;
import eu.europeana.core.database.exception.EuropeanaUriNotFoundException;
import eu.europeana.core.database.exception.UserNotFoundException;
import eu.europeana.core.util.web.ControllerUtil;
import eu.europeana.definitions.domain.Language;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Locale;

/**
 * Handle the rest interface for storing and retrieving annotations
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Christian Sadilek <christian.sadilek@gmail.com>
 */

@Controller
@RequestMapping("/annotation")
public class AnnotationController {

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private AnnotationDao annotationDao;

    @RequestMapping
    public ModelAndView legend() {
        return ControllerUtil.createModelAndViewPage("annotation");
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/{type}/{europeanaUri}", params = "!predecessor", method = RequestMethod.POST)
    public @ResponseBody String createFresh(
            @PathVariable String type,
            @PathVariable String europeanaUri,
            Locale locale,
            @RequestBody String content
    ) throws UserNotFoundException, UnsupportedEncodingException {
        AnnotationType annotationType = AnnotationType.valueOf(type);
        String decoded = URLDecoder.decode(europeanaUri, "UTF-8");
        Language language = Language.findByCode(locale.getLanguage());
        Annotation fresh = annotationDao.create(getUser(), annotationType, decoded, language, content);
        return fresh.getId().toString();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(params = "predecessor", method = RequestMethod.POST)
    public @ResponseBody String createDependent(
            @RequestParam Long predecessor,
            @RequestBody String content
    ) throws AnnotationNotFoundException, UserNotFoundException, AnnotationHasBeenModifiedException {
        Annotation fresh = annotationDao.create(getUser(), predecessor, content);
        return fresh.getId().toString();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public @ResponseBody String update(
            @PathVariable Long id,
            @RequestBody String content
    ) throws AnnotationNotFoundException, AnnotationNotOwnedException, AnnotationHasBeenModifiedException, UserNotFoundException {
        Annotation fresh = annotationDao.update(getUser(), id, content);
        return fresh.getId().toString();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public @ResponseBody String delete(
            @PathVariable Long id
    ) throws AnnotationNotOwnedException, AnnotationNotFoundException, AnnotationHasBeenModifiedException, UserNotFoundException {
        Annotation dead = annotationDao.delete(getUser(), id);
        return dead.getId().toString();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public @ResponseBody String get(
            @PathVariable Long id
    ) throws IOException, AnnotationNotFoundException {
        Annotation annotation = annotationDao.get(id);
        return annotation.getContent();
    }

    @RequestMapping(value = "/{type}/{europeanaUri}", method = RequestMethod.GET)
    public @ResponseBody String list(
            @PathVariable String europeanaUri,
            @PathVariable String type
    ) throws IOException, EuropeanaUriNotFoundException {
        AnnotationType annotationType = AnnotationType.valueOf(type);
        String decoded = URLDecoder.decode(europeanaUri, "UTF-8");
        List<Long> ids = annotationDao.list(annotationType, decoded);
        StringBuilder out = new StringBuilder();
        for (Long id : ids) {
            out.append(id).append('\n');
        }
        return out.toString();
    }

    // ===== exceptions ======

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({AnnotationNotFoundException.class, EuropeanaUriNotFoundException.class})
    public void notFoundAnnotation(Exception e) {
        log.warn("problem", e);
    }

    @ExceptionHandler({AnnotationHasBeenModifiedException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public void hasBeenModified(Exception e) {
        log.warn("problem", e);
    }

    @ExceptionHandler({AnnotationNotOwnedException.class, UserNotFoundException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void notOwned(Exception e) {
        log.warn("problem", e);
    }
    
    @ExceptionHandler({Throwable.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void unknownProblem(Exception e) {
        log.warn("problem", e);
    }

    private User getUser() throws UserNotFoundException {
        User user = ControllerUtil.getUser();
        if (user == null) {
            throw new UserNotFoundException();
        }
        return user;
    }
}
