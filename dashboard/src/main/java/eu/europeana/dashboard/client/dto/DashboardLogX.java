package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.Date;

/**
 * Mirrors domain.DashboardLog
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */
public class DashboardLogX implements IsSerializable {
    private Long id;
    private String who;
    private Date time;
    private String what;

    public DashboardLogX(Long id, String who, Date time, String what) {
        this.id = id;
        this.who = who;
        this.time = time;
        this.what = what;
    }

    public DashboardLogX() {
    }

    public Long getId() {
        return id;
    }

    public String getWho() {
        return who;
    }

    public Date getTime() {
        return time;
    }

    public String getWhat() {
        return what;
    }
}
