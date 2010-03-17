package eu.europeana.sip.xml;

import eu.europeana.sip.mapping.QNamePath;
import eu.europeana.sip.mapping.Statistics;
import org.apache.log4j.Logger;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Analyze xml input and compile statistics.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class AnalysisParser {
    private Logger logger = Logger.getLogger(getClass());
    private QNamePath path = new QNamePath();
    private Map<QNamePath, Statistics> statisticsMap = new TreeMap<QNamePath, Statistics>();

    public void parseFile(String name, InputStream inputStream) throws XMLStreamException, FileNotFoundException {
        XMLInputFactory2 xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
        xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        xmlif.configureForSpeed();
        XMLStreamReader2 input = (XMLStreamReader2) xmlif.createXMLStreamReader(name, inputStream);
        StringBuilder text = new StringBuilder();
        long count = 0;
        while (true) {
            switch (input.getEventType()) {
                case XMLEvent.START_DOCUMENT:
                    logger.info("Starting document");
                    break;
                case XMLEvent.START_ELEMENT:
                    if (++count % 100000 == 0) {
                        logger.info("Processed "+count+" elements");
                    }
                    path.push(input.getName());
                    if (input.getAttributeCount() > 0) {
                        for (int walk = 0; walk < input.getAttributeCount(); walk++) {
                            QName attributeName = input.getAttributeName(walk);
                            path.push(attributeName);
                            recordValue(input.getAttributeValue(walk));
                            path.pop();
                        }
                    }
                    break;
                case XMLEvent.CHARACTERS:
                    text.append(input.getText());
                    break;
                case XMLEvent.CDATA:
                    text.append(input.getText());
                    break;
                case XMLEvent.END_ELEMENT:
                    recordValue(text.toString());
                    text.setLength(0);
                    path.pop();
                    break;
                case XMLEvent.END_DOCUMENT: {
                    logger.info("Ending document");
                    break;
                }
            }
            if (!input.hasNext()) {
                break;
            }
            input.next();
        }
    }

    public List<Statistics> getStatistics(int maximumSize) {
        List<Statistics> statisticsList = new ArrayList<Statistics>(statisticsMap.values());
        Collections.sort(statisticsList);
        for (Statistics statistics : statisticsList) {
            statistics.trimTo(maximumSize);
        }
        return statisticsList;
    }

    public String toString() {
        StringBuilder out = new StringBuilder();
        for (Statistics statistics : getStatistics(10)) {
            out.append(statistics);
        }
        return out.toString();
    }

    private void recordValue(String value) {
        value = value.trim();
        if (value.isEmpty()) {
            return;
        }
        Statistics statistics = statisticsMap.get(path);
        if (statistics == null) {
            QNamePath key = new QNamePath(path);
            statisticsMap.put(key, statistics = new Statistics(key));
        }
        statistics.recordValue(value);
    }
}