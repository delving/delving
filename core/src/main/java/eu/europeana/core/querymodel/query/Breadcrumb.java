package eu.europeana.core.querymodel.query;

/**
 * Track back to where you came from
 *
 * @author Gerald de Jong geralddejong@gmail.com
 */

public class Breadcrumb {
    private String href;
    private String display;
    private String field;
    private String value;
    private boolean last;

    public Breadcrumb(String href, String display) {
        this.href = href;
        this.display = display;
    }

    public Breadcrumb(String href, String display, String field, String value) {
        this.href = href;
        this.display = display;
        this.field = field;
        this.value = value;
    }

    void flagAsLast() {
        this.last = true;
    }

    public String getDisplay() {
        return display;
    }

    public String getHref() {
        return href;
    }

    public boolean isLast() {
        return last;
    }

    public String getField() {
        return field;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return "<a href=\"" + href + "\">" + display + "</a>";
    }

}
