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

package eu.europeana.core.database.dao;

import eu.europeana.core.database.AnnotationDao;
import eu.europeana.core.database.domain.Annotation;
import eu.europeana.core.database.domain.AnnotationType;
import eu.europeana.core.database.domain.EuropeanaId;
import eu.europeana.core.database.domain.User;
import eu.europeana.core.database.exception.AnnotationHasBeenModifiedException;
import eu.europeana.core.database.exception.AnnotationNotFoundException;
import eu.europeana.core.database.exception.AnnotationNotOwnedException;
import eu.europeana.core.database.exception.EuropeanaUriNotFoundException;
import eu.europeana.core.database.exception.UserNotFoundException;
import eu.europeana.definitions.domain.Language;
import eu.europeana.fixture.DatabaseFixture;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.fail;

/**
 * Test the AnnotationDao methods
 *
 * @author "Gerald de Jong" <geralddejong@gmail.com>
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "/core-application-context.xml",
        "/test-application-context.xml"
})

@Ignore("needs a reference to europeana.properties test file")
@Transactional
public class TestAnnotationDao {
    private Logger log = Logger.getLogger(TestAnnotationDao.class);

    @Autowired
    private AnnotationDao annotationDao;

    @Autowired
    private DatabaseFixture databaseFixture;

    private List<User> users;
    private List<EuropeanaId> europeanaIds;
    private Annotation predecessor, successor;

    @Before
    public void prepare() throws IOException {
        users = databaseFixture.createUsers("Gumby", 100);
        europeanaIds = databaseFixture.createEuropeanaIds("Test Collection", 10);
        log.info("created " + europeanaIds.size() + " europeanaids");
        databaseFixture.flush();
    }

    @After
    public void cleanup() {
        log.info("cleanup");
    }

    @Test
    public void createFresh() throws UserNotFoundException {
        log.info("createFresh");
        predecessor = annotationDao.create(
                users.get(0),
                AnnotationType.IMAGE_ANNOTATION,
                europeanaIds.get(0).getEuropeanaUri(),
                Language.EN,
                "predecessor content"
        );
        assertNotNull(predecessor);
        databaseFixture.flush();
    }

    @Test
    public void createSuccessor() throws AnnotationHasBeenModifiedException, AnnotationNotFoundException, UserNotFoundException {
        log.info("start createSuccessor");
        createFresh();
        assertNotNull(predecessor);
        successor = annotationDao.create(
                users.get(1),
                predecessor.getId(),
                "successor content"
        );
        assertNotNull(predecessor);
        log.info("finished createSuccessor");
        databaseFixture.flush();
    }

    @Test
    public void deletePredecessorFail() throws AnnotationNotFoundException, UserNotFoundException, AnnotationNotOwnedException, AnnotationHasBeenModifiedException {
        createSuccessor();
        try {
            annotationDao.delete(users.get(0), predecessor.getId());
            fail();
        }
        catch (AnnotationHasBeenModifiedException e) {
            log.info("Got the expected exception", e);
        }
    }

    @Test
    public void deleteSuccessorFailWrongUser() throws AnnotationNotFoundException, UserNotFoundException, AnnotationNotOwnedException, AnnotationHasBeenModifiedException {
        createSuccessor();
        try {
            successor = annotationDao.delete(users.get(0), successor.getId());
            fail();
        }
        catch (AnnotationNotOwnedException e) {

        }
        assertEquals("successor content", successor.getContent());
    }

    @Test
    public void deleteSuccessor() throws AnnotationNotFoundException, UserNotFoundException, AnnotationNotOwnedException, AnnotationHasBeenModifiedException {
        createSuccessor();
        successor = annotationDao.delete(users.get(1), successor.getId());
        assertEquals("successor content", successor.getContent());
    }

    @Test
    public void updateAndGet() throws UserNotFoundException, AnnotationHasBeenModifiedException, AnnotationNotFoundException, AnnotationNotOwnedException {
        createFresh();
        Annotation updated = annotationDao.update(users.get(0), predecessor.getId(), "fresh content");
        assertEquals("fresh content", updated.getContent());
        assertNotSame(updated.getId(), predecessor.getId());
        databaseFixture.flush();
        Annotation fetched = annotationDao.get(updated.getId());
        assertEquals(fetched.getId(), updated.getId());
    }

    @Test
    public void list() throws AnnotationHasBeenModifiedException, AnnotationNotFoundException, UserNotFoundException, EuropeanaUriNotFoundException {
        createSuccessor();
        List<Long> ids = annotationDao.list(AnnotationType.IMAGE_ANNOTATION, europeanaIds.get(0).getEuropeanaUri());
        assertEquals(2, ids.size());
    }
}