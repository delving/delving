package eu.europeana.database;

import eu.europeana.database.domain.Token;
import eu.europeana.query.EuropeanaQueryException;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;


public interface TokenDao extends PersistentTokenRepository {

    public Token createNewToken(String email);

    public void removeToken(Token token);

    public Token getToken(String token) throws EuropeanaQueryException;

    public long countTokens();

    public Token getTokenByEmail(String email);

    public boolean isOlderThan(Token token, long time);

}
