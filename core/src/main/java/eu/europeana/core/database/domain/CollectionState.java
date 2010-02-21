package eu.europeana.core.database.domain;

/**
 * An enumeration describing the current state of a collection
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public enum CollectionState {
    EMPTY,
    DISABLED,
    QUEUED,
    INDEXING,
    ENABLED
}