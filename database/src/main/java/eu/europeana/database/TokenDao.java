package eu.europeana.database;

import java.util.Date;
import org.springframework.security.ui.rememberme.PersistentRememberMeToken;
import eu.europeana.database.domain.Token;
import eu.europeana.query.EuropeanaQueryException;



public interface TokenDao {

   
    public void createNewToken(PersistentRememberMeToken persistentRememberMeToken);

    public void updateToken(String series, String tokenValue, Date lastUsed) ;

    public PersistentRememberMeToken getTokenForSeries(String series);
   
    public void removeUserTokens(String username);
    

    public Token createNewToken(String email);

   
    public void removeToken(Token token);
    
    public Token getToken(String token) throws EuropeanaQueryException ;
    

    public long countTokens() ;
    
    public Token getTokenByEmail(String email) ;

    public boolean isOlderThan(Token token, long time) ;

}
