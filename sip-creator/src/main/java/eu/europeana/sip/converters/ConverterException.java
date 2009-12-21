package eu.europeana.sip.converters;

/**
 * Something went wrong during conversion
 */
public class ConverterException extends Exception {
    private static final long serialVersionUID = -9132890488828985033L;

    public ConverterException(Throwable cause) {
        super(cause);
    }

    public ConverterException(String message) {
        super(message);
    }

    public ConverterException(String message, Throwable cause) {
        super(message, cause);
    }
}
