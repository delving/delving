package eu.europeana.sip.core;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Store the record root, along with the number of records
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class RecordRoot {
    private static final String PREFIX = "// ## RecordRoot ";
    private QName rootQName;
    private int recordCount;

    public static RecordRoot fromMapping(List<String> mapping) {
        for (String line : mapping) {
            RecordRoot recordRoot = fromLine(line);
            if (recordRoot != null) {
                return recordRoot;
            }
        }
        return null;
    }

    private static RecordRoot fromLine(String line) {
        if (line.startsWith(PREFIX)) {
            String recordRootString = line.substring(PREFIX.length());
            String[] parts = recordRootString.split(" ");
            QName qname = QName.valueOf(parts[0]);
            int recordCount = Integer.parseInt(parts[1]);
            return new RecordRoot(qname, recordCount);
        }
        return null;
    }

    public RecordRoot(QName rootQName, int recordCount) {
        this.rootQName = rootQName;
        this.recordCount = recordCount;
    }

    public QName getRootQName() {
        return rootQName;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public String toString() {
        return PREFIX + rootQName + " " + recordCount;
    }
}
