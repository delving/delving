package eu.delving.core.storage;

import org.apache.log4j.Logger;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import java.util.Date;

/**
 * todo: authors? was author Vitali Kiruta
 */
public class TokenRepositoryService implements PersistentTokenRepository {

    private Logger logger = Logger.getLogger(getClass());

    @Override
    public void createNewToken(PersistentRememberMeToken persistentRememberMeToken) {
        AuthenticationToken token = new AuthenticationToken(persistentRememberMeToken);
//        entityManager.persist(token);
    }

    @Override
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        AuthenticationToken token = null; //entityManager.find(AuthenticationToken.class, series);
//        token.setToken(tokenValue);
//        token.setLastUsed(lastUsed);
    }

    @Override
    public PersistentRememberMeToken getTokenForSeries(String series) {
//        Query query = entityManager.createQuery("select t from AuthenticationToken as t where t.series = :series");
//        query.setParameter("series", series);
//        try {
        AuthenticationToken token = (AuthenticationToken) null; //query.getSingleResult();
        return new PersistentRememberMeToken(
                token.getUsername(),
                token.getSeries(),
                token.getToken(),
                token.getLastUsed()
        );
//        }
//        catch (NoResultException e) {
//            return null;
//        }
    }

    @Override
    public void removeUserTokens(String username) {
//        Query query = entityManager.createQuery("delete from AuthenticationToken where username = :username");
//        query.setParameter("username", username);
//        query.executeUpdate();
    }
}
