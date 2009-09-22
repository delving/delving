package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This is intended to perfectly mirror eu.europeana.database.domain.CacheState
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public enum CacheStateX implements IsSerializable {
    EMPTY(FilterChoice.NOT_STARTED),
    UNCACHED(FilterChoice.NOT_STARTED),
    QUEUED(FilterChoice.IN_PROGRESS),
    CACHEING(FilterChoice.IN_PROGRESS),
    CACHED(FilterChoice.COMPLETED);

    private FilterChoice filterChoice;

    CacheStateX() {
    }

    CacheStateX(FilterChoice filterChoice) {
        this.filterChoice = filterChoice;
    }

    public FilterChoice getFilterChoice() {
        return filterChoice;
    }
}