package eu.europeana.json;

import eu.europeana.query.EuropeanaQueryException;
import eu.europeana.query.QueryExpression;
import eu.europeana.query.RecordField;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanQuery;

import java.util.regex.Pattern;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class SolrQueryExpression implements QueryExpression {
    private static final Pattern OR_PATTERN = Pattern.compile("\\s+[oO][rR]\\s+");
    private static final Pattern AND_PATTERN = Pattern.compile("\\s+[aA][nN][dD]\\s+");
    private static final Pattern NOT_START_PATTERN = Pattern.compile("^\\s*[nN][oO][tT]\\s+");
    private static final Pattern NOT_MIDDLE_PATTERN = Pattern.compile("\\s+[nN][oO][tT]\\s+");
    private String originalQuery;
    private Query query;
    private QueryType queryType = QueryType.SIMPLE_QUERY;
    private boolean moreLikeThis = false;

    public SolrQueryExpression(String queryString) throws EuropeanaQueryException {
        this.originalQuery = sanitize(queryString);
        try {
            QueryParser queryParser = new QueryParser("text", new StandardAnalyzer());
            queryParser.setDefaultOperator(QueryParser.AND_OPERATOR);
            query = queryParser.parse(originalQuery);
            analyze(query);
        }
        catch (ParseException e) {
            throw new EuropeanaQueryException("Cannot parse [" + queryString + "]", e);
        }
    }

    String sanitize(String query) {
        StringBuilder out = new StringBuilder();
        for (int walk = 0; walk < query.length(); walk++) {
            char ch = query.charAt(walk);
            switch (ch) {
                case '{':
                case '}':
                    break;
                default:
                    out.append(ch);
            }
        }
        String q = out.toString();
        q = AND_PATTERN.matcher(q).replaceAll(" AND ");
        q = OR_PATTERN.matcher(q).replaceAll(" OR ");
        q = NOT_START_PATTERN.matcher(q).replaceAll("NOT ");
        q = NOT_MIDDLE_PATTERN.matcher(q).replaceAll(" NOT ");
        return q;
    }

    public String getQueryString() {
        return originalQuery;
    }

    public String getBackendQueryString() {
        return originalQuery;
//        switch (type) {
//            case SIMPLE_QUERY:
//            case MORE_LIKE_THIS_QUERY:
//                return originalQuery;
//            case ADVANCED_QUERY:
//                return query.toString();
//        }
//        throw new RuntimeException();
    }

    public QueryType getType() {
        return queryType;
    }

    public void setMoreLikeThis(boolean moreLikeThis) {
        this.moreLikeThis = moreLikeThis;
    }

    public boolean isMoreLikeThis() {
        return moreLikeThis;
    }

    private void analyze(Query query) throws EuropeanaQueryException {
        if (query instanceof TermQuery) {
            analyzeTermQuery((TermQuery) query);
        }
        else if (query instanceof MultiTermQuery) {
            analyzeMultiTermQuery((MultiTermQuery) query);
        }
        else if (query instanceof BooleanQuery) {
            analyzeBooleanQuery((BooleanQuery) query);
        }
        else if (query instanceof PrefixQuery) {
            analyzePrefixQuery((PrefixQuery) query);
        }
        else if (query instanceof PhraseQuery) {
            analyzePhraseQuery((PhraseQuery) query);
        }
        else if (query instanceof MultiPhraseQuery) {
            analyzeMultiPhraseQuery((MultiPhraseQuery) query);
        }
        else if (query instanceof RangeQuery) {
            analyzeRangeQuery((RangeQuery) query);
        }
        else if (query instanceof SpanQuery) {
            analyzeSpanQuery((SpanQuery) query);
        }
        // Sjoerd added this to support *:* parameter in the querybox
        else if (query instanceof MatchAllDocsQuery) {
            analyzeMatchAllDocsQuery((MatchAllDocsQuery) query);
        }
        else if (query instanceof ConstantScoreRangeQuery) {
            analyzeConstantScoreRangeQuery((ConstantScoreRangeQuery) query);
        }
        else {
            throw new EuropeanaQueryException("Unknown query class: " + query.getClass().getName());
        }
    }

    private void analyzeConstantScoreRangeQuery(ConstantScoreRangeQuery query) throws EuropeanaQueryException {
        queryType = QueryType.ADVANCED_QUERY;
//        throw new EuropeanaQueryException("There is no analysis for " + query.getClass().getName());
    }

    private void analyzeSpanQuery(SpanQuery query) throws EuropeanaQueryException {
        throw new EuropeanaQueryException("There is no analysis for " + query.getClass().getName());
    }

    private void analyzeRangeQuery(RangeQuery query) throws EuropeanaQueryException {
        throw new EuropeanaQueryException("There is no analysis for " + query.getClass().getName());
    }

    private void analyzeMultiPhraseQuery(MultiPhraseQuery query) throws EuropeanaQueryException {
        throw new EuropeanaQueryException("There is no analysis for " + query.getClass().getName());
    }

    private void analyzeMatchAllDocsQuery(MatchAllDocsQuery query) throws EuropeanaQueryException {
        queryType = QueryType.ADVANCED_QUERY;
        this.query = new TermQuery(new Term("*", "*"));
//        throw new EuropeanaQueryException("There is no analysis for " + query.getClass().getName());
    }

    private void analyze(Term term) {
        String field = term.field();
        if (!"text".equals(field)) {
            boolean europeanaField = RecordField.EUROPEANA_URI.toFieldNameString().equals(field);
            switch (queryType) {
                case SIMPLE_QUERY:
                    if (europeanaField) {
                        queryType = QueryType.MORE_LIKE_THIS_QUERY;
                    }
                    else {
                        queryType = QueryType.ADVANCED_QUERY;
                    }
                    break;
                case ADVANCED_QUERY:
                    if (europeanaField) {
                        queryType = QueryType.MORE_LIKE_THIS_QUERY;
                    }
                    break;
                case MORE_LIKE_THIS_QUERY:
                    break;
            }
        }
    }

    private void analyzePhraseQuery(PhraseQuery query) {
        Term[] terms = query.getTerms();
        for (Term term : terms) {
            analyze(term);
        }
    }

    private void analyzePrefixQuery(PrefixQuery query) {
        analyze(query.getPrefix());
    }

    private void analyzeTermQuery(TermQuery query) {
        analyze(query.getTerm());
    }

    private void analyzeMultiTermQuery(MultiTermQuery query) {
        analyze(query.getTerm());
    }

    private void analyzeBooleanQuery(BooleanQuery query) throws EuropeanaQueryException {
        BooleanClause[] clauses = query.getClauses();
        for (BooleanClause clause : clauses) {
//            System.out.println(clause.getOccur());
            if (BooleanClause.Occur.SHOULD.equals(clause.getOccur()) || BooleanClause.Occur.MUST_NOT.equals(clause.getOccur())) {
                queryType = QueryType.ADVANCED_QUERY;
            }
            Query subquery = clause.getQuery();
            analyze(subquery);
        }
    }
}
