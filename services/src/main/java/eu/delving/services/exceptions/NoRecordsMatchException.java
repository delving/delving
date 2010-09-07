package eu.delving.services.exceptions;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 27, 2010 5:19:26 PM
 */
public class NoRecordsMatchException extends Throwable {
    private static final long serialVersionUID = 942631363533503772L;

    private String errorMessage;

    public NoRecordsMatchException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
