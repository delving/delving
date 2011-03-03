/*
 * Copyright 2010 DELVING BV
 *
 * Licensed under the EUPL, Version 1.1 or as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.delving.core.util;

import java.io.File;
import java.util.Map;

/**
 * Fetch changes from mongo for message files
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MessageFileUpdater {

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
        MessageSourceRepo repo = new MessageSourceRepo();
        final MongoFactory mongoFactory = new MongoFactory();
        mongoFactory.setTestContext("true");
        repo.setMongoFactory(mongoFactory);
        repo.setDatabaseName(databaseName);
        Map<String, Map<String, String>> messageFileMaps = repo.getMessageFileMaps();
        for (Map.Entry<String, Map<String, String>> messageFileEntry : messageFileMaps.entrySet()) {
            File messageFile = new File(directory, messageFileEntry.getKey());
            if (!messageFile.exists()) {
                throw new Exception("File doesn't exist! " + messageFile.getAbsolutePath());
            }
            Map<String,String> existing = MessageFileUtil.readMap(messageFile);
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
            MessageFileUtil.writeMap(existing, messageFile);
        }
    }
}
