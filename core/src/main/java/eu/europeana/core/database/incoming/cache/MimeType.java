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

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public enum MimeType {
    JPEG("image/jpeg",".jpg", true),
    JPG("image/jpg",".jpg", true),
    PNG("image/png",".png", true),
    GIF("image/gif",".gif", true),
    PDF("application/pdf",".pdf", true),
    HTML("text/html",".html", false),
    XML("text/xml;charset=utf-8",".xml", false),
    ERROR("error",".error", false);

    private String type, extension;
    private boolean cacheable;

    MimeType(String type, String extension, boolean cacheable) {
        this.extension = extension;
        this.type = type;
        this.cacheable = cacheable;
    }

    public String getExtension() {
        return extension;
    }

    public String getType() {
        return type;
    }

    public boolean isCacheable() {
        return cacheable;
    }
}