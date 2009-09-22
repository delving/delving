package eu.europeana.dashboard.client.dto;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Mirroring the StaticPage
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

public class StaticPageX implements IsSerializable {
    private Long id;
    private String pageType;
    private LanguageX language;
    private String content;

    public StaticPageX(Long id, String pageType, LanguageX language, String content) {
        this.id = id;
        this.pageType = pageType;
        this.language = language;
        this.content = content;
    }

    public StaticPageX(String pageType, LanguageX language) {
        this.pageType = pageType;
        this.language = language;
    }

    public StaticPageX() {
    }

    public Long getId() {
        return id;
    }

    public String getPageType() {
        return pageType;
    }

    public LanguageX getLanguage() {
        return language;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}