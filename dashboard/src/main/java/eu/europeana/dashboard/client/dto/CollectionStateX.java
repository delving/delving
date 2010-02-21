package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This is intended to perfectly mirror eu.europeana.core.database.domain.CollectionState
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public enum CollectionStateX implements IsSerializable {
    EMPTY(FilterChoice.NOT_STARTED),
    DISABLED(FilterChoice.NOT_STARTED),
    QUEUED(FilterChoice.IN_PROGRESS),
    INDEXING(FilterChoice.IN_PROGRESS),
    ENABLED(FilterChoice.COMPLETED);

    private FilterChoice filterChoice;

    CollectionStateX() {
    }

    CollectionStateX(FilterChoice filterChoice) {
        this.filterChoice = filterChoice;
    }

    public FilterChoice getFilterChoice() {
        return filterChoice;
    }
}
