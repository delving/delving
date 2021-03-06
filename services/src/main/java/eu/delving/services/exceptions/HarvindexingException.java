package eu.delving.services.exceptions;

/**
 * Something went wrong during importing
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class HarvindexingException extends Exception {
    private static final long serialVersionUID = 957621372671543988L;
    private int recordNumber = -1;

    public HarvindexingException(String message, int recordNumber) {
        super(message);
        this.recordNumber = recordNumber;
    }

    public HarvindexingException(String message, Throwable cause, int recordNumber) {
        super(message, cause);
        this.recordNumber = recordNumber;
    }

    public HarvindexingException(String message) {
        super(message);
    }

    public HarvindexingException(String message, Throwable cause) {
        super(message, cause);
    }

    public int getRecordNumber() {
        return recordNumber;
    }

    @Override
    public String getMessage() {
        if (recordNumber >= 0) {
            return super.getMessage()+" [Record #"+recordNumber+"]";
        }
        else {
            return super.getMessage();
        }
    }

}
