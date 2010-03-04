/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.0 or - as soon they
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

package eu.europeana.core.database.incoming.cache;

import eu.europeana.core.database.domain.EuropeanaCollection;

import java.io.File;

/**
 * Cache digital objects which can be fetched from remote sites
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public interface ObjectCache {

    /**
     * Get a file object corresponding to the script file to fetch objects for the collection.
     *
     * @param collection to what does this file apply?
     * @return a file object for the script
     */

    File getFetchScriptFile(EuropeanaCollection collection);

    /**
     * Get the first part of the script file
     *
     * @param collectionName which collection
     * @return a string to put at the beginning of the file
     */

    String createFetchScriptBegin(String collectionName);

    /**
     * Create the last piece of text for in the script file, reporting some things
     *
     * @param collectionName which collection is this
     * @param recordCount how many records
     * @param objectCount how many europeana objects
     * @param elapsedMillis how long did indexing take
     * @param indexErrorCount how many errors
     * @return some text for at the end of the script file
     */

    String createFetchScriptEnd(String collectionName, int recordCount, int objectCount, long elapsedMillis, int indexErrorCount);

    /**
     * Create a fetch instruction which will pick up the object at the given URI and
     * store it in the proper part of the cache directory structure.
     *
     * @param collectionName which collection was it
     * @param europeanaUri the main URI to which the object is attached
     * @param objectUri the object URI   @return fetch instruction for CacheMachine
     * @return the string to add
     */

    String createFetchScriptItem(String collectionName, String europeanaUri, String objectUri);

    /**
     * Get a file object which will lead to the proper cached file
     *
     * @param itemSize which item size do you want
     * @param uri where this thing was fetched
     * @return a file object
     */

    File getFile(ItemSize itemSize, String uri);

    /**
     * Discover what mime time is represented by the cached file
     *
     * @param cachedFile which file
     * @return what mime time is it.
     */

    MimeType getMimeType(File cachedFile);

}
