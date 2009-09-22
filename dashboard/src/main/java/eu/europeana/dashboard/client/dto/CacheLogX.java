package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

/**
 * hold a cache log entry
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class CacheLogX implements IsSerializable {
    private int objectCount;
    private Date date;

    public CacheLogX(int objectCount, Date date) {
        this.objectCount = objectCount;
        this.date = date;
    }

    public CacheLogX() {
    }

    public int getObjectCount() {
        return objectCount;
    }

    public Date getDate() {
        return date;
    }
}
