package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Mirroring the Partner domain object
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class PartnerX implements IsSerializable {
    private Long id;
    private String name;
    private String sector;
    private String url;

    public PartnerX(Long id, String name, String sector, String url) {
        this.id = id;
        this.name = name;
        this.sector = sector;
        this.url = url;
    }

    public PartnerX(String name, String sector, String url) {
        this.name = name;
        this.sector = sector;
        this.url = url;
    }

    public PartnerX() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSector() {
        return sector;
    }

    public String getUrl() {
        return url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSector(String sector) {
        this.sector = sector;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
