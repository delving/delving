package eu.europeana.web.util;

import org.apache.lucene.search.Query;
import org.osuosl.srw.SRWDiagnostic;
import org.osuosl.srw.lucene.LuceneTranslator;
import org.z3950.zing.cql.CQLNode;
import org.z3950.zing.cql.CQLParseException;
import org.z3950.zing.cql.CQLParser;

import java.io.IOException;
import java.util.HashMap;

@Deprecated
public class CQL2Lucene
{
	private static final HashMap<String, String> SUPPORTED_SEARCH_FIELDS = new HashMap<String, String>();

	static
	{
		SUPPORTED_SEARCH_FIELDS.put("title", "dc_title");
		SUPPORTED_SEARCH_FIELDS.put("author", "dc_creator");
		SUPPORTED_SEARCH_FIELDS.put("language", "dc_language");
		SUPPORTED_SEARCH_FIELDS.put("date", "dc_date");
		SUPPORTED_SEARCH_FIELDS.put("subject", "dc_subject");
		SUPPORTED_SEARCH_FIELDS.put("cql.serverChoice:", ""); // maybe place text: here later
	}

	private CQL2Lucene()
	{
		//nothing; Utility class
	}

	public static String translate(String cqlQuery) throws SRWDiagnostic, IOException, CQLParseException {
        CQLNode root = new CQLParser().parse(cqlQuery);
        final Query luceneQuery = LuceneTranslator.makeQuery(root);
		return replaceAll(luceneQuery.toString());
	}

	private static String replaceAll(String queryString)
	{
		for (String replace : SUPPORTED_SEARCH_FIELDS.keySet())
		{
			queryString = queryString.replaceAll(replace, SUPPORTED_SEARCH_FIELDS.get(replace));
		}
		return queryString;
	}
}