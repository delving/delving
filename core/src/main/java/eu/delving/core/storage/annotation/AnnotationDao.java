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

package eu.delving.core.storage.annotation;

import eu.delving.core.storage.UserRepo;
import eu.delving.domain.Language;

import java.util.List;

/**
 * This interface describes all the access that annotation tools should need for storing and retrieving
 * their stuff in our database.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Christian Sadilek <christian.sadilek@gmail.com>
 */

public interface AnnotationDao {


    /**
     * Create an annotation which depends on an existing one
     *
     * @param user who is doing this?
     * @param predecessorId another annotation that we depend on
     * @param content the content of the new annotation
     * @return the newly created annotation
     * @throws AnnotationNotFoundException the predecessor wasn't found
     * @throws UserNotFoundException the user in the session wasn't found
     * @throws AnnotationHasBeenModifiedException someone has changed the predecessor
     */

    Annotation create(UserRepo.Person user, Long predecessorId, String content) throws AnnotationNotFoundException, UserNotFoundException, AnnotationHasBeenModifiedException;

    /**
     * Create an annotation from scratch.
     *
     * @param user who is doing this?
     * @param annotationType which annotation tool are we dealing with?
     * @param europeanaUri which uri does this refer to?
     * @param language of this annotation
     * @param content the content of the annotation
     * @return the annotation that has just been stored
     * @throws UserNotFoundException the user in the session wasn't found
     */

    Annotation create(UserRepo.Person user, AnnotationType annotationType, String europeanaUri, Language language, String content) throws UserNotFoundException;

    /**
     * Update the content of an existing annotation
     *
     * @param user who is doing this?
     * @param annotationId the internal identifier of the annotation
     * @param content the new content
     * @return the updated annotation
     * @throws AnnotationNotFoundException don't know which one
     * @throws AnnotationNotOwnedException cannot modify it because we don't own it
     * @throws AnnotationHasBeenModifiedException somebody has changed this already
     */

    Annotation update(UserRepo.Person user, Long annotationId, String content) throws AnnotationNotFoundException, AnnotationNotOwnedException, AnnotationHasBeenModifiedException;

    /**
     * Remove an existing annotation
     *
     * @param user who is doing this?
     * @param annotationId which one? internal id.
     * @return the one that was removed
     * @throws AnnotationNotOwnedException cannot modify it because we don't own it
     * @throws AnnotationNotFoundException couldn't find it
     * @throws AnnotationHasBeenModifiedException somebody has changed this already
     */

    Annotation delete(UserRepo.Person user, Long annotationId) throws AnnotationNotOwnedException, AnnotationNotFoundException, AnnotationHasBeenModifiedException;

    /**
     * Fetch a single annotation
     *
     * @param annotationId which one?  internal id.
     * @return the annotation
     * @throws AnnotationNotFoundException couldn't find it
     */
    
    Annotation get(Long annotationId) throws AnnotationNotFoundException;

    /**
     * Fetch a (fairly small) list of annotations of the given type for the given object/
     *
     * @param annotationType which annotation tool are we dealing with?
     * @param europeanaUri which id's annotations do we want?
     * @return a list of internal identifiers which can be used with get() above
     */
    
    List<Long> list(AnnotationType annotationType, String europeanaUri) throws EuropeanaUriNotFoundException;
}