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

import java.io.IOException;
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

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = "/{type}/{europeanaUri}", params = "!predecessor", method = RequestMethod.POST)
    public @ResponseBody String createFresh(
            @PathVariable String type,
            @PathVariable String europeanaUri,
            Locale locale,
            String content
    ) throws UserNotFoundException {
        AnnotationType annotationType = AnnotationType.valueOf(type);
        Language language = Language.findByCode(locale.getLanguage());
        Annotation fresh = annotationDao.create(getUser(), annotationType, europeanaUri, language, content);
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
        List<Long> ids = annotationDao.list(annotationType, europeanaUri);
        StringBuilder out = new StringBuilder();
        for (Long id : ids) {
            out.append(id).append('\n');
        }
        return out.toString();
    }

    // ===== exceptions ======

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({AnnotationNotFoundException.class})
    public void notFoundAnnotation() {
        log.warn("problem");
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({EuropeanaUriNotFoundException.class})
    public void notFoundId() {
         log.warn("problem");
    }

    @ExceptionHandler({AnnotationHasBeenModifiedException.class})
    @ResponseStatus(HttpStatus.CONFLICT)
    public void hasBeenModified() {
        log.warn("problem");
    }

    @ExceptionHandler({AnnotationNotOwnedException.class})
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void notOwned() {
        log.warn("problem");
    }

    @ExceptionHandler({UserNotFoundException.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void userNotFound() {
        log.warn("problem");
    }

    @ExceptionHandler({Throwable.class})
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void unknownProblem() {
        log.warn("problem");
    }

    private User getUser() throws UserNotFoundException {
        User user = ControllerUtil.getUser();
        if (user == null) {
            throw new UserNotFoundException();
        }
        return user;
    }
}