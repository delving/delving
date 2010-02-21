package eu.europeana.core.util.web;

import eu.europeana.core.database.domain.AuthenticationToken;
import org.apache.log4j.Logger;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;

/**
 * todo: authors? was author Vitali Kiruta
 */
public class TokenRepositoryService implements PersistentTokenRepository {

    private Logger logger = Logger.getLogger(getClass());

    @PersistenceContext
    protected EntityManager entityManager;

    @Transactional
    public void createNewToken(PersistentRememberMeToken persistentRememberMeToken) {
        AuthenticationToken token = new AuthenticationToken(persistentRememberMeToken);
        entityManager.persist(token);
    }

    @Transactional
    public void updateToken(String series, String tokenValue, Date lastUsed) {
        AuthenticationToken token = entityManager.find(AuthenticationToken.class, series);
        token.setToken(tokenValue);
        token.setLastUsed(lastUsed);
    }

    @Transactional
    public PersistentRememberMeToken getTokenForSeries(String series) {
        Query query = entityManager.createQuery("select t from AuthenticationToken as t where t.series = :series");
        query.setParameter("series", series);
        try {
            AuthenticationToken token = (AuthenticationToken) query.getSingleResult();
            return new PersistentRememberMeToken(
                    token.getUsername(),
                    token.getSeries(),
                    token.getToken(),
                    token.getLastUsed()
            );
        }
        catch (NoResultException e) {
            return null;
        }
    }

    @Transactional
    public void removeUserTokens(String username) {
        Query query = entityManager.createQuery("delete from AuthenticationToken where username = :username");
        query.setParameter("username", username);
        query.executeUpdate();
    }
}
