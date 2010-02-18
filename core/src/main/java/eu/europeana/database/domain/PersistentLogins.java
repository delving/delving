package eu.europeana.database.domain;

import org.hibernate.annotations.Entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Sjoerd Siebinga <sjoerd.siebinga@gmail.com>
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 * @since Dec 23, 2008: 1:11:50 AM
 */

@Entity
@Table(name = "persistent_logins")
public class PersistentLogins implements Serializable {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false, length = 64)
    private String user;

    @Column(nullable = false, length = 64)
    private String series;

//    @Id
    @Column(nullable = false, length = 64, unique = true)
    private String token;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date last_used;

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
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

    public Date getLast_used() {
        return last_used;
    }

    public void setLast_used(Date last_used) {
        this.last_used = last_used;
    }
}
