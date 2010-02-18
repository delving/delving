package eu.europeana.database.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 * @author Cesare Corcordia <cesare.concordia@isti.cnr.it>
 */
@Entity
public class Token {

    @Id
    @Column(length = 64, nullable = false)
    private String token;

    @Column(length = 64, nullable = false)
    private String email;

    @Column(nullable = false)
    private long created;

    public Token() {
    }

    public Token(String token, String email, long created) {
        this.token = token;
        this.email = email;
        this.created = created;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
}