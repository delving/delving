package eu.delving.sip;

/**
 * the types of files that are uploaded
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public enum FileType {

    SOURCE_DETAILS(
            "application/gzip"
    ),
    SOURCE(
            "text/plain"
    ),
    MAPPING(
            "text/xml"
    );

    private String contentType;

    FileType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
}
