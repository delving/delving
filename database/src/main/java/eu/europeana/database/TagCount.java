package eu.europeana.database;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class TagCount implements Comparable<TagCount> {
    private String tag;
    private Long count;

    public TagCount(String tag, Long count) {
        this.tag = tag;
        this.count = count;
    }

    public Long getCount() {
        return count;
    }

    public String getTag() {
        return tag;
    }

    public String toString() {
        return "'"+tag+"' ("+count+")";
    }

    public int compareTo(TagCount o) {
        return count.intValue() - o.count.intValue(); 
    }
}
