package eu.europeana.sip;

import com.thoughtworks.xstream.XStream;
import eu.europeana.core.querymodel.query.Language;
import eu.europeana.query.RecordField;
import eu.europeana.sip.converters.ConverterException;
import org.apache.log4j.*;
import org.codehaus.stax2.XMLInputFactory2;
import org.codehaus.stax2.XMLStreamReader2;

import javax.xml.namespace.QName;
import javax.xml.stream.*;
import java.io.*;
import java.util.*;

/**
 * Turn diverse source xml data into standardized output for import into the europeana portal database and search
 * engine.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class Normalizer implements Runnable {
    public static final Layout LOG_LAYOUT = new PatternLayout("%d{ABSOLUTE} %-5p %C{1} - %m%n");
    private static Logger log;
    private Profile profile;
    private Map<String, String> typeMap = new HashMap<String, String>();
    private Map<String, String> languageMap = new HashMap<String, String>();
    private Set<String> europeanaUriSet = new HashSet<String>();
    private Counters counters = new Counters();
    private Set<String> missingTypeMappings = new TreeSet<String>();
    private Set<String> missingLanguageMappings = new TreeSet<String>();
    private File inputDirectory, outputDirectory;
    private boolean writeSolr;
    private Runnable finalAct;
    private Progress progress;
    private boolean running = true;

    public Normalizer(File inputDirectory, File outputDirectory, boolean writeSolr) throws IOException {
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        this.writeSolr = writeSolr;
        this.log = Logger.getLogger(inputDirectory.getName());
        XStream stream = new XStream();
        stream.processAnnotations(Profile.class);
        File profileFile = new File(new File(inputDirectory, "profile"), "profile.xml");
        try {
            this.profile = loadProfile(profileFile);
        }
        catch (Exception e) {
            String message = "Unable to read profile "+profileFile.getAbsolutePath()+"]";
            log.error(message, e);
            throw new IOException(message);
        }
        if (profile.typeMappings != null) {
            for (Profile.TypeMapping typeMapping : profile.typeMappings) {
                for (String from : typeMapping.from) {
                    typeMap.put(from, typeMapping.type);
                }
            }
        }
        if (profile.languageMappings != null) {
            for (Profile.LanguageMapping languageMapping : profile.languageMappings) {
                Language language = Language.get(languageMapping.code, true);
                for (String from : languageMapping.from) {
                    for (String alternative : language.getAlternatives()) {
                        if (alternative.equals(from)) {
                            throw new RuntimeException("Redundant language mapping: "+from+" => "+language);
                        }
                    }
                    languageMap.put(from, languageMapping.code);
                }
            }
        }
    }

    public static Profile loadProfile(File profileFile) throws IOException {
        XStream stream = new XStream();
        stream.processAnnotations(Profile.class);
        Profile profile = (Profile) stream.fromXML(new InputStreamReader(new FileInputStream(profileFile), "UTF-8"));

        /*
        Checks on loading
         */
        for (Profile.Source source : profile.sources) {
            for (Profile.FieldMapping fl : source.fieldMappings) {
                if (fl.from == null)
                    // todo: give deprecation warning first
//                    log.warn("'<field-mapping key=\"\"/>' is no longer allowed. Please change it to '<field-mapping from=\"\"/>' " +
//                            "In the next release this will give runtime errors");
                    throw new IOException("Missing from clause in field mapping to " + fl.mapTo);
            }
        }

        return profile;
    }

    public interface Progress {
        void message(String message);
        void recordsProcessed(int count);
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public void abort() {
        running = false;
    }

    public void setFinalAct(Runnable runnable) {
        this.finalAct = runnable;
    }

    @Override
    public void run() {
        try {
            for (Profile.Source source : profile.sources) {
                Map<String, Profile.FieldMapping> fieldMappings = new HashMap<String, Profile.FieldMapping>();
                for (Profile.FieldMapping fieldMapping : source.fieldMappings) {
                    if (fieldMappings.containsKey(fieldMapping.from)) {
                        log.warn("Multiple field mapping for "+fieldMapping.from);
                    }
                    fieldMappings.put(fieldMapping.from, fieldMapping);
                }
                parseXMLRecords(source, fieldMappings);
            }
        }
        finally {
            if (finalAct != null) {
                finalAct.run();
            }
        }
    }

    private void parseXMLRecords(Profile.Source source, Map<String, Profile.FieldMapping> fieldMappings) {
        QName separator = QName.valueOf(source.recordSeparator);
        File inputFile = new File(new File(inputDirectory, "input_source"), source.file);
        File outputFile = new File(outputDirectory, source.file);
        int recordsProcessed = 0;
        int recordsNormalized = 0;
        Stack<TextValue> textValues = new Stack<TextValue>();
        XMLStreamReader2 input = null;
        Appender fileAppender = null;
        XMLStreamWriter output = null;
        try {
            input = createReader(inputFile);
            File logFile = new File(outputFile + ".log");
            fileAppender = new FileAppender(LOG_LAYOUT, logFile.getAbsolutePath());
            fileAppender.setName(logFile.getName());
            Logger.getRootLogger().addAppender(fileAppender);
            output = createStreamWriter(outputFile);
            int depth = 0;
            SolrRecord record = null;
            String path = "";
            while (running) {
                switch (input.getEventType()) {
                    case XMLStreamConstants.START_DOCUMENT:
                        startDocument(output, writeSolr);
                        break;
                    case XMLStreamConstants.START_ELEMENT:
                        depth++;
                        path += "/" + input.getPrefixedName();
                        if (separator.equals(input.getName()) && (source.recordSeparatorDepth == 0 || depth == source.recordSeparatorDepth)) {
                            record = new SolrRecord(typeMap, missingTypeMappings, languageMap, missingLanguageMappings, source.collectionId);
                            recordsProcessed++;
                            if (recordsProcessed % 100 == 0) {
                                if (progress != null) {
                                    progress.recordsProcessed(recordsProcessed);
                                }
                            }
                        }
                        if (record != null) {
                            appendChar(textValues, ' ');
                            Profile.FieldMapping elementFieldMapping = fieldMappings.get(path);
                            QName tagName = input.getName();
                            SolrField field = null;
                            if (elementFieldMapping != null) {
                                field = new SolrField(tagName, elementFieldMapping);
                            }
                            for (int walk = 0; walk < input.getAttributeCount(); walk++) {
                                String attribute = (input.getAttributePrefix(walk) != null) ? input.getAttributePrefix(walk)+":"+input.getAttributeLocalName(walk): input.getAttributeLocalName(walk);
                                String attributePath = path + "@" + attribute;
                                Profile.FieldMapping attributeFieldMapping = fieldMappings.get(attributePath);
                                if (attributeFieldMapping == null) {
                                    continue;
                                }
                                String attributeValue = input.getAttributeValue(walk);
                                if (attributeFieldMapping.discardRecord != null) {
                                    if (attributeValue.equals(attributeFieldMapping.discardRecord)) {
                                        log.debug("discarding record due to "+attributePath+" being equal to "+attributeFieldMapping.discardRecord);
                                        record = null;
                                        continue;
                                    }
                                }
                                if (attributeFieldMapping.acceptField != null) {
                                    if (!attributeValue.equals(attributeFieldMapping.acceptField)) {
                                        log.debug("discarding field due to "+attributePath+" being unequal to "+attributeFieldMapping.acceptField);
                                        field = null;
                                        continue;
                                    }
                                }
//                                uncomment this if you want langages to be recorded
//                                if (field != null && "xml:lang".equals(attribute)) {
//                                    field.setLanguage(attributeValue);
//                                }
                                record.add(new SolrField(input.getAttributeName(walk), attributeFieldMapping)).getValue().append(input.getAttributeValue(walk));
                            }
                            if (field != null) {
                                textValues.push(new TextValue(tagName,record.add(field).getValue()));
                                log.info("push: "+tagName);
                            }
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                    case XMLStreamConstants.CDATA:
                        if (textValues.size() > 1) {
                            log.info("text values: "+textValues.size());
                        }
                        if (!textValues.isEmpty()) {
                            String chars = input.getText().trim();
                            for (int walk = 0; walk < chars.length(); walk++) {
                                char ch = chars.charAt(walk);
                                if (ch == '\n') {
                                    ch = ' ';
                                }
                                appendChar(textValues, ch);
                            }
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        depth--;
                        path = path.substring(0, path.length() - input.getPrefixedName().length() - 1);
                        QName tagName = input.getName();
                        if (!textValues.isEmpty()) {
                            if (textValues.peek().getQName().equals(tagName)) {
                                textValues.pop();
                                log.info("pop: "+tagName);
                            }
                        }
                        if (record != null && separator.equals(tagName)) {
                            try {
                                record.doConversions();
                            }
                            catch (ConverterException e) {
                                log.warn("Unable to convert", e);
                                if (log.isDebugEnabled()) log.debug(record);
                                counters.converterProblem();
                                record = null;
                                break;
                            }
                            record.removeEmpty();
                            boolean hasObject = record.hasField(RecordField.EUROPEANA_OBJECT);
                            if (!hasObject) {
                                counters.withoutEuropeanaObject();
                                log.warn("Record has no " + RecordField.EUROPEANA_OBJECT);
                                if (log.isDebugEnabled()) log.debug(record);
                                if (profile.discardWithoutObject) {
                                    counters.discardedWithoutEuropeanaObject();
                                    record = null;
                                    break;
                                }
                            }
                            boolean hasIsShownValue = record.hasField(RecordField.EUROPEANA_IS_SHOWN_AT) || record.hasField(RecordField.EUROPEANA_IS_SHOWN_BY);
                            if (!hasIsShownValue) {
                                counters.noIsShownValue();
                                log.warn("Record has no " + RecordField.EUROPEANA_IS_SHOWN_AT + " or " + RecordField.EUROPEANA_IS_SHOWN_BY);
                                if (log.isDebugEnabled()) log.debug(record);
                                record = null;
                                break;
                            }
                            record.putValue(new Profile.MapTo(RecordField.EUROPEANA_HAS_OBJECT), String.valueOf(hasObject));
                            record.chooseFirstOrLast();
                            if (source.additions != null) {
                                appendAdditions(source, record);
                            }
                            if (!record.hasField(RecordField.EUROPEANA_TYPE)) {
                                counters.unknownEuropeanaType();
                                log.warn("Record has no " + RecordField.EUROPEANA_TYPE + ", discarding");
                                if (log.isDebugEnabled()) log.debug(record);
                                record = null;
                                break;
                            }
                            if (!record.hasField(RecordField.EUROPEANA_LANGUAGE)) {
                                counters.missingLanguage();
                                log.warn("Record has no " + RecordField.EUROPEANA_LANGUAGE + ", discarding");
                                record = null;
                                break;
                            }
                            String europeanaUri = record.getFirstValue(RecordField.EUROPEANA_URI);
                            if (europeanaUri == null) {
                                counters.missingEuropeanaUri();
                                log.info("Missing Europeana URI");
                                if (log.isDebugEnabled()) log.debug(record);
                                record = null;
                                break;
                            }
                            if (europeanaUriSet.contains(europeanaUri)) {
                                if (profile.duplicatesAllowed) {
                                    log.warn("Duplicate Europeana URI: " + europeanaUri);
                                    if (!profile.renderDuplicates) {
                                        counters.duplicateUriDiscarded();
                                        log.warn("Discarding record with original duplicate URI: " + europeanaUri);
                                        if (log.isDebugEnabled()) log.debug(record);
                                        record = null;
                                        break;
                                    }
                                    else {
                                        counters.duplicateUriAllowed();
                                    }
                                }
                                else {
                                    throw new XMLStreamException("Duplicate EuropeanaURI: [" + europeanaUri + "]");
                                }
                            }
                            europeanaUriSet.add(europeanaUri);
                            validateRecord(record);
                            recordsNormalized++;
                            record.concatenateDuplicates();
                            record.removeDuplicates();
                            record.removeEmpty();
                            if (record.removeIfNotURL(RecordField.EUROPEANA_IS_SHOWN_AT)) {
                                log.warn(RecordField.EUROPEANA_IS_SHOWN_AT+" was not a URL");
                            }
                            if (record.removeIfNotURL(RecordField.EUROPEANA_IS_SHOWN_BY)) {
                                log.warn(RecordField.EUROPEANA_IS_SHOWN_BY+" was not a URL");
                            }
                            if (record.removeIfNotURL(RecordField.EUROPEANA_OBJECT)) {
                                log.warn(RecordField.EUROPEANA_OBJECT+" was not a URL");
                            }
                            record.render(output, writeSolr);
                            record = null;
                        }
                        break;
                    case XMLStreamConstants.END_DOCUMENT:
                        log.info("End Document.");
                        if (!missingTypeMappings.isEmpty()) {
                            for (String missing : missingTypeMappings) {
                                log.info("Missing Type Mapping: " + missing);
                            }
                        }
                        if (!missingLanguageMappings.isEmpty()) {
                            for (String missing : missingLanguageMappings) {
                                log.info("Missing Language: " + missing);
                            }
                        }
                        output.writeEndDocument();
                        break;
                }
                if (!input.hasNext()) {
                    break;
                }
                input.next();
            }
        }
        catch (Exception e) {
            log.error("Problem with profile " + profile.name, e);
        }
        finally {
            try {
                input.close();
                output.close();
            }
            catch (XMLStreamException e) {
                log.error("Problem closing streams",e);
            }
            if (progress != null) {
                progress.message(running ? "Finished" : "Aborted");
            }
            log.info("Statistics...\n"+counters);
            log.info("Total Records Processed: "+recordsProcessed);
            log.info("Total Records Normalized: "+recordsNormalized);
            Logger.getRootLogger().removeAppender(fileAppender);
            fileAppender.close();
        }
    }

    private void appendChar(Stack<TextValue> textValues, char ch) {
        for (TextValue fieldValue : textValues) {
            StringBuilder builder = fieldValue.getStringBuilder();
            if (builder.length() > 0) {
                char lastChar = builder.charAt(builder.length()-1);
                if (!(lastChar == ' ' && ch == ' ')) {
                    fieldValue.getStringBuilder().append(ch);
                }
            }
            else if (ch != ' '){
                fieldValue.getStringBuilder().append(ch);
            }
        }
    }

    private void appendAdditions(Profile.Source source, SolrRecord record) {
        for (Profile.RecordAddition addition : source.additions) {
            boolean found = record.containsRecordField(addition.key);
            if (found) {
                if (!addition.ifMissing) {
                    if (log.isDebugEnabled()) log.debug(record);
                    throw new RuntimeException("Addition " + addition.key + " conflicts with existing field unless 'ifMissing' flag in profile is set to 'true'");
                }
            }
            else {
                record.putValue(new Profile.MapTo(addition.key), addition.value);
            }
        }
    }

    private class TextValue {
        private QName tagName;
        private StringBuilder stringBuilder;

        private TextValue(QName tagName, StringBuilder stringBuilder) {
            this.tagName = tagName;
            this.stringBuilder = stringBuilder;
        }

        public QName getQName() {
            return tagName;
        }

        public StringBuilder getStringBuilder() {
            return stringBuilder;
        }

        public String toString() {
            return tagName+":"+stringBuilder;
        }
    }

//    private boolean matchFilter(XMLStreamReader input, String acceptField) {
//        boolean filterMatch = false;
//        boolean prefix = acceptField.indexOf(":") > 0;
//        for (int walk = 0; walk < input.getAttributeCount(); walk++) {
//            String nameValue = (prefix ? input.getAttributePrefix(walk) + ":" : "") + input.getAttributeLocalName(walk) + "=" + input.getAttributeValue(walk);
//            if (acceptField.equals(nameValue)) {
//                filterMatch = true;
//                break;
//            }
//        }
//        return filterMatch;
//    }
//
    private void validateRecord(SolrRecord record) throws ConverterException {
        validateField(record, RecordField.EUROPEANA_TYPE); // already been checked, this is double-check
        validateField(record, RecordField.EUROPEANA_LANGUAGE); // already been checked, this is double-check
        validateField(record, RecordField.EUROPEANA_COUNTRY);
        validateField(record, RecordField.EUROPEANA_PROVIDER);
        List<SolrRecord.Entry> yearEntries = record.getEntries(RecordField.EUROPEANA_YEAR);
        for (SolrRecord.Entry entry : yearEntries) {
            String europeanaYear = entry.getValue();
            if ("0000".equals(europeanaYear)) {
                record.removeEntry(entry);
                log.warn("Record had unparseable year. Removed field");
                if (log.isDebugEnabled()) log.debug(record);
                counters.unparseableYear();
            }
        }
    }

    private void validateField(SolrRecord record, RecordField field) throws ConverterException {
        if (!record.hasField(field)) {
            throw new RuntimeException("Missing " + field);
        }
    }

    private XMLStreamWriter createStreamWriter(File outputFile) throws FileNotFoundException, UnsupportedEncodingException, XMLStreamException {
        PrintWriter out = new PrintWriter(outputFile, "UTF-8");
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        return factory.createXMLStreamWriter(out);
    }

    private XMLStreamReader2 createReader(File inputFile) throws XMLStreamException, FileNotFoundException {
        XMLInputFactory2 xmlif = createInputFactory();
        return (XMLStreamReader2) xmlif.createXMLStreamReader(inputFile.toString(), new FileInputStream(inputFile));
    }

    private XMLInputFactory2 createInputFactory() {
        XMLInputFactory2 xmlif = (XMLInputFactory2) XMLInputFactory2.newInstance();
        xmlif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, Boolean.FALSE);
        xmlif.setProperty(XMLInputFactory.IS_COALESCING, Boolean.FALSE);
        xmlif.configureForSpeed();
        return xmlif;
    }

    private void startDocument(XMLStreamWriter writer, boolean writeSolr) throws XMLStreamException {
        log.info("Start Document.");
        writer.writeStartDocument();
        writer.writeCharacters("\n");
        if (!writeSolr) {
            writer.writeStartElement("metadata");
            for (String[] pair : QNameBuilder.PREFIX) {
                writer.writeNamespace(pair[0], pair[1]);
            }
        }
        else {
            writer.writeStartElement("save");
        }
        writer.writeCharacters("\n");
    }

    public String toString() {
        return "Parse(" + profile.name + ")";
    }

    public static List<File> getSourcesWithProfiles(File sourceRoot) {
        List<File> list = new ArrayList<File>();
        for (File directory : sourceRoot.listFiles()) {
            File profile = new File(directory, "profile/profile.xml");
            if (profile.exists()) {
                list.add(directory);
            }
        }
        return list;
    }

    private static class PrintProgress implements Progress {

        private String name;

        private PrintProgress(String name) {
            this.name = name;
        }

        @Override
        public void message(String message) {
            System.out.println(name+": "+message);
        }

        @Override
        public void recordsProcessed(int count) {
            System.out.println(name+": processed "+count);
            try {
                Thread.sleep(100);
            }
            catch (InterruptedException e) {
                // nothing
            }
        }
    }

    public static void main(String... args) throws Exception {
        if (args.length < 2) {
            System.out.println("Usage: <source> <destination> [... source numbers ...]");
            return;
        }
        Logger.getRootLogger().setLevel(Level.INFO);
        String fromDir = ".";
        if (args.length > 0) {
            fromDir = args[0];
        }
        File sourceRoot = new File(fromDir);
        String toDir = ".";
        if (args.length > 1) {
            toDir = args[1];
        }
        File destinationRoot = new File(toDir);
        List<File> sources = getSourcesWithProfiles(sourceRoot);
        Map<String, File> sourceMap = new TreeMap<String, File>();
        for (File source : sources) {
            String name = source.getName();
            int underscore = name.indexOf("_");
            if (underscore < 0) {
                throw new Exception("Collection without a number! "+name);
            }
            String number = name.substring(0, underscore);
            sourceMap.put(number, source);
        }
        if (args.length == 2) {
            System.out.println("Usage: <source> <destination> [... source numbers ...]");
            System.out.println("Choose from the Sources: ");
            for (Map.Entry<String,File> entry : sourceMap.entrySet()) {
                System.out.println("    "+entry.getKey()+") "+entry.getValue().getName());
            }
        }
        else {
            List<Normalizer> normalizers = new ArrayList<Normalizer>();
            if (args.length == 3 && "*".equals(args[2])) {
                for (File sourceDirectory : sourceMap.values()) {
                    try {
                        Normalizer normalizer = new Normalizer(sourceDirectory, destinationRoot, false);
                        normalizers.add(normalizer);
                    }
                    catch (IOException e) {
                        System.out.println(e.toString());
                    }
                }
            }
            else {
                for (int walk=2; walk<args.length; walk++) {
                    File sourceDirectory = sourceMap.get(args[walk]);
                    if (sourceDirectory == null) {
                        System.out.println("Didn't recognize ["+args[walk]+"]");
                        System.out.println("It must be one of these: ");
                        for (Map.Entry<String,File> entry : sourceMap.entrySet()) {
                            System.out.println("    "+entry.getKey()+") "+entry.getValue().getName());
                        }
                        return;
                    }
                    try {
                        Normalizer normalizer = new Normalizer(sourceDirectory, destinationRoot, false);
                        normalizers.add(normalizer);
                    }
                    catch (IOException e) {
                        System.out.println(e.toString());
                    }
                }
            }
            for (Normalizer normalizer : normalizers) {
                Thread thread = new Thread(normalizer);
                thread.setName(normalizer.toString());
                System.out.println("Starting thread "+thread.getName());
                normalizer.setProgress(new PrintProgress(thread.getName()));
                thread.start();
            }
        }
    }
}
