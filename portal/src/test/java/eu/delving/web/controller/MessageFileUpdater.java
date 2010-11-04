package eu.delving.web.controller;

import com.mongodb.Mongo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.TreeMap;

/**
 * Fetch changes from mongo for message files
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MessageFileUpdater {

    private static Map<String, String> readMap(File file) throws IOException {
        Map<String, String> map = new TreeMap<String, String>();
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("#") || line.isEmpty()) {
                continue;
            }
            int equals = line.indexOf("=");
            if (equals < 0) {
                throw new IOException("No equals sign found: " + line);
            }
            String key = line.substring(0, equals).trim();
            StringBuilder value = new StringBuilder();
            String valuePart = line.substring(equals + 1).trim();
            while (valuePart.endsWith("\\")) {
                value.append(valuePart.substring(0, valuePart.length() - 1));
                value.append(' ');
                valuePart = br.readLine().trim();
            }
            value.append(valuePart);
            map.put(key, value.toString().trim());
        }
        return map;
    }

    private static void writeMap(Map<String, String> map, File file) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            out.write(String.format("%s=%s\n", entry.getKey(), entry.getValue()));
        }
        out.close();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.out.println("MessageFileUpdater <database-name> <message-file-directory>");
            System.exit(0);
        }
        String databaseName = args[0];
        String messageFileDirectory = args[1];
        File directory = new File(messageFileDirectory);
        if (!directory.exists() || !directory.isDirectory() || !new File(directory, "messages.properties").exists()) {
            System.out.println("Second parameter " + messageFileDirectory + " must be the directory containing messages*.properties files");
            System.exit(1);
        }
        Mongo mongo = new Mongo();
        MessageSourceRepo repo = new MessageSourceRepo();
        repo.setMongo(mongo);
        repo.setDatabaseName(databaseName);
        Map<String, Map<String, String>> messageFileMaps = repo.getMessageFileMaps();
        for (Map.Entry<String, Map<String, String>> messageFileEntry : messageFileMaps.entrySet()) {
            File messageFile = new File(directory, messageFileEntry.getKey());
            if (!messageFile.exists()) {
                throw new Exception("File doesn't exist! " + messageFile.getAbsolutePath());
            }
            Map<String,String> existing = readMap(messageFile);
            Map<String, String> changes = messageFileEntry.getValue();
            for (Map.Entry<String, String> entry : changes.entrySet()) {
                System.out.println(String.format(
                        "Changing %s to %s in file %s",
                        entry.getKey(),
                        entry.getValue(),
                        messageFile.getAbsolutePath()
                ));
                existing.put(entry.getKey(), entry.getValue());
            }
            writeMap(existing, messageFile);
        }
    }
}
