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

package eu.europeana.database;

import eu.europeana.database.domain.DashboardLog;
import eu.europeana.database.domain.EuropeanaCollection;
import eu.europeana.database.domain.EuropeanaId;
import eu.europeana.database.domain.IndexingQueueEntry;

import java.util.List;

/**
 * This interface represents the contract for data access that the Dashboard application needs, above and beyond
 * what the other DAO objects offer.
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public interface DashboardDao {

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
     * @param collectionName the name of the collection
     * @param collectionFileName the file name, null is allowed. todo: this attribute can be removed when collection names ALWAYS depend on file names
     * @param createIfAbsent create the collection if it wasn't found
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
     * Update the contets of the collection
     *
     * @param collection with new field values
     * @return the updated version fresh from the database
     */

    EuropeanaCollection updateCollection(EuropeanaCollection collection);

    /**
     * This is a very radical move!  All collections are disabled and their new disabled versions are
     * returned so that something outside of the database can be done with these collections.  ie. remove from index.
     *
     * @return the collections after they are disabled
     */

    List<EuropeanaCollection> disableAllCollections();

    /**
     * Another radical method!  All collections are first disabled, and then they are all enabled which causes
     * work to be placed in the index queue.
     */

    void enableAllCollections();

    /**
     * Prepare a collection for importing by removing any saved import error and setting the date last modified.
     *
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
     * @param importError the string representing the error
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
     *
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
     *
     */

    List<EuropeanaId> fetchCollectionObjects(EuropeanaCollection collection);

    /**
     * Fetch all the entries out of the current queue, since each one of them contains information
     * about how far the process has come so far.  The indexing updates these entries, so this method
     * just fetches the latest news.
     *
     * @return all entries in the queues for indexing and cacheing
     */

    List<IndexingQueueEntry> fetchQueueEntries();

    /**
     * Indicate that the given collection is to be indexed
     *
     * @param collection the collection to be indexed
     * @return true if it was done, false if the collection was unknown
     */

    boolean addToIndexQueue(EuropeanaCollection collection);

    /**
     * Start indexing associated with the job stored on the queue
     *
     * @param indexingQueueEntry which job is it?
     * @return the updated entry
     */

    IndexingQueueEntry startIndexing(IndexingQueueEntry indexingQueueEntry);

    /**
     * Fetch a list of europeana ids which are in need of indexing.
     *
     * @param maxResults how big can the list be
     * @param indexingQueueEntry which entry are we talking about?
     * @return the list of to-index ids.
     */

    List<EuropeanaId> getEuropeanaIdsForIndexing(int maxResults, IndexingQueueEntry indexingQueueEntry);

    /**
     * When getEuropeanaIdsForIndexing returns an empty list, indicate with this method that the entry can be
     * removed from the queue.
     *
     * @param collection the collection should no longer be indexed
     */

    void removeFromIndexQueue(EuropeanaCollection collection);

    /**
     * Check if there is indexing work to do
     *
     * @return the top queue entry, or null if there's no work
     */

    IndexingQueueEntry getEntryForIndexing();

    /**
     * Update the status of the collection to show that another bunch of records have been indexed.  The queue is
     * polled elsewhere to show running status.
     *
     * @param count how many records processed this time around
     * @param indexingQueueEntry which job being worked on
     * @param lastEuropeanaId what was the last europeana id indexed
     */

    void saveRecordsIndexed(int count, IndexingQueueEntry indexingQueueEntry, EuropeanaId lastEuropeanaId);

    /**
     * Completely refresh the collection counters by doing some nasty queries to the database.
     *
     * @param collectionId which collection
     * @return the updated entity, with new counts
     */

    EuropeanaCollection updateCollectionCounters(Long collectionId);

    /**
     * Record in the audit log what the dashboard has just done, saving who did it and what they did
     *
     * @param who the username
     * @param what what did they do?
     */

    void log(String who, String what);

    /**
     * Fetch another bunch of log entries for display from the given id onwards
     *
     * @param bottomId where to start
     * @param pageSize maximum list size
     * @return the list of log entries
     */

    List<DashboardLog> fetchLogEntriesFrom(Long bottomId, int pageSize);

    /**
     * Fetch the log entries up to the given id
     *
     * @param topId where to end
     * @param pageSize maximum list size
     * @return the list of log entries
     */

    List<DashboardLog> fetchLogEntriesTo(Long topId, int pageSize);

}