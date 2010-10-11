package eu.delving.services.exceptions;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 27, 2010 5:19:26 PM
 */

public class NoRecordsMatchException extends Exception {
    public NoRecordsMatchException(String errorMessage) {
        super(errorMessage);
    }
}
