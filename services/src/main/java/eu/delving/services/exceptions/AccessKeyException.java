package eu.delving.services.exceptions;

/**
 * Access to the API not granted
 *
 * @author Gerald de Jong <gerald@delving.eu>
 */

public class AccessKeyException extends Exception {
    public AccessKeyException(String errorMessage) {
        super(errorMessage);
    }
}