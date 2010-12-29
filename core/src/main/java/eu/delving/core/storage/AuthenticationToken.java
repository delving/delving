package eu.delving.core.storage;

import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import java.util.Date;

/**
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 * @author Cesare Corcordia <cesare.concordia@isti.cnr.it>
 */
public class AuthenticationToken {

    private String series;

    private String username;

    private String token;

    private Date lastUsed;

    public AuthenticationToken() {
    }

    public AuthenticationToken(PersistentRememberMeToken t) {
        setSeries(t.getSeries());
        setUsername(t.getUsername());
        setToken(t.getTokenValue());
        setLastUsed(t.getDate());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Date last_used) {
        this.lastUsed = last_used;
    }
}
