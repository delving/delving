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

package eu.europeana.core.database;

import eu.europeana.core.database.domain.EuropeanaId;

/**
 * This interface represents the contract for data access that the Console application needs, above and beyond
 * what the other DAO objects offer.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Nicola Aloia   <nicola.aloia@isti.cnr.it>
 */

public interface ConsoleDao {

    /**
     * During importing, this method is used to record a newly-created europeana id entity,
     * or to add the ID to the database if it is an existing europeana id.
     * The check for an existing id is done by fetching on the basis of the europeana uri and the
     * collection (calling getEuropeanaId below).
     *
     * @param europeanaId internal identifier
     * @return the updated collection
     */

    EuropeanaId saveEuropeanaId(EuropeanaId europeanaId);

}