package eu.delving.web.controller;

import java.io.File;
import java.util.Map;

/**
 * Fetch changes from mongo for message files
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MessageRefactor {
    private static final String PROPERTIES_EXTENSION = ".properties";

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
//        Map<String, Map<String, String>> messageFileMaps = new TreeMap<String, Map<String,String>>();
        for (File messageFile : messageDirectory.listFiles()) {
            if (messageFile.getName().endsWith(PROPERTIES_EXTENSION)) {
                Map<String,String> messageMap = MessageFileUtil.readMap(messageFile);
                MessageFileUtil.writeMap(messageMap, messageFile);
//                messageFileMaps.put(messageFile.getName(), messageMap);
            }
        }
    }
}
