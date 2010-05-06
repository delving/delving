package eu.europeana.sip.model;

import javax.xml.namespace.QName;

/**
 * Store the record root, along with the number of records
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class RecordRoot {
    private QName rootQName;
    private int recordCount;

    public RecordRoot(String string) {
        String[] parts = string.split("\n");
        if (parts.length != 2) {
            throw new RuntimeException("Expected two lines");
        }
        this.rootQName = QName.valueOf(parts[0]);
        this.recordCount = Integer.parseInt(parts[1]);
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
        return rootQName + "\n" + recordCount;
    }
}
