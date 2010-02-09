package eu.europeana.database.dao;

import eu.europeana.database.TokenDao;
import eu.europeana.database.domain.AuthenticationToken;
import eu.europeana.database.domain.Token;
import eu.europeana.query.EuropeanaQueryException;
import eu.europeana.query.QueryProblem;
import org.apache.log4j.Logger;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

public class TokenDaoImpl implements TokenDao{

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
    


    private static final int TOKEN_LENGTH = 32;
    private static final long MAX_TOKEN_AGE = 1000L*60*60*6;

    @Transactional
    public Token createNewToken(String email) {
        //genereate unique value
        StringBuilder out = new StringBuilder(TOKEN_LENGTH);
        for (int walk=0; walk<TOKEN_LENGTH; walk++) {
            out.append((char)('A'+((int)(Math.random()*26))));
        }
        String unique = out.toString();

        //create token domain
        Token token = new Token(
                unique,
                email,
                System.currentTimeMillis());

        entityManager.persist(token);

        return token;
    }

    @Transactional
    public void removeToken(Token token) {
        Query query = entityManager.createQuery("delete from Token where token = :token");
        query.setParameter("token", token.getToken());
        query.executeUpdate();
    }

    @Transactional
    public Token getToken(String token) throws EuropeanaQueryException {
        if (token == null) {
            throw new EuropeanaQueryException(QueryProblem.UNKNOWN_TOKEN.toString()); // token may not be null
        }

        Token t = entityManager.find(Token.class, token);

        if (t == null) {
            throw new EuropeanaQueryException(QueryProblem.UNKNOWN_TOKEN.toString());
        }

        if (isOlderThan(t, MAX_TOKEN_AGE)) {
            removeToken(t); //token has expired.
            return null;
        }

        return t;
    }

    @Transactional
    public long countTokens() {
        return (Long)entityManager.createQuery("select count(*) from Token").getSingleResult();
    }

    /**
     * This method is intended to be used by unit tests
     */
    @Transactional
    public Token getTokenByEmail(String email) {
        Query q = entityManager.createQuery("select t from Token as t where email = :email");
        q.setParameter("email", email);
        List tokens = q.getResultList();
        if (tokens.isEmpty()) {
            return null;
        }
        return (Token) tokens.get(0);
    }

    public boolean isOlderThan(Token token, long time) {
        return (System.currentTimeMillis() - token.getCreated()) > time;
    }


}
