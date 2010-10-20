package eu.delving.services.exceptions;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 27, 2010 5:27:15 PM
 */

public class BadArgumentException extends Exception {
    public BadArgumentException(String errorMessage) {
        super(errorMessage);
    }
}