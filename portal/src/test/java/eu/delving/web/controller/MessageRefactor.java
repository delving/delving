package eu.delving.web.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fetch changes from mongo for message files
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MessageRefactor {
    private static final String PROPERTIES_EXTENSION = ".properties";

    private static void gatherFiles(File directory, Set<File> files) {
        if ("target".equals(directory.getName())) {
            return;
        }
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                gatherFiles(file, files);
            }
            else if (file.getName().endsWith(".java") || file.getName().endsWith(".scala") || file.getName().endsWith(".ftl")) {
                files.add(file);
            }
        }
    }

    private static List<String> readFile(File file) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(file));
        List<String> lines = new ArrayList<String>();
        String line;
        while ((line = in.readLine()) != null) {
            lines.add(line);
        }
        in.close();
        return lines;
    }

    private static class Counter {
        String key;
        int count;

        private Counter(String key) {
            this.key = key;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("MessageFileUpdater <portal-path> <message-file-path>");
            System.exit(0);
        }
        String portalPath = args[0];
        String messageFilePath = args[1];
        File portalDirectory = new File(portalPath);
        if (!portalDirectory.exists() || !"portal".equals(portalDirectory.getName()) || !new File(portalDirectory, "src/main").exists()) {
            System.out.println("First parameter " + portalPath + " must be the directory containing the portal project");
            System.exit(1);
        }
        File messageDirectory = new File(messageFilePath);
        if (!messageDirectory.exists() || !messageDirectory.isDirectory() || !new File(messageDirectory, "messages.properties").exists()) {
            System.out.println("Second parameter " + messageFilePath + " must be the directory containing messages*.properties files");
            System.exit(1);
        }
        Map<String, Map<String, String>> messageFileMaps = new TreeMap<String, Map<String,String>>();
        for (File messageFile : messageDirectory.listFiles()) {
            if (messageFile.getName().endsWith(PROPERTIES_EXTENSION)) {
                Map<String,String> messageMap = MessageFileUtil.readMap(messageFile);
                messageFileMaps.put(messageFile.getName(), messageMap);
            }
        }
        Set<String> allKeys = new TreeSet<String>();
        for (Map<String,String> messageMap : messageFileMaps.values()) {
            allKeys.addAll(messageMap.keySet());
        }
        System.out.println("\n\nMissing Keys:");
        for (Map.Entry<String,Map<String,String>> entry : messageFileMaps.entrySet()) {
            Set<String> missingKeys = new TreeSet<String>(allKeys);
            missingKeys.removeAll(entry.getValue().keySet());
            if (!missingKeys.isEmpty()) {
                for (String key : missingKeys) {
                    System.out.println(String.format("%s: %s", entry.getKey(), key));
                }
            }
        }
        System.out.println("\n\nOccurrences:");
        Map<String, Counter> counters = new TreeMap<String,Counter>();
        Set<File> files = new TreeSet<File>();
        gatherFiles(new File(portalDirectory, "src/main"), files);
        for (File file : files) {
            List<String> lines = readFile(file);
            int lineNumber = 0;
            for (String line : lines) {
                lineNumber++;
                for (String key : allKeys) {
                    Matcher matcher = Pattern.compile(key).matcher(line);
                    while (matcher.find()) {
                        System.out.println(String.format(
                                "%s:%d> %s [%s]",
                                file.getName(),
                                lineNumber,
                                key,
                                line
                        ));
                        Counter counter = counters.get(key);
                        if (counter == null) {
                            counter = new Counter(key);
                            counters.put(key, counter);
                        }
                        counter.count++;
                    }
                }
            }
        }
        System.out.println("\n\nUsages:");
        for (Counter counter : counters.values()) {
            System.out.println(String.format("%s : %d", counter.key, counter.count));
        }
        allKeys.removeAll(counters.keySet());
        System.out.println("\n\nUnused:");
        for (String key : allKeys) {
            System.out.println(key);
        }
    }
}
