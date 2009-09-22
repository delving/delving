package eu.europeana.json;

import eu.europeana.query.QueryModel;
import eu.europeana.query.QueryModelFactory;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 */

public class JsonQueryModelFactory implements QueryModelFactory {

    private QueryModelFactory simpleQueryModelFactory;
    private QueryModelFactory advancedQueryModelFactory;

    public void setSimpleQueryModelFactory(QueryModelFactory simpleQueryModelFactory) {
        this.simpleQueryModelFactory = simpleQueryModelFactory;
    }

    public void setAdvancedQueryModelFactory(QueryModelFactory advancedQueryModelFactory) {
        this.advancedQueryModelFactory = advancedQueryModelFactory;
    }

    public QueryModel createQueryModel(SearchType searchType) {
        QueryModel queryModel = null;
        switch (searchType) {
            case SIMPLE:
                queryModel = simpleQueryModelFactory.createQueryModel(SearchType.SIMPLE);
                break;
            case ADVANCED:
                queryModel = advancedQueryModelFactory.createQueryModel(SearchType.ADVANCED);
                break;
        }
        return queryModel;
    }
}