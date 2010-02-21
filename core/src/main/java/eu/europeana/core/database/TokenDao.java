package eu.europeana.core.database;

import eu.europeana.core.database.domain.Token;
import eu.europeana.core.querymodel.query.EuropeanaQueryException;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;


/**
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 * @author Cesare Corcordia <cesare.concordia@isti.cnr.it>
 */

public interface TokenDao extends PersistentTokenRepository {

    public Token createNewToken(String email);

    public void removeToken(Token token);

    public Token getToken(String token) throws EuropeanaQueryException;

    public long countTokens();

    public Token getTokenByEmail(String email);

    public boolean isOlderThan(Token token, long time);

}
