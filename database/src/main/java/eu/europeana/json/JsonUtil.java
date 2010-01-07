package eu.europeana.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * This is a group of utility functions useful for interpreting information from a JSON tree.
 *
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

@Deprecated
public class JsonUtil {

    public static Integer getInteger(JSONObject jsonObject, String... name) throws JSONException {
        for (String n : name) {
            if (has(jsonObject, n)) {
                Object object = get(jsonObject, n);
                if (object instanceof Integer) {
                    return (Integer) object;
                }
                else {
                    throw new JSONException("Can't handle type: " + object.getClass().getName());
                }
            }
        }
        throw new JSONException("No integer value found for any of: " + getNameList(name) + " in " + jsonObject);
    }

    public static Boolean getBoolean(JSONObject jsonObject, Default defaultValue, String... name) throws JSONException {
        for (String n : name) {
            if (has(jsonObject, n)) {
                Object object = get(jsonObject, n);
                if (object instanceof Boolean) {
                    return (Boolean) object;
                }
//                else if (object instanceof JSONArray) {
//                    return getString((JSONArray) object);
//                }
                else {
                    throw new JSONException("Can't handle type: " + object.getClass().getName());
                }
            }
            else {
                if (defaultValue != Default.ERROR) {
                    return Boolean.parseBoolean(defaultValue.toString());
                }
            }
        }
        throw new JSONException("No string value found for any of: " + getNameList(name) + " in " + jsonObject);
    }

    public static String getString(JSONObject jsonObject, Default defaultValue, String... name) throws JSONException {
        for (String n : name) {
            if (has(jsonObject, n)) {
                Object object = get(jsonObject, n);
                if (object instanceof String) {
                    return (String) object;
                }
                else if (object instanceof JSONArray) {
                    return getString((JSONArray) object);
                }
                else {
                    throw new JSONException("Can't handle type: " + object.getClass().getName());
                }
            }
            else {
                if (defaultValue != Default.ERROR) {
                    return defaultValue.toString();
                }
            }
        }
        throw new JSONException("No string value found for any of: " + getNameList(name) + " in " + jsonObject);
    }

    public static String[] getStringArray(JSONObject jsonObject, Default defaultValue, boolean insertHref, String... names) throws JSONException {
        List<String> result = new ArrayList<String>();
        for (String name : names) {
            if (has(jsonObject, name)) {
                Object object = get(jsonObject, name);
                if (object instanceof String) {
                    if (!insertHref) {
                        result.add((String) object);
                    }
                    else {
                        result.add(insertAHrefs((String) object));
                    }
                }
                else if (object instanceof JSONArray) {
                    JSONArray array = (JSONArray) object;
                    for (int walk = 0; walk < array.length(); walk++) {
                        if (!insertHref) {
                            result.add((String) array.get(walk));
                        }
                        else {
                            result.add(insertAHrefs((String) array.get(walk)));
                        }

                    }
                }
                else {
                    throw new JSONException("Can't handle type: " + object.getClass().getName());
                }
            }
        }
        if (result.isEmpty() && defaultValue != Default.ERROR) {
            result.add(defaultValue.toString());
        }
        String[] answer = new String[result.size()];
        return result.toArray(answer);
    }

    /*
    * This method finds embedded links inside a string and makes it clickable
    */
    static String insertAHrefs(String jsonString) {
        // todo find better way to check for url
        if (!jsonString.contains("http://") && !jsonString.contains("https://")) {
            return jsonString;
        }
        StringBuilder out = new StringBuilder();
        // split string by whitespace
        String[] tokens = jsonString.split(" ");
        for (String token : tokens) {
            if (token.startsWith("http://") || token.startsWith("https://")) {
                out.append("<a href=\"").append(token.replaceAll("[\\.,!:]$", "")).append("\">");
                out.append(token).append("</a>").append(" ");
            }
            else {
                out.append(token).append(" ");
            }
        }
        return out.toString().trim();
    }

    public static String getString(JSONObject jsonObject, String... name) throws JSONException {
        return getString(jsonObject, Default.ERROR, name);
    }

    public static String getThing(JSONObject jsonObject, String... name) throws JSONException {
        for (String n : name) {
            if (has(jsonObject, n)) {
                Object object = get(jsonObject, n);
                if (object instanceof String || object instanceof Double || object instanceof Integer) {
                    return object.toString();
                }
                else if (object instanceof JSONArray) {
                    return getString((JSONArray) object);
                }
                else {
                    throw new JSONException("Can't handle type: " + object.getClass().getName());
                }
            }
        }
        throw new JSONException("No string value found for any of: " + getNameList(name) + " in " + jsonObject);
    }

    public static String getString(JSONArray jsonArray) throws JSONException {
        StringBuilder out = new StringBuilder();
        for (int walk = 0; walk < jsonArray.length(); walk++) {
            out.append(jsonArray.get(walk)).append(' ');
        }
        return out.toString().trim();
    }

    public static boolean has(JSONObject jsonObject, String nameString) throws JSONException {
        if (nameString.endsWith("_")) {
            JSONArray names = jsonObject.names();
            for (int walk = 0; walk < names.length(); walk++) {
                String name = (String) names.get(walk);
                if (name.startsWith(nameString)) {
                    return true;
                }
            }
            return false;
        }
        else {
            return jsonObject.has(nameString);
        }
    }

    public static Object get(JSONObject jsonObject, String nameString) throws JSONException {
        if (nameString.endsWith("_")) {
            JSONArray names = jsonObject.names();
            for (int walk = 0; walk < names.length(); walk++) {
                String name = (String) names.get(walk);
                if (name.startsWith(nameString)) {
                    return jsonObject.get(name);
                }
            }
            throw new JSONException("Expected to find " + nameString);
        }
        else {
            return jsonObject.get(nameString);
        }
    }

    public static String getNameList(String... name) {
        StringBuilder out = new StringBuilder();
        for (String n : name) {
            out.append('"').append(n).append('"').append(' ');
        }
        return out.toString();
    }

    public enum Default {
        ERROR("ERROR"),
        UNKNOWN(" "), // used to be unknown now just space
        FALSE("false"),
        DATE_DEFAULT("0000");

        private String string;

        Default(String string) {
            this.string = string;
        }

        public String toString() {
            return string;
        }
    }
}