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

package eu.europeana.cache;

import java.io.File;
import java.io.IOException;

/**
 * Cache digital objects which can be fetched from remote sites
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public interface DigitalObjectCache {
    boolean cache(String uri) throws IOException;
    boolean remove(String uri);
    File[] getItemSizeCachedFiles(String uri);
    MimeType getMimeType(File cachedFile);
}
