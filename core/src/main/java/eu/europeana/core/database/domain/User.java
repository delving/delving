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
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gerald de Jong <geralddejong@gmail.com>
 * @author Nicola Aloia <nicola.aloia@isti.cnr.it>
 */

@Entity
@Table(name = "users")
public class User implements Serializable {
    public static final int USER_NAME_LENGTH = 60;

    private static final long serialVersionUID = -9114716643338178599L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(length = FieldSize.PERSONAL_FIELD, unique = true, nullable = false)
    @Index(name = "email_index")
    private String email;

    @Column(length = FieldSize.PASSWORD)
    private String password;

    @Column(length = USER_NAME_LENGTH)
    @Index(name = "username_index")
    private String userName;

    @Column(length = FieldSize.PERSONAL_FIELD)
    private String firstName;

    @Column(length = FieldSize.PERSONAL_FIELD)
    private String lastName;

    @Column(length = FieldSize.LANGUAGE_LIST)
    private String languages;

    @Column(length = FieldSize.IDENTIFIER)
    private String projectId;

    @Column(length = FieldSize.IDENTIFIER)
    private String providerId;

    @Column
    private boolean newsletter;

    @Column
    @Temporal(TemporalType.DATE)
    private Date registrationDate;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLogin;

    @Column
    private boolean enabled;

    @Column(length = 25)
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "userid", nullable = false)
    private List<SavedItem> savedItems;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "userid", nullable = false)
    private List<SavedSearch> savedSearches;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "userid")
    private List<SocialTag> socialTags;

    @Transient
    private List<SocialTagList> socialTagLists;

    public User(Long id, String userName, String email, String password, String firstName, String lastName, String languages, String projectId, String providerId, boolean newsletter, Role role, boolean enabled) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        if (password.isEmpty()) {
            this.setHashedPassword("");
        } else {
            this.setPassword(password); // hashing it!
        }
        this.firstName = firstName;
        this.lastName = lastName;
        this.languages = languages;
        this.projectId = projectId;
        this.providerId = providerId;
        this.newsletter = newsletter;
        this.role = role;
        this.enabled = enabled;
    }

    public User() {
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        if (email != null) {
            this.email = email.toLowerCase();
        } else {
            this.email = null;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLanguages() {
        return languages;
    }

//    public Set<Language> getLanguageEnums() {
//        if (languages == null) {
//            languages = "";
//        }
//        StringTokenizer tok = new StringTokenizer(languages, ", ");
//        Set<Language> set = EnumSet.noneOf(Language.class);
//        while (tok.hasMoreTokens()) {
//            String token = tok.nextToken();
//            Language language = Language.findByCode(token);
//            set.add(language);
//        }
//        return set;
//    }
//

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProviderId() {
        return providerId;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    public boolean isNewsletter() {
        return newsletter;
    }

    public void setNewsletter(boolean newsletter) {
        this.newsletter = newsletter;
    }

    public String getPassword() {
        return "";
    }

    public void setPassword(String password) {
        this.password = hashPassword(password);
    }

    public String getHashedPassword() {
        return password;
    }

    public void setHashedPassword(String hashedPassword) {
        this.password = hashedPassword;
    }

    public Date getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(Date registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public List<SavedItem> getSavedItems() {
        if (savedItems == null) {
            savedItems = new ArrayList<SavedItem>();
        }
        return savedItems;
    }

    public void setSavedItems(List<SavedItem> savedItems) {
        this.savedItems = savedItems;
    }

    public List<SavedSearch> getSavedSearches() {
        if (savedSearches == null) {
            savedSearches = new ArrayList<SavedSearch>();
        }
        return savedSearches;
    }

    public void setSavedSearches(List<SavedSearch> savedSearches) {
        this.savedSearches = savedSearches;
    }

    public List<SocialTag> getSocialTags() {
        if (socialTags == null) {
            return socialTags = new ArrayList<SocialTag>();
        }
        return socialTags;
    }

    public synchronized List<SocialTagList> getSocialTagLists() {
        if (socialTagLists == null) {
            Map<String, SocialTagList> countMap = new HashMap<String, SocialTagList>();
            for (SocialTag socialTag : socialTags) {
                SocialTagList socialSocialTagList = countMap.get(socialTag.getTag());
                if (socialSocialTagList == null) {
                    countMap.put(socialTag.getTag(), socialSocialTagList = new SocialTagList(socialTag.getTag()));
                }
                socialSocialTagList.getList().add(socialTag);
            }
            socialTagLists = new ArrayList<SocialTagList>(countMap.values());
            Collections.sort(socialTagLists);
        }
        return socialTagLists;
    }

    public void setSocialTags(List<SocialTag> socialTags) {
        this.socialTags = socialTags;
    }

    public static String hashPassword(String password) {
        if (password == null || password.trim().length() == 0) {
            return "";
        } else {
            ShaPasswordEncoder shaPasswordEncoder = new ShaPasswordEncoder();
            return shaPasswordEncoder.encodePassword(password, null);
        }
    }

    public static class SocialTagList implements Comparable<SocialTagList> {
        private String tag;
        private List<SocialTag> list = new ArrayList<SocialTag>();

        public SocialTagList(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }

        public List<SocialTag> getList() {
            return list;
        }

        @Override
        public int compareTo(SocialTagList other) {
            return other.list.size() - list.size();
        }
    }
}
