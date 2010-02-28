package eu.europeana.sip.generator;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 *         <p/>
 *         The purpose of this class is to automate the conversion from mapping to profile
 */
public class ProfileGenerator {
    private static int lines = 0;
    private static final int NUMBER_OF_HEADER_LINES = 57;

    private static String createHeader(String fileName) {
        String fileBase = fileName.replaceAll("(.*)_mapping.txt", "$1");
        String xmlFileName = fileBase + ".xml";
        String collectionId = fileBase.replaceAll("^(.*?)_.*", "$1");
        String providerId = collectionId.substring(0, 3);
        return
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<profile name=\"" + fileBase + "\" directory=\"" + fileBase + "/input_source/\" publisherId=\"" + providerId + "\" duplicatesAllowed=\"false\" renderDuplicates=\"false\">\n" +
                "   <sources>\n" +
                "      <source file=\"" + xmlFileName + "\" recordSeparator=\"\" collectionId=\"" + collectionId + "\">\n" +
                "         <additions>\n" +
                "            <addition key=\"EUROPEANA_COUNTRY\" value=\"\"/>\n" +
                "            <addition key=\"EUROPEANA_PROVIDER\" value=\"\"/>\n" +
                "            <addition key=\"EUROPEANA_TYPE\" value=\"IMAGE\"/>\n" +
                "            <addition key=\"EUROPEANA_LANGUAGE\" value=\"\"/>\n" +
                "         </additions>\n" +
                "        <field-mappings>\n";
    }

    private static String createFooter() {
        return
                "        </field-mappings>\n" +
                "      </source>\n" +
                "   </sources>\n" +
                "   <type-mappings>\n" +
                "      <type-mapping type=\"TEXT\">\n" +
                "         <from>text</from>\n" +
                "      </type-mapping>\n" +
                "      <type-mapping type=\"IMAGE\">\n" +
                "         <from>image</from>\n" +
                "      </type-mapping>\n" +
                "      <type-mapping type=\"SOUND\">\n" +
                "         <from>sound</from>\n" +
                "      </type-mapping>\n" +
                "      <type-mapping type=\"VIDEO\">\n" +
                "         <from>video</from>\n" +
                "      </type-mapping>\n" +
                "   </type-mappings>\n" +
                "</profile>\n";
    }

    private static String createMapping(String line) {
        Pattern replacePattern = Pattern.compile("^(.*?)[\\s]*?=>[\\s]*(.*):(.*)[\\s]*$");
        Matcher replaceMatcher = replacePattern.matcher(line);
        if (!replaceMatcher.matches()) {
            throw new RuntimeException("No match ["+line+"]");
        }
        String from = replaceMatcher.group(1).trim();
        String toNamespace = replaceMatcher.group(2).trim();
        String toLocalName = replaceMatcher.group(3).trim();
        String mappedLine =
                "            <field-mapping from=\""+from+"\">\n" +
                "               <to key=\""+toNamespace+"_"+toLocalName+"\"/>\n";
        if (mappedLine.contains("(ID)") || mappedLine.contains("(YEAR)") || mappedLine.contains("(TYPE)")) {
            mappedLine = createConverters(mappedLine);
        }
        if (mappedLine.contains("_||_")) {
            mappedLine = mappedLine.replace("_||_", "\"/>\n               <to key=\"");
            mappedLine = mappedLine.replace("europeana:", "europeana_");
        }
        Pattern toKeyPattern = Pattern.compile("<to key=\"(.*)\"/>");
        Matcher matcher = toKeyPattern.matcher(mappedLine);
        while (matcher.find()) {
            mappedLine = mappedLine.replaceAll(matcher.group(1), matcher.group(1).toUpperCase());
            mappedLine = insertRecordFieldUnderscores(mappedLine);
        }
        mappedLine += "            </field-mapping>\n";
        return mappedLine;
    }

    private static String createEmptyMapping(String thisLine) {
        return thisLine.replaceAll(
                "^(.*)[\\s]{2,5}=>[\\s]{2,3}N/A",
                "            <field-mapping key=\"$1\"/>\n"
        );
    }

    private static String insertRecordFieldUnderscores(String mappedLine) {
        String correctedLine = mappedLine;
        if (correctedLine.contains("ISSHOWN")) {
            correctedLine = correctedLine.replace("ISSHOWN", "IS_SHOWN_");
        }
        else if (correctedLine.contains("CONFORMSTO")) {
            correctedLine = correctedLine.replace("CONFORMSTO", "CONFORMS_TO");
        }
        else if (correctedLine.contains("_HAS")) {
            correctedLine = correctedLine.replace("_HAS", "_HAS_");
        }
        else if (correctedLine.contains("ISPART")) {
            correctedLine = correctedLine.replace("ISPART", "IS_PART_");
        }
        else if (correctedLine.contains("ISFORMAT")) {
            correctedLine = correctedLine.replace("ISFORMAT", "IS_FORMAT_");
        }
        else if (correctedLine.contains("ISREFERENCED")) {
            correctedLine = correctedLine.replace("ISREFERENCED", "IS_REFERENCED_");
        }
        else if (correctedLine.contains("ISREPLACED")) {
            correctedLine = correctedLine.replace("ISREPLACED", "IS_REPLACED_");
        }
        else if (correctedLine.contains("ISREQUIRED")) {
            correctedLine = correctedLine.replace("ISREQUIRED", "IS_REQUIRED_");
        }
        else if (correctedLine.contains("ISVERSION")) {
            correctedLine = correctedLine.replace("ISVERSION", "IS_VERSION_");
        }
        else if (correctedLine.contains("TABLEOF")) {
            correctedLine = correctedLine.replace("TABLEOF", "TABLE_OF_");
        }
        return correctedLine;
    }

    private static String createConverters(String thisLine) {
        String mappedLine = null;
        if (thisLine.contains("(ID)")) {
            mappedLine = thisLine.replaceAll("[\\s]*\\(ID\\)", "");
            mappedLine += "               <to key=\"EUROPEANA_URI\"/>\n";
        }
        else if (thisLine.contains("(YEAR)")) {
            mappedLine = thisLine.replaceAll("[\\s]*\\(YEAR\\)", "");
            mappedLine += "               <to converter=\"YearExtractor\" key=\"EUROPEANA_YEAR\"/>\n";
        }
        else if (thisLine.contains("(TYPE)")) {
            mappedLine = thisLine.replaceAll("[\\s]*\\(TYPE\\)", "");
            mappedLine += "               <to key=\"EUROPEANA_TYPE\"/>\n";
        }
        return mappedLine;
    }

    @SuppressWarnings({"NestedAssignment"})
    private static void convertMappingToProfile(File inputFile, Boolean singleFile) {
        try {
            BufferedReader in = new BufferedReader(new FileReader(inputFile));
            String outputFileName = inputFile.getParentFile() + File.separator + "profile.xml";
            if (!singleFile) {
                outputFileName = inputFile.toString().replace("_mapping.txt", "") + "_profile.xml";
            }
            FileWriter writer = new FileWriter(outputFileName);
            writer.append(createHeader(inputFile.getName()));
            String thisLine;
            while ((thisLine = in.readLine()) != null) {
                lines++;
                if (lines > NUMBER_OF_HEADER_LINES && !thisLine.matches("^$")) {
                    if (thisLine.contains("N/A")) {
                        writer.append(createEmptyMapping(thisLine));
                    }
                    else if (thisLine.contains(" => ")) {
                        writer.append(createMapping(thisLine));
                    }
                    else {
                        writer.append(thisLine.replaceAll("#(.*)$", "            <!-- $1 -->\n"));
                    }
                }
            }
            writer.append(createFooter());
            lines = 0;
            in.close();
            writer.close();

        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processDirectory(File folder) {
        File[] listOfFiles = folder.listFiles();
        int numberOfFiles = 0;
        for (File inputFile : listOfFiles) {
            if (inputFile.isFile() && inputFile.getName().endsWith("_mapping.txt")) {
                numberOfFiles++;
                convertMappingToProfile(inputFile, false);
                System.out.println(new StringBuilder().append("Processing ").append(inputFile.getName()).toString());
            }
        }
        System.out.println("Processed " + numberOfFiles + " files.");

    }

    private static void processFile(File file) {
        if (file.getName().endsWith("_mapping.txt")) {
            convertMappingToProfile(file, true);
            System.out.println("Finished processing " + file.toString());
        }
        else {
            System.out.println("Please make sure that the input file end with '_mapping.txt'.");
        }
    }

    public static void main(String[] args) {
        File input = new File(args[0]);
        if (input.isFile()) {
            processFile(input);
        }
        else if (input.isDirectory()) {
            processDirectory(input);
        }
        else {
            System.out.println("Unable to handle input " + args[0] + ". Please specify a input file or folder");
        }
    }
}