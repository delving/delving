package eu.europeana.core.querymodel.query;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public enum FormatType {
    HTML("text/html;charset=UTF-8","brief-doc-window", "table"),
    TABLE("text/html;charset=UTF-8","brief-doc-window", "table"),
    LIST("text/html;charset=UTF-8","brief-doc-window", "list"),
    FLOW("text/html;charset=UTF-8","brief-doc-window", "flow"),
    RSS("application/rss+xml;charset=UTF-8","brief-doc-window-rss"),
    RDF("text/xml;charset=UTF-8","brief-doc-window-rdf"),
    SRW("text/xml;charset=UTF-8","brief-doc-window-srw");

    private String contentType;
    private String viewName;
    private String display;

    private FormatType(String contentType, String viewName) {
        this.contentType = contentType;
        this.viewName = viewName;
        this.display = "";
    }

    private FormatType(String contentType, String viewName, String display) {
        this.contentType = contentType;
        this.viewName = viewName;
        this.display = display;
    }

    public String getContentType() {
        return contentType;
    }

    public String getViewName() {
        return viewName;
    }

    public boolean isDisplaySpecified() {
        return display.length() > 0;
    }

    public String getDisplay() {
        return display;
    }

    public static FormatType get(String formatParam) {
        for (FormatType ft : values()) {
            if (ft.toString().equalsIgnoreCase(formatParam)) {
                return ft;
            }
        }
        return HTML;
    }
}
