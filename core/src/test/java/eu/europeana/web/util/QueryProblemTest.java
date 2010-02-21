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

package eu.europeana.web.util;

import eu.europeana.core.querymodel.query.EuropeanaQueryException;
import eu.europeana.core.querymodel.query.QueryProblem;
import junit.framework.Assert;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jul 25, 2009: 3:22:39 PM
 */
public class QueryProblemTest {
    private static final Logger log = Logger.getLogger(QueryProblemTest.class);

    @Test
    public void testGetMessage() {
        EuropeanaQueryException exception = new EuropeanaQueryException(QueryProblem.RECORD_NOT_FOUND.toString());
        QueryProblem problem = QueryProblem.get(exception.getMessage());
        Assert.assertEquals("Should be full-doc not found", QueryProblem.RECORD_NOT_FOUND, problem);
    }

}
