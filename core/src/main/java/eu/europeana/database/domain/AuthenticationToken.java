package eu.europeana.database.domain;

import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;

import javax.persistence.*;
import java.util.Date;

/**
 * todo: authors? was author Vitali Kiruta
 */
@Entity
public class AuthenticationToken {

    @Id
    @Column(length = 64)
    private String series;

    @Column(length = 64, nullable=false)
    private String username;

    @Column(length = 64, nullable=false)
    private String token;

    @Column(nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date   lastUsed;

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
