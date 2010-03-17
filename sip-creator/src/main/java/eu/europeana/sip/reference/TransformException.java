package eu.europeana.sip.reference;

/**
 * Something went wrong during conversion
 */

@Deprecated
public class TransformException extends Exception {

    public TransformException(Throwable cause) {
        super(cause);
    }

    public TransformException(String message) {
        super(message);
    }

    public TransformException(String message, Throwable cause) {
        super(message, cause);
    }
}