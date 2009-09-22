package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * This enum determines the checkboxes that appear in the interface
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public enum FilterChoice implements IsSerializable {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    ERROR
}