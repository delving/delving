package eu.delving.metarepo.exceptions;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Jun 27, 2010 5:27:15 PM
 */
public class BadArgumentException extends Throwable {

    private static final long serialVersionUID = -6617982891436276130L;
    private String errorMessage;

    public BadArgumentException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}