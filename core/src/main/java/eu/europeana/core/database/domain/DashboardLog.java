/*
 * Copyright 2007 EDL FOUNDATION
 *
 * Licensed under the EUPL, Version 1.1 or - as soon they
 * will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * you may not use this work except in compliance with the
 * Licence.
 * You may obtain a copy of the Licence at:
 *
 * http://ec.europa.eu/idabc/eupl
 *
 * Unless required by applicable law or agreed to in
 * writing, software distributed under the Licence is
 * distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied.
 * See the Licence for the specific language governing
 * permissions and limitations under the Licence.
 */

package eu.europeana.core.database.domain;

import org.hibernate.annotations.Index;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * @author Gerald de Jong, Beautiful Code BV, <geralddejong@gmail.com>
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 */

@Entity
public class DashboardLog implements Serializable {
    private static final long serialVersionUID = -2184440316137953279L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column
    private Long id;

    @Column(nullable = false, length = FieldSize.USER_NAME)
    private String who;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @Index(name = "dashboardlogwhenindex")
    private Date time;

    @Lob
    private String what;

    public DashboardLog() {
    }

    public DashboardLog(String who, Date time, String what) {
        this.who = who;
        this.time = time;
        this.what = what;
    }

    public Long getId() {
        return id;
    }

    public String getWho() {
        return who;
    }

    public Date getTime() {
        return time;
    }

    public String getWhat() {
        return what;
    }

    public String toString() {
        return who + ":" + time + ":" + what;
    }
}