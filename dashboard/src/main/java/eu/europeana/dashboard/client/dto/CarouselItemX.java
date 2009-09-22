package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Parallels CarouselItem
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class CarouselItemX implements IsSerializable {
    private Long id;
    private String europeanaUri;
    private String title;
    private String thumbnail;
    private String creator;
    private String year;
    private String provider;
    private String language;
    private DocTypeX type;

    public CarouselItemX(Long id, String europeanaUri, String title, String thumbnail, String creator, String year, String provider, String language, DocTypeX type) {
        this.id = id;
        this.europeanaUri = europeanaUri;
        this.title = title;
        this.thumbnail = thumbnail;
        this.creator = creator;
        this.year = year;
        this.provider = provider;
        this.language = language;
        this.type = type;
    }

    public CarouselItemX() {
    }

    public Long getId() {
        return id;
    }

    public String getEuropeanaUri() {
        return europeanaUri;
    }

    public String getTitle() {
        return title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getCreator() {
        return creator;
    }

    public String getYear() {
        return year;
    }

    public String getProvider() {
        return provider;
    }

    public String getLanguage() {
        return language;
    }

    public DocTypeX getType() {
        return type;
    }

    public enum DocTypeX implements IsSerializable {
        TEXT,
        IMAGE,
        SOUND,
        VIDEO,
        UNKNOWN
    }
}
