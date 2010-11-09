package eu.delving.core.util;

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
 * read and write message files using sorted tree maps
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class MessageFileUtil {

    public static Map<String, String> readMap(File file) throws IOException {
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
                throw new IOException(String.format("No equals sign found in %s: %s", file.getAbsolutePath(), line));
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
        br.close();
        return map;
    }

    public static void writeMap(Map<String, String> map, File file) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        for (Map.Entry<String, String> entry : map.entrySet()) {
            out.write(String.format("%s=%s\n", entry.getKey(), entry.getValue()));
        }
        out.close();
    }

}
