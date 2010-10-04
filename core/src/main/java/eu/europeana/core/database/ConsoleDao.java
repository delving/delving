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

import eu.europeana.core.database.domain.EuropeanaCollection;
import eu.europeana.core.database.domain.EuropeanaId;

import java.util.List;

/**
 * This interface represents the contract for data access that the Console application needs, above and beyond
 * what the other DAO objects offer.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Nicola Aloia   <nicola.aloia@isti.cnr.it>
 */

public interface ConsoleDao {

    /**
     * Fetch all of the collections
     *
     * @return a list
     */

    List<EuropeanaCollection> fetchCollections();

    /**
     * Fetch the collections for which the name begins with the given prefix
     *
     * @param prefix how each name must begin
     * @return a filtered list
     */

    List<EuropeanaCollection> fetchCollections(String prefix);

    /**
     * Try to find the collection based on the file name, and if that doesn't work use the
     * collection name, and change the file name to the new value
     *
     * @param collectionName     the name of the collection
     * @param collectionFileName the file name, null is allowed. todo: this attribute can be removed when collection names ALWAYS depend on file names
     * @param createIfAbsent     create the collection if it wasn't found
     * @return a europeana collection, or perhaps null if createIfAbsent is null and it wasn't found
     */

    EuropeanaCollection fetchCollection(String collectionName, String collectionFileName, boolean createIfAbsent);

    /**
     * Fetch the collection based on its internal id.  If it's absent there will be an exception.
     *
     * @param id the identifier
     * @return the collection
     */

    EuropeanaCollection fetchCollection(Long id);

    /**
     * Update the contents of the collection
     *
     * @param collection with new field values
     * @return the updated version fresh from the database
     */

    EuropeanaCollection updateCollection(EuropeanaCollection collection);

    /**
     * Prepare a collection for importing by removing any saved import error and setting the date last modified.
     * <p/>
     * note: originally the indexing was triggered using the last modified date. is that still the case?
     *
     * @param collectionId the internal identifier
     * @return the updated collection
     */

    EuropeanaCollection prepareForImport(Long collectionId);

    /**
     * Persist the import error that caused the termination of importing so that it can be viewed later.
     *
     * @param collectionId internal identifier
     * @param importError  the string representing the error
     * @return the updated collection
     */

    EuropeanaCollection setImportError(Long collectionId, String importError);

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

    /**
     * Fetch the current version of the existing europeana id.  Search using uri and collection.
     * <p/>
     * todo: is URI not sufficient? if so, the method below can be used
     *
     * @param europeanaId the id,
     * @return the existing id, or null if it was not found
     */

    EuropeanaId getEuropeanaId(EuropeanaId europeanaId);

    /**
     * Fetch the europeana id entity based only on the europeana uri
     *
     * @param europeanaUri the uri to search for
     * @return an instance of EuropeanaId class or null
     */

    EuropeanaId fetchEuropeanaId(String europeanaUri);

    /**
     * Fetch objects from a collection
     */

    List<EuropeanaId> fetchCollectionObjects(EuropeanaCollection collection);

    /**
     * Completely refresh the collection counters by doing some nasty queries to the database.
     *
     * @param collectionId which collection
     * @return the updated entity, with new counts
     */

    EuropeanaCollection updateCollectionCounters(Long collectionId);

    List<EuropeanaCollection> fetchEnabledCollections();
}