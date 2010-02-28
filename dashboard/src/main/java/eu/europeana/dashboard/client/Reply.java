package eu.europeana.dashboard.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * An abstract superclass which reports errors
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public abstract class Reply<T> implements AsyncCallback<T> {

    private static FailureHandler handler;

    public static void setHandler(FailureHandler handler) {
        Reply.handler = handler;
    }

    @Override
    public void onFailure(Throwable caught) {
        handler.onFailure(caught);
    }

    public interface FailureHandler {
        void onFailure(Throwable caught);
    }
}
