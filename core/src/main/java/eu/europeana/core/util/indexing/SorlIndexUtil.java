package eu.europeana.core.util.indexing;

import eu.europeana.core.database.UserDao;
import eu.europeana.core.database.domain.SocialTag;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @since Oct 4, 2010 12:56:13 AM
 */
public class SorlIndexUtil {
    private final String USER_TAG = "europeana_userTag";

    @Autowired
    @Qualifier("solrUpdateServer")
    private SolrServer solrServer;

    @Autowired
    private UserDao userDao;

    public boolean indexUserTags(String europeanaUri) {
        boolean success = false;
        List<SocialTag> socialTags = userDao.fetchAllSocialTags(europeanaUri);
        try {
            QueryResponse query = solrServer.query(new SolrQuery("europeana_uri:" + europeanaUri));
            if (query.getResults().size() != 0) {
                SolrDocument doc = query.getResults().get(0);
                final SolrInputDocument outputDoc = convertToInputDocument(doc);
                for (SocialTag socialTag : socialTags) {
                    outputDoc.addField(USER_TAG, socialTag.getTag());
                }
                solrServer.add(outputDoc);
                success = true;
            }
            // todo add better error handling
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return success;
    }

    private SolrInputDocument convertToInputDocument(SolrDocument inputDoc) {
        SolrInputDocument outputDoc = new SolrInputDocument();
        inputDoc.removeFields(USER_TAG);
        final Map<String, Object> fieldValueMap = inputDoc.getFieldValueMap();
        for (String field : fieldValueMap.keySet()) {
            final Collection<Object> fieldValues = inputDoc.getFieldValues(field);
            for (Object fieldValue : fieldValues) {
                outputDoc.addField(field, fieldValue);
            }
        }
        return outputDoc;
    }
}
