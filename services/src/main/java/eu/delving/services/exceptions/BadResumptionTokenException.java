package eu.delving.services.exceptions;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 27, 2010 5:19:26 PM
 */
public class BadResumptionTokenException extends Exception {

    private String errorMessage;
    private static final long serialVersionUID = -8837508447682753322L;

    public BadResumptionTokenException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}