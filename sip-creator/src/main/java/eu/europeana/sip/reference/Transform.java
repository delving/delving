package eu.europeana.sip.reference;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An enumeration of the transforms available
 *
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 */

@Deprecated
public enum Transform implements FieldTransform {

    NONE(new FieldTransform() {
        @Override
        public String[] parameterNames() {
            return new String[0];
        }

        @Override
        public String transform(String string, String[] parameters) throws TransformException {
            return string;
        }
    }),

    DELIMITED(new FieldTransform() {
        @Override
        public String[] parameterNames() {
            return new String[]{"Delimiter"};
        }

        @Override
        public String transform(String string, String[] parameters) throws TransformException {
            String[] parts = string.split(parameters[0]);
            StringBuilder whole = new StringBuilder();
            int pipes = parts.length - 1;
            for (String part : parts) {
                whole.append(part);
                if (pipes > 0) {
                    whole.append(PIPE);
                    pipes--;
                }
            }
            return whole.toString();
        }
    }),

    SURROUND(new FieldTransform() {
        @Override
        public String[] parameterNames() {
            return new String[]{"Prefix", "Suffix"};
        }

        @Override
        public String transform(String string, String[] parameters) throws TransformException {
            return parameters[0] + string + parameters[1];
        }
    }),


    SURROUND_DELIMITED(new FieldTransform() {
        @Override
        public String[] parameterNames() {
            return new String[]{"Prefix", "Suffix", "Delimiter"};
        }

        @Override
        public String transform(String string, String[] parameters) throws TransformException {
            String[] parts = string.split(parameters[2]);
            StringBuilder whole = new StringBuilder();
            int pipes = parts.length - 1;
            for (String part : parts) {
                whole.append(parameters[0]).append(part).append(parameters[1]);
                if (pipes > 0) {
                    whole.append(PIPE);
                    pipes--;
                }
            }
            return whole.toString();
        }
    }),

    SURROUND_FIRST_DELIMITED(new FieldTransform() {
        @Override
        public String[] parameterNames() {
            return new String[]{"Prefix", "Suffix", "Delimiter"};
        }

        @Override
        public String transform(String string, String[] parameters) throws TransformException {
            String[] parts = string.split(parameters[2]);
            return parameters[0] + parts[0] + parameters[1];
        }
    }),

    SURROUND_REPLACE(new FieldTransform() {
        @Override
        public String[] parameterNames() {
            return new String[]{"Prefix", "Suffix", "From", "To"};
        }

        @Override
        public String transform(String string, String[] parameters) throws TransformException {
            string = parameters[0] + string + parameters[1];
            return string.replace(parameters[2], parameters[3]);
        }
    }),

    SURROUND_SELECT_DELIMITED(new FieldTransform() {
        @Override
        public String[] parameterNames() {
            return new String[]{"Prefix", "Suffix", "Delimiter", "Selector"};
        }

        @Override
        public String transform(String string, String[] parameters) throws TransformException {
            String[] parts = string.split(parameters[2]);
            for (String part : parts) {
                if (part.matches(parameters[3])) {
                    return parameters[0] + part + parameters[1];
                }
            }
            throw new TransformException("Value [" + string + "] does not contain selector [" + parameters[3] + "]");
        }
    }),

    SURROUND_SELECT_DELIMITED_DEFAULT(new FieldTransform() {
        @Override
        public String[] parameterNames() {
            return new String[]{"Prefix", "Suffix", "Delimiter", "Selector", "Default Value"};
        }

        @Override
        public String transform(String string, String[] parameters) throws TransformException {
            String[] parts = string.split(parameters[2]);
            for (String part : parts) {
                if (part.matches(parameters[3])) {
                    return parameters[0] + part + parameters[1];
                }
            }
            return parameters[0] + parameters[4] + parameters[1];
        }
    }),

    REPLACE(new FieldTransform() {
        @Override
        public String[] parameterNames() {
            return new String[]{"From", "To"};
        }

        @Override
        public String transform(String string, String[] parameters) throws TransformException {
            return string.replace(parameters[0], parameters[1]);
        }
    }),


    SELECT_IF_CONTAINS(new FieldTransform() {
        @Override
        public String[] parameterNames() {
            return new String[]{"Selector"};
        }

        @Override
        public String transform(String string, String[] parameters) throws TransformException {
            if (string.contains(parameters[0])) {
                return string;
            }
            else {
                return "";
            }
        }
    }),

    EXTRACT_YEAR(new FieldTransform() {
        private final Pattern PATTERN = Pattern.compile("\\d{4}");

        @Override
        public String[] parameterNames() {
            return new String[]{"Selector"};
        }

        @Override
        public String transform(String string, String[] parameters) throws TransformException {
            Matcher matcher = PATTERN.matcher(string);
            if (matcher.find()) {
                return matcher.group();
            }
            return "0000";
        }
    });


    private FieldTransform fieldTransform;

    Transform(FieldTransform fieldTransform) {
        this.fieldTransform = fieldTransform;
    }

    @Override
    public String[] parameterNames() {
        return fieldTransform.parameterNames();
    }

    @Override
    public String transform(String string, String[] parameters) throws TransformException {
        return fieldTransform.transform(string, parameters);
    }
}
