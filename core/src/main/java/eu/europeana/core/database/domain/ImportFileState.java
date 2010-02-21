package eu.europeana.core.database.domain;

/**
 * An enumeration describing the current state of a collection's import file
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public enum ImportFileState {
    NONEXISTENT,
    UPLOADING,
    UPLOADED,
    VALIDATING,
    VALIDATED,
    IMPORTING,
    IMPORTED,
    ERROR
}
