package eu.europeana.database.domain;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 * @since Mar 9, 2009: 4:45:17 PM
 */
public enum PartnerSector {
    ARCHIVES("Archives"),
    AUDIO_VISUAL_COLLECTIONS("Audio-visual collections"),
    CROSS_DOMAIN("Cross-domain associations"),
    LIBRARIES("Libraries"),
    MUSEUMS("Museums"),
    NATIONAL_REPRESENTATIVES("National representatives"),
    OTHER("Other"),
    PROJECT_CONTRIBUTORS("Project Contributors"),
    RESEARCH_INSTITUTIONS("Research institutions");

    private String viewName;

    PartnerSector() {
    }

    PartnerSector(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public static PartnerSector get(String string) {
        for (PartnerSector t : values()) {
            if (t.getViewName().equalsIgnoreCase(string)) {
                return t;
            }
        }
        throw new IllegalArgumentException("Did not recognize PartnerSector: ["+string+"]");
    }
}
