package eu.europeana.web.util;

import eu.europeana.query.FacetType;
import eu.europeana.query.QueryModel;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 */

public class QueryConstraints implements QueryModel.Constraints {
    public static final String PARAM_KEY = "qf";
    private static final String PROMPT = "&"+PARAM_KEY+"=";
    private List<ConstraintEntry> entries = new ArrayList<ConstraintEntry>();

    public QueryConstraints(String [] constraintStrings) {
        if (constraintStrings != null) {
            for (String constraint: constraintStrings) {
                String [] facetValue = constraint.split(":");
                FacetType facetType;
                try {
                    facetType = FacetType.valueOf(facetValue[0].toUpperCase()); // added to uppercase
                } catch (IllegalArgumentException e) {
                    continue;
                }
                // is this redundant ?
                if (facetType == null) {
                    continue;
                }
                entries.add(new ConstraintEntry(facetType,facetValue[1]));
            }
        }
    }

    public String toQueryString() throws UnsupportedEncodingException {
        StringBuilder out = new StringBuilder();
        for (ConstraintEntry entry : entries) {
            appendToURI(out, entry.getFacetType().toString(), entry.getValue());
        }
        return out.toString();
    }

    public List<Breadcrumb> toBreadcrumbs(String param, String value) throws UnsupportedEncodingException {
        List<Breadcrumb> breadcrumbs = new ArrayList<Breadcrumb>();
        String prefix = param + "=" + value;
        breadcrumbs.add(new Breadcrumb(prefix,value));
        for (int walk=0; walk<entries.size(); walk++) {
            StringBuilder out = new StringBuilder(prefix);
            int count = walk;
            for (ConstraintEntry entry : entries) {
                appendToURI(out, entry.getFacetType().toString(), entry.getValue());
                if (count-- == 0) {
                    breadcrumbs.add(new Breadcrumb(out.toString(), entry.getFacetType()+":"+entry.getValue()));
                    break;
                }
            }
        }
        breadcrumbs.get(breadcrumbs.size()-1).flagAsLast();
        return breadcrumbs;
    }

    public static void appendToURI(StringBuilder uri, String name, String value) throws UnsupportedEncodingException {
        uri.append(PROMPT).append(name).append(":").append(URLEncoder.encode(value, "utf-8"));
    }

    public List<FacetType> getFacetTypes() {
        boolean [] seen = new boolean[FacetType.values().length];
        List<FacetType> facetTypes = new ArrayList<FacetType>();
        for (ConstraintEntry entry : entries) {
            if (!seen[entry.getFacetType().ordinal()]) {
                facetTypes.add(entry.getFacetType());
                seen[entry.getFacetType().ordinal()] = true;
            }
        }
        return facetTypes;
    }

    public List<String> getConstraint(FacetType type) {
        List<String> constraint = new ArrayList<String>();
        for (ConstraintEntry entry : entries) {
            if (entry.getFacetType() == type) {
                constraint.add(entry.getValue());
            }
        }
        return constraint;
    }

    public List<? extends Entry> getEntries() {
        return entries;
    }

    public static class Breadcrumb {
        private String href;
        private String display;
        private boolean last;

        public Breadcrumb(String href, String display) {
            this.href = href;
            this.display = display;
        }

        public void flagAsLast() {
            this.last = true;
        }

        public String getDisplay() {
            return display;
        }

        public String getHref() {
            return href;
        }

        public boolean getLast() {
            return last;
        }
    }

    private static class ConstraintEntry implements Entry {
        private FacetType facetType;
        private String value;

        private ConstraintEntry(FacetType facetType, String value) {
            this.facetType = facetType;
            this.value = value;
        }

        public FacetType getFacetType() {
            return facetType;
        }

        public String getValue() {
            return value;
        }
    }
}