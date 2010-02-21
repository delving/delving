package eu.europeana.core.querymodel.query;

/**
* @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
* @since Feb 20, 2010 8:20:42 PM
*/
public class PageLink {
    private int start;
    private int display;
    private boolean linked;

    public PageLink(int display, int start, boolean linked) {
        this.display = display;
        this.start = start;
        this.linked = linked;
    }

    public int getDisplay() {
        return display;
    }

    public int getStart() {
        return start;
    }

    public boolean isLinked() {
        return linked;
    }

    public String toString() {
        if (linked) {
            return display + ":" + start;
        }
        else {
            return String.valueOf(display);
        }
    }
}
