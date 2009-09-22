package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Interesting stuff from the saved item
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class SavedItemX implements IsSerializable {
    private String title;
    private String uri;

    public SavedItemX(String title, String uri) {
        this.title = title;
        this.uri = uri;
    }

    public SavedItemX() {
    }

    public String getTitle() {
        return title;
    }

    public String getUri() {
        return uri;
    }
}
