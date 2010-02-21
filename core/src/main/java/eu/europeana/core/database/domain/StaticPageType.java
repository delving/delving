package eu.europeana.core.database.domain;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 * @since Mar 17, 2009: 12:18:13 AM
 */
public enum StaticPageType {
    ABOUT_US("aboutus"),
    ACCESSIBILITY("accessibility"),
    LANGUAGE_POLICY("languagepolicy"),
    NEW_CONTENT("newcontent"),
    PRIVACY("privacy"),
    TERMS_OF_SERVICE("termsofservice"),
    USING_EUROPEANA("usingeuropeana"),
    THINK_VIDEO("thinkvideo"),
    ERROR("error"),
    LOGIN_PAGE("login"),
    SITEMAP("sitemap-outline"),
    ADVANCED_SEARCH("advancedsearch"),
    NEWS("news"),
    COMMUNITIES("communities"),
    THOUGHT_LAB("thought-lab"),
    CONTRIBUTORS("partners"),
    TAG_GRID("tag-grid"),
    YEAR_GRID("yeargrid");

    private String viewName;

    StaticPageType(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public static StaticPageType get(String string) {
        for (StaticPageType t : values()) {
            if (t.getViewName().equalsIgnoreCase(string)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Did not recognize StaticPageType: [" + string + "]");
    }

}
