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
import org.apache.log4j.Logger;

import java.util.List;

/**
 * The implementation of the annotation dao
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Christian Sadilek <christian.sadilek@gmail.com>
 */

public class AnnotationDaoImpl implements AnnotationDao {
    private Logger log = Logger.getLogger(getClass());

    @Override
    public Annotation create(UserRepo.Person user, Long predecessorId, String content) throws AnnotationNotFoundException, UserNotFoundException, AnnotationHasBeenModifiedException {
        /*
        Long userId = user.getId();
        user = entityManager.find(UserRepo.Person.class, userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        Annotation predecessor = entityManager.find(Annotation.class, predecessorId);
        if (predecessor == null) {
            throw new AnnotationNotFoundException(predecessorId);
        }
        try {
            entityManager.lock(predecessor, LockModeType.WRITE);
            entityManager.refresh(predecessor);
        }
        catch (OptimisticLockException e) {
            throw new AnnotationHasBeenModifiedException(predecessorId, e);
        }
        
        Annotation annotation = new Annotation();
        annotation.setType(predecessor.getType());
        annotation.setLanguage(predecessor.getLanguage());
//        annotation.setEuropeanaId(predecessor.getEuropeanaId());
//        annotation.setUser(user);
        annotation.setContent(content);
        annotation.setDateSaved(new Date());
        annotation.setPredecessorId(predecessorId);
        entityManager.persist(annotation);
        log.info("Created annotation " + annotation.getId() + " dependent on " + predecessor.getId());
        return annotation;
        */
        return null;
    }

    @Override
    public Annotation create(UserRepo.Person user, AnnotationType annotationType, String europeanaUri, Language language, String content) throws UserNotFoundException {
        /*
        Long userId = user.getId();
        user = entityManager.find(UserRepo.Person.class, userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        Query query = entityManager.createQuery("select id from EuropeanaId id where id.europeanaUri = :europeanaUri");
        query.setParameter("europeanaUri", europeanaUri);
        EuropeanaId europeanaId = (EuropeanaId) query.getSingleResult();
        Annotation annotation = new Annotation();
        annotation.setType(annotationType);
        annotation.setUser(user);
        annotation.setLanguage(language);
        annotation.setEuropeanaId(europeanaId);
        annotation.setContent(content);
        annotation.setDateSaved(new Date());
        entityManager.persist(annotation);
        return annotation;
        */
        return null;
    }

    @Override
    public Annotation update(UserRepo.Person user, Long annotationId, String content) throws AnnotationNotFoundException, AnnotationNotOwnedException, AnnotationHasBeenModifiedException {
        /*
        Annotation existing = entityManager.find(Annotation.class, annotationId);
        if (existing == null) {
            throw new AnnotationNotFoundException(annotationId);
        }
        entityManager.lock(existing, LockModeType.WRITE);
        entityManager.refresh(existing);
        if (!existing.getChildren().isEmpty()) {
            throw new AnnotationHasBeenModifiedException(existing.getId());
        }
        if (!existing.getUser().getId().equals(user.getId())) {
            throw new AnnotationNotOwnedException(existing.getId(), user);
        }
        Annotation fresh = new Annotation();
        // copy from existing
        fresh.setEuropeanaId(existing.getEuropeanaId());
        fresh.setLanguage(existing.getLanguage());
        fresh.setPredecessorId(existing.getPredecessorId());
        fresh.setType(existing.getType());
        fresh.setUser(existing.getUser());
        // new stuff
        fresh.setDateSaved(new Date());
        fresh.setContent(content);
        entityManager.remove(existing);
        entityManager.persist(fresh);
        return fresh;
        */
        return null;
    }

    @Override
    public Annotation delete(UserRepo.Person user, Long annotationId) throws AnnotationNotOwnedException, AnnotationNotFoundException, AnnotationHasBeenModifiedException {
        /*
        Annotation existing = entityManager.find(Annotation.class, annotationId);
        if (existing == null) {
            throw new AnnotationNotFoundException(annotationId);
        }
        entityManager.lock(existing, LockModeType.WRITE);
        entityManager.refresh(existing);
        if (!existing.getChildren().isEmpty()) {
            throw new AnnotationHasBeenModifiedException(existing.getId());
        }
        if (!existing.getUser().getId().equals(user.getId())) {
           throw new AnnotationNotOwnedException(existing.getId(), user);
        }
        entityManager.remove(existing);
        return existing;
        */
        return null;
    }

    @Override
    public Annotation get(Long annotationId) throws AnnotationNotFoundException {
        return null;
    }
 
    @Override
    @SuppressWarnings("unchecked")
    public List<Long> list(AnnotationType annotationType, String europeanaUri) throws EuropeanaUriNotFoundException {
        /*
        Query idQuery = entityManager.createQuery("select id from EuropeanaId id where id.europeanaUri = :europeanaUri");
        idQuery.setParameter("europeanaUri", europeanaUri);
        try {
            EuropeanaId europeanaId = (EuropeanaId) idQuery.getSingleResult();
            Query query = entityManager.createQuery("select a.id from Annotation a where a.europeanaId = :europeanaId");
            query.setParameter("europeanaId", europeanaId);
            return (List<Long>) query.getResultList();
        }
        catch (NoResultException e) {
            throw new EuropeanaUriNotFoundException(europeanaUri);
        }
        */
        return null;
    }
}
