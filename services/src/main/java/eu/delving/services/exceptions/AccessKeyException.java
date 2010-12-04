package eu.delving.services.exceptions;

/**
 * Something went wrong with the MetaRepo
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class AccessKeyException extends Exception {
    public AccessKeyException(String s) {
        super(s);
    }

    public AccessKeyException(String s, Throwable throwable) {
        super(s, throwable);
    }
}