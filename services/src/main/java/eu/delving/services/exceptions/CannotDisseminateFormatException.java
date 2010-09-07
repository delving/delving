package eu.delving.services.exceptions;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 27, 2010 5:27:15 PM
 */
public class CannotDisseminateFormatException extends Throwable {
    private static final long serialVersionUID = -613061879532968842L;

    private String errorMessage;

    public CannotDisseminateFormatException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
