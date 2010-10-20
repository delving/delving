package eu.delving.services.exceptions;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 27, 2010 5:19:26 PM
 */

public class BadResumptionTokenException extends Exception {
    public BadResumptionTokenException(String errorMessage) {
        super(errorMessage);
    }
}